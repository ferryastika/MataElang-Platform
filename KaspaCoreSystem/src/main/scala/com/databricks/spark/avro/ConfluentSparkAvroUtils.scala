package com.databricks.spark.avro

import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.services.kms.{ AWSKMS, AWSKMSClient }
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import java.nio.ByteBuffer
import org.apache.avro.Schema
import org.apache.commons.logging.LogFactory
import org.apache.spark.sql.catalyst.expressions.GenericRow
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.types.DataType

import scala.collection.mutable
import scala.collection.JavaConverters._
import scalaz.Memo

class ConfluentSparkAvroUtils(schemaRegistryURLs: String) extends Serializable {
  @transient private lazy val logger = LogFactory.getLog(this.getClass)

  @transient private lazy val schemaRegistry = {
    // Multiple URLs for HA mode.
    val urls = schemaRegistryURLs.split(",").toList.asJava
    // Store up to 128 schemas.
    val cacheCapacity = 128

    logger.info("Connecting to schema registry server at " + schemaRegistryURLs)

    new CachedSchemaRegistryClient(urls, cacheCapacity)
  }

  @transient private lazy val deserializer = new KafkaAvroDeserializer(schemaRegistry)

  @transient private lazy val awsKms: AWSKMS = new AWSKMSClient()

  @transient private lazy val keyCache = mutable.Map[Seq[Byte], Array[Byte]]()

  val AES_MAGIC_BYTES = Set[Byte](2, 3)
  val HEADER_SIZE = 5
  val AES_ENCRYPTED_KEY_SIZE = 184

  def deserializerForSubject(subject: String, version: String): UserDefinedFunction = {
    udf(
      (payload: Array[Byte]) => {
        val magicByte: Byte = payload(0)

        val data: Array[Byte] =
          if (AES_MAGIC_BYTES(magicByte)) {
            // Decrypt Key
            val encryptedKey: Array[Byte] = payload.slice(HEADER_SIZE,
              HEADER_SIZE + AES_ENCRYPTED_KEY_SIZE)
            val decryptKey = (encryptedKey: Array[Byte]) => {
              val encryptedKeyBuff: ByteBuffer = ByteBuffer.wrap(encryptedKey)
              val decryptRequest = new DecryptRequest().withCiphertextBlob(encryptedKeyBuff)
              val plainKey = awsKms.decrypt(decryptRequest).getPlaintext()
              val plainKeyData = Array.fill(plainKey.remaining()){plainKey.get}
              plainKeyData
            }

            val plainKeyBytes: Array[Byte] = keyCache.getOrElseUpdate(encryptedKey,
              decryptKey(encryptedKey))

            // Decrypt Data
            val cipher = Cipher.getInstance("AES")
            val keySpec = new SecretKeySpec(plainKeyBytes, "AES")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decryptedData: Array[Byte] = cipher.doFinal(
              payload.slice(HEADER_SIZE + AES_ENCRYPTED_KEY_SIZE, payload.size)
            )
            val data = payload.slice(0, HEADER_SIZE) ++ decryptedData

            // hack: magic byte could be only 0 or 1 for confluent lib compatibility
            data(0) = 0
            data
          } else {
            payload
          }

        val avroSchema = avroSchemaForSubject(subject, version)
        val dataType = dataTypeForSubject(subject, version)
        val obj = deserializer.deserialize("", data, avroSchema)
        val toSqlRow = sqlConverter(avroSchema, dataType)

        toSqlRow(obj).asInstanceOf[GenericRow]
      },
      dataTypeForSubject(subject, version)
    )
  }

  def deserializerForSubject(subject: String): UserDefinedFunction = deserializerForSubject(subject, "latest")

  @transient private lazy val avroSchemaForSubject: ((String, String)) => Schema = Memo.mutableHashMapMemo {
    subjectAndVersion =>
      var schemaId: Int = -1

      if (subjectAndVersion._2 == "latest") {
        schemaId = schemaRegistry.getLatestSchemaMetadata(subjectAndVersion._1).getId
      } else {
        schemaId = schemaRegistry.getSchemaMetadata(subjectAndVersion._1, subjectAndVersion._2.toInt).getId
      }

      logger.info("Resolving avro schema for subject " + subjectAndVersion._1 + " with version " + subjectAndVersion._2)

      schemaRegistry.getByID(schemaId)
  }

  @transient private lazy val dataTypeForSubject: ((String, String)) => DataType = Memo.mutableHashMapMemo {
    subjectAndVersion =>
      SchemaConverters.toSqlType(avroSchemaForSubject(subjectAndVersion._1, subjectAndVersion._2)).dataType
  }

  @transient private lazy val sqlConverter: ((Schema, DataType)) => Function[AnyRef, AnyRef] = Memo.mutableHashMapMemo {
    schemaAndType =>
      SchemaConverters.createConverterToSQL(schemaAndType._1, schemaAndType._2)
  }
}
