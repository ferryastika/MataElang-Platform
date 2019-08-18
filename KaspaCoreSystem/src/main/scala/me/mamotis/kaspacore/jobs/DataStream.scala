package me.mamotis.kaspacore.jobs

import java.sql.Timestamp

import com.databricks.spark.avro.ConfluentSparkAvroUtils
import com.mongodb.client.MongoCollection
import com.mongodb.spark.MongoConnector
import com.mongodb.spark.config.WriteConfig
import me.mamotis.kaspacore.util._
import org.apache.spark.sql.ForeachWriter
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StringType
import org.bson.Document
import org.joda.time.DateTime

import scala.collection.mutable
import scala.collection.JavaConverters._

object DataStream extends Utils {

  def main(args: Array[String]): Unit = {
    //=================================AVRO DESERIALIZER====================================
    val utils = new ConfluentSparkAvroUtils(PropertiesLoader.schemaRegistryUrl)
    val keyDes = utils.deserializerForSubject(PropertiesLoader.kafkaInputTopic + "-key")
    val valDes = utils.deserializerForSubject(PropertiesLoader.kafkaInputTopic + "-value")

    //=================================SPARK CONFIGURATION==================================

    val sparkSession = getSparkSession(args)
    val sparkContext = getSparkContext(sparkSession)

    // Maxmind GeoIP Configuration
    sparkContext.addFile(PropertiesLoader.GeoIpPath)


    // Cassandra Connector
    val connector = getCassandraSession(sparkContext)

    // set implicit and log level
    import sparkSession.implicits._
    sparkContext.setLogLevel("ERROR")

    //==================================KAFKA DEFINITION=====================================

    val kafkaStreamDF = sparkSession.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", PropertiesLoader.kafkaBrokerUrl)
      .option("subscribe", PropertiesLoader.kafkaInputTopic)
      .option("startingOffsets", PropertiesLoader.kafkaStartingOffset)
      .load()

    val decoded = kafkaStreamDF.select(
      valDes(col("value")).alias("value")
    )

    val parsedRawDf = decoded.select("value.*")

    //======================================DATAFRAME PARSING==================================

    //region +++++++++++Raw Data++++++++++++++

    val eventDf = parsedRawDf.select(
      $"timestamp", $"device_id", $"protocol", $"ip_type", $"src_mac", $"dest_mac", $"src_ip",
      $"dest_ip", $"src_port", $"dst_port", $"alert_msg", $"classification", $"priority", $"sig_id",
      $"sig_gen", $"sig_rev", $"company"
    ).map { r =>
      val ts = r.getAs[String](0)
      val device_id = r.getAs[String](1)
      val protocol = r.getAs[String](2)
      val ip_type = r.getAs[String](3)
      val src_mac = r.getAs[String](4)
      val dest_mac = r.getAs[String](5)
      val src_ip = r.getAs[String](6)
      val dest_ip = r.getAs[String](7)
      val src_port = r.getAs[Long](8).toInt
      val dest_port = r.getAs[Long](9).toInt
      val alert_msg = r.getAs[String](10)
      val classification = r.getAs[Long](11).toInt
      val priority = r.getAs[Long](12).toInt
      val sig_id = r.getAs[Long](13).toInt
      val sig_gen = r.getAs[Long](14).toInt
      val sig_rev = r.getAs[Long](15).toInt
      val company = r.getAs[String](16)
      val src_country = Tools.IpLookupCountry(src_ip)
      val src_region = Tools.IpLookupRegion(src_ip)
      val dest_country = Tools.IpLookupCountry(dest_ip)
      val dest_region = Tools.IpLookupRegion(dest_ip)

      val date = new DateTime((ts.toDouble * 1000).toLong)
      val year = date.getYear()
      val month = date.getMonthOfYear()
      val day = date.getDayOfMonth()
      val hour = date.getHourOfDay()
      val minute = date.getMinuteOfHour()
      val second = date.getSecondOfMinute()

      new Commons.EventObj(
        ts, company, device_id, year, month, day, hour, minute, second,
        protocol, ip_type, src_mac, dest_mac, src_ip, dest_ip,
        src_port, dest_port, alert_msg, classification, priority,
        sig_id, sig_gen, sig_rev, src_country, src_region, dest_country, dest_region
      )
    }.toDF(ColsArtifact.colsEventObj: _*)

    val eventDs = eventDf.select($"ts", $"company", $"device_id", $"year", $"month",
      $"day", $"hour", $"minute", $"second", $"protocol", $"ip_type",
      $"src_mac", $"dest_mac", $"src_ip", $"dest_ip", $"src_port",
      $"dest_port", $"alert_msg", $"classification", $"priority",
      $"sig_id", $"sig_gen", $"sig_rev", $"src_country", $"src_region",
      $"dest_country", $"dest_region").as[Commons.EventObj]

    //endregion

    //region lawas

    //+++++++++++++Push Event Hit Company per Second++++++++++++++++++++++
    //+++++Second
    val eventHitCompanySecDf_1 = parsedRawDf.select(to_utc_timestamp(
      from_unixtime($"timestamp"), "GMT").alias("timestamp").cast(StringType), $"company").withColumn("value", lit(1)
    ).groupBy(
      $"company",
      window($"timestamp", "1 seconds").alias("windows")
    ).sum("value")

    val eventHitCompanySecDf_2 = eventHitCompanySecDf_1.select($"company", $"windows.start" , $"sum(value)").map{
      r =>
        val company = r.getAs[String](0)

        val epoch = r.getAs[Timestamp](1).getTime

        val date = new DateTime(epoch)
        val year = date.getYear()
        val month = date.getMonthOfYear()
        val day = date.getDayOfMonth()
        val hour = date.getHourOfDay()
        val minute = date.getMinuteOfHour()
        val second = date.getSecondOfMinute()

        val value = r.getAs[Long](2)

        new Commons.EventHitCompanyObjSec(
          company, year, month, day, hour, minute, second, value
        )
    }.toDF(ColsArtifact.colsEventHitCompanyObjSec: _*)

    val eventHitCompanySecDs = eventHitCompanySecDf_2.select($"company", $"year",
      $"month", $"day", $"hour", $"minute", $"second", $"value").as[Commons.EventHitCompanyObjSec]

    val eventHitCompanySecKafkaDs = eventHitCompanySecDf_2
      .withColumn("value", concat_ws(";", $"company", $"year", $"month", $"day", $"hour", $"minute",
        $"second", $"value")).selectExpr("CAST(value AS STRING)")

    //+++++Minute
    val eventHitCompanyMinDf_1 = parsedRawDf.select(to_utc_timestamp(
      from_unixtime($"timestamp"), "GMT").alias("timestamp").cast(StringType), $"company").withColumn("value", lit(1)
    ).groupBy(
      $"company",
      window($"timestamp", "1 minutes").alias("windows")
    ).sum("value")

    val eventHitCompanyMinDf_2 = eventHitCompanyMinDf_1.select($"company", $"windows.start" , $"sum(value)").map{
      r =>
        val company = r.getAs[String](0)

        val epoch = r.getAs[Timestamp](1).getTime

        val date = new DateTime(epoch)
        val year = date.getYear()
        val month = date.getMonthOfYear()
        val day = date.getDayOfMonth()
        val hour = date.getHourOfDay()
        val minute = date.getMinuteOfHour()

        val value = r.getAs[Long](2)

        new Commons.EventHitCompanyObjMin(
          company, year, month, day, hour, minute, value
        )
    }.toDF(ColsArtifact.colsEventHitCompanyObjMin: _*)

    val eventHitCompanyMinDs = eventHitCompanyMinDf_2.select($"company", $"year",
      $"month", $"day", $"hour", $"minute", $"value").as[Commons.EventHitCompanyObjMin]

    //+++++Hour
    val eventHitCompanyHourDf_1 = parsedRawDf.select(to_utc_timestamp(
      from_unixtime($"timestamp"), "GMT").alias("timestamp").cast(StringType), $"company").withColumn("value", lit(1)
    ).groupBy(
      $"company",
      window($"timestamp", "1 hours").alias("windows")
    ).sum("value")

    val eventHitCompanyHourDf_2 = eventHitCompanyHourDf_1.select($"company", $"windows.start" , $"sum(value)").map{
      r =>
        val company = r.getAs[String](0)

        val epoch = r.getAs[Timestamp](1).getTime

        val date = new DateTime(epoch)
        val year = date.getYear()
        val month = date.getMonthOfYear()
        val day = date.getDayOfMonth()
        val hour = date.getHourOfDay()

        val value = r.getAs[Long](2)

        new Commons.EventHitCompanyObjHour(
          company, year, month, day, hour, value
        )
    }.toDF(ColsArtifact.colsEventHitCompanyObjHour: _*)

    val eventHitCompanyHourDs = eventHitCompanyHourDf_2.select($"company", $"year",
      $"month", $"day", $"hour", $"value").as[Commons.EventHitCompanyObjHour]

    //+++++++++++++Push Event Hit DeviceId per Second++++++++++++++++++++++
    //+++++Second
    val eventHitDeviceIdSecDf_1 = parsedRawDf.select(to_utc_timestamp(
      from_unixtime($"timestamp"), "GMT").alias("timestamp").cast(StringType), $"device_id").withColumn("value", lit(1)
    ).groupBy(
      $"device_id",
      window($"timestamp", "1 seconds").alias("windows")
    ).sum("value")

    val eventHitDeviceIdSecDf_2 = eventHitDeviceIdSecDf_1.select($"device_id", $"windows.start" , $"sum(value)").map{
      r =>
        val device_id = r.getAs[String](0)

        val epoch = r.getAs[Timestamp](1).getTime

        val date = new DateTime(epoch)
        val year = date.getYear()
        val month = date.getMonthOfYear()
        val day = date.getDayOfMonth()
        val hour = date.getHourOfDay()
        val minute = date.getMinuteOfHour()
        val second = date.getSecondOfMinute()

        val value = r.getAs[Long](2)

        new Commons.EventHitDeviceIdObjSec(
          device_id, year, month, day, hour, minute, second, value
        )
    }.toDF(ColsArtifact.colsEventHitDeviceIdObjSec: _*)

    val eventHitDeviceIdSecDs = eventHitDeviceIdSecDf_2.select($"device_id", $"year",
      $"month", $"day", $"hour", $"minute", $"second", $"value").as[Commons.EventHitDeviceIdObjSec]

    //+++++Minute
    val eventHitDeviceIdMinDf_1 = parsedRawDf.select(to_utc_timestamp(
      from_unixtime($"timestamp"), "GMT").alias("timestamp").cast(StringType), $"device_id").withColumn("value", lit(1)
    ).groupBy(
      $"device_id",
      window($"timestamp", "1 minutes").alias("windows")
    ).sum("value")

    val eventHitDeviceIdMinDf_2 = eventHitDeviceIdMinDf_1.select($"device_id", $"windows.start" , $"sum(value)").map{
      r =>
        val device_id = r.getAs[String](0)

        val epoch = r.getAs[Timestamp](1).getTime

        val date = new DateTime(epoch)
        val year = date.getYear()
        val month = date.getMonthOfYear()
        val day = date.getDayOfMonth()
        val hour = date.getHourOfDay()
        val minute = date.getMinuteOfHour()

        val value = r.getAs[Long](2)

        new Commons.EventHitDeviceIdObjMin(
          device_id, year, month, day, hour, minute, value
        )
    }.toDF(ColsArtifact.colsEventHitDeviceIdObjMin: _*)

    val eventHitDeviceIdMinDs = eventHitDeviceIdMinDf_2.select($"device_id", $"year",
      $"month", $"day", $"hour", $"minute", $"value").as[Commons.EventHitDeviceIdObjMin]

    //+++++Hour
    val eventHitDeviceIdHourDf_1 = parsedRawDf.select(to_utc_timestamp(
      from_unixtime($"timestamp"), "GMT").alias("timestamp").cast(StringType), $"device_id").withColumn("value", lit(1)
      ).groupBy(
      $"device_id",
      window($"timestamp", "1 hours").alias("windows")
    ).sum("value")

    val eventHitDeviceIdHourDf_2 = eventHitDeviceIdHourDf_1.select($"device_id", $"windows.start" , $"sum(value)").map{
      r =>
        val device_id = r.getAs[String](0)

        val epoch = r.getAs[Timestamp](1).getTime

        val date = new DateTime(epoch)
        val year = date.getYear()
        val month = date.getMonthOfYear()
        val day = date.getDayOfMonth()
        val hour = date.getHourOfDay()

        val value = r.getAs[Long](2)

        new Commons.EventHitDeviceIdObjHour(
          device_id, year, month, day, hour, value
        )
    }.toDF(ColsArtifact.colsEventHitDeviceIdObjHour: _*)

    val eventHitDeviceIdHourDs = eventHitDeviceIdHourDf_2.select($"device_id", $"year",
      $"month", $"day", $"hour", $"value").as[Commons.EventHitDeviceIdObjHour]

    //endregion

    //region sliding 1 second

    val signature1sDf_1 = parsedRawDf
      .select(
        to_utc_timestamp(from_unixtime($"timestamp"), "GMT").alias("timestamp").cast(StringType),
        $"alert_msg",
        $"src_ip",
        $"dest_ip",
        $"company")
      .withColumn("value", lit(1))
      .groupBy(
        $"alert_msg",
        $"src_ip",
        $"dest_ip",
        $"company",
        window($"timestamp", "1 second").alias("windows"))
      .sum("value")

    val signature1sDf_2 = signature1sDf_1.select($"company", $"alert_msg", $"windows.start", $"sum(value)", $"src_ip", $"dest_ip").map{
      r =>
        val company = r.getAs[String](0)
        val alert_msg = r.getAs[String](1)

        val epoch = r.getAs[Timestamp](2).getTime

        val date = new DateTime(epoch)
        val year = date.getYear()
        val month = date.getMonthOfYear()
        val day = date.getDayOfMonth()
        val hour = date.getHourOfDay()
        val minute = date.getMinuteOfHour()
        val second = date.getSecondOfMinute()

        val value = r.getAs[Long](3)

        val src_ip = r.getAs[String](4)
        val dest_ip = r.getAs[String](5)
        val src_country = Tools.IpLookupCountry(src_ip)
        val dest_country = Tools.IpLookupCountry(dest_ip)

        new Commons.SteviaObjSec(
          company, alert_msg, src_ip, src_country, dest_ip, dest_country, year, month, day, hour, minute, second, value
        )
    }.toDF(ColsArtifact.colsSteviaObjSec: _*)

    val signature1sDs = signature1sDf_2.select($"company", $"alert_msg", $"src_ip", $"src_country", $"dest_ip", $"dest_country", $"year",
      $"month", $"day", $"hour", $"minute", $"second", $"value").as[Commons.SteviaObjSec]

    //endregion

    //region sliding 2 seconds

    val signature2sDf_1 = parsedRawDf
      .select(
        to_utc_timestamp(from_unixtime($"timestamp"), "GMT").alias("timestamp").cast(StringType),
        $"alert_msg",
        $"company")
      .withColumn("value", lit(1))
      .groupBy(
        $"alert_msg",
        $"company",
        window($"timestamp", "2 seconds").alias("windows"))
      .sum("value")

    val signature2sDf_2 = signature2sDf_1.select($"company", $"alert_msg", $"windows.start" , $"sum(value)").map{
      r =>
        val company = r.getAs[String](0)
        val alert_msg = r.getAs[String](1)

        val epoch = r.getAs[Timestamp](2).getTime

        val date = new DateTime(epoch)
        val year = date.getYear()
        val month = date.getMonthOfYear()
        val day = date.getDayOfMonth()
        val hour = date.getHourOfDay()
        val minute = date.getMinuteOfHour()
        val second = date.getSecondOfMinute()

        val value = r.getAs[Long](3)

        new Commons.SignatureHitCompanyObjSec(
          company, alert_msg, year, month, day, hour, minute, second, value
        )
    }.toDF(ColsArtifact.colsSignatureHitCompanyObjSec: _*)

    val signature2sDs = signature2sDf_2.select($"company", $"alert_msg", $"year",
      $"month", $"day", $"hour", $"minute", $"second", $"value").as[Commons.SignatureHitCompanyObjSec]

    //endregion 2

    //region sliding 3 seconds

    val signature3sDf_1 = parsedRawDf
      .select(
        to_utc_timestamp(from_unixtime($"timestamp"), "GMT").alias("timestamp").cast(StringType),
        $"alert_msg",
        $"company")
      .withColumn("value", lit(1))
      .groupBy(
        $"alert_msg",
        $"company",
        window($"timestamp", "3 seconds").alias("windows"))
      .sum("value")

    val signature3sDf_2 = signature3sDf_1.select($"company", $"alert_msg", $"windows.start" , $"sum(value)").map{
      r =>
        val company = r.getAs[String](0)
        val alert_msg = r.getAs[String](1)

        val epoch = r.getAs[Timestamp](2).getTime

        val date = new DateTime(epoch)
        val year = date.getYear()
        val month = date.getMonthOfYear()
        val day = date.getDayOfMonth()
        val hour = date.getHourOfDay()
        val minute = date.getMinuteOfHour()
        val second = date.getSecondOfMinute()

        val value = r.getAs[Long](3)

        new Commons.SignatureHitCompanyObjSec(
          company, alert_msg, year, month, day, hour, minute, second, value
        )
    }.toDF(ColsArtifact.colsSignatureHitCompanyObjSec: _*)

    val signature3sDs = signature3sDf_2.select($"company", $"alert_msg", $"year",
      $"month", $"day", $"hour", $"minute", $"second", $"value").as[Commons.SignatureHitCompanyObjSec]

    //endregion

    //region sliding 5 seconds

    val signature5sDf_1 = parsedRawDf
      .select(
        to_utc_timestamp(from_unixtime($"timestamp"), "GMT").alias("timestamp").cast(StringType),
        $"alert_msg",
        $"company")
      .withColumn("value", lit(1))
      .groupBy(
        $"alert_msg",
        $"company",
        window($"timestamp", "5 seconds").alias("windows"))
      .sum("value")

    val signature5sDf_2 = signature5sDf_1.select($"company", $"alert_msg", $"windows.start" , $"sum(value)").map{
      r =>
        val company = r.getAs[String](0)
        val alert_msg = r.getAs[String](1)

        val epoch = r.getAs[Timestamp](2).getTime

        val date = new DateTime(epoch)
        val year = date.getYear()
        val month = date.getMonthOfYear()
        val day = date.getDayOfMonth()
        val hour = date.getHourOfDay()
        val minute = date.getMinuteOfHour()
        val second = date.getSecondOfMinute()

        val value = r.getAs[Long](3)

        new Commons.SignatureHitCompanyObjSec(
          company, alert_msg, year, month, day, hour, minute, second, value
        )
    }.toDF(ColsArtifact.colsSignatureHitCompanyObjSec: _*)

    val signature5sDs = signature5sDf_2.select($"company", $"alert_msg", $"year",
      $"month", $"day", $"hour", $"minute", $"second", $"value").as[Commons.SignatureHitCompanyObjSec]

    //endregion

    //======================================================MONGO WRITER======================================

    val writerMongo = new ForeachWriter[Commons.EventObj] {

      val writeConfig: WriteConfig = WriteConfig(Map("uri" -> "mongodb://admin:jarkoM@127.0.0.1:27017/stevia.event?replicaSet=rs0&authSource=admin"))
      var mongoConnector: MongoConnector = _
      var events: mutable.ArrayBuffer[Commons.EventObj] = _

      override def open(partitionId: Long, version: Long): Boolean = {
        mongoConnector = MongoConnector(writeConfig.asOptions)
        events = new mutable.ArrayBuffer[Commons.EventObj]()
        true
      }

      override def process(value: Commons.EventObj): Unit = {
        events.append(value)
      }

      override def close(errorOrNull: Throwable): Unit = {
        if (events.nonEmpty) {
          mongoConnector.withCollectionDo(writeConfig, { collection: MongoCollection[Document] =>
            collection.insertMany(events.map(sc => {
              val doc = new Document()
              doc.put("ts", sc.ts)
              doc.put("company", sc.company)
              doc.put("device_id", sc.device_id)
              doc.put("year", sc.year)
              doc.put("month", sc.month)
              doc.put("day", sc.day)
              doc.put("hour", sc.hour)
              doc.put("minute", sc.minute)
              doc.put("second", sc.second)
              doc.put("protocol", sc.protocol)
              doc.put("ip_type", sc.ip_type)
              doc.put("src_mac", sc.src_port)
              doc.put("dest_mac", sc.dest_mac)
              doc.put("src_ip", sc.src_ip)
              doc.put("dest_ip", sc.dest_ip)
              doc.put("src_port", sc.src_port)
              doc.put("dest_port", sc.dest_port)
              doc.put("alert_message", sc.alert_msg)
              doc.put("classification", sc.classification)
              doc.put("priority", sc.sig_id)
              doc.put("sig_id", sc.sig_id)
              doc.put("sig_gen", sc.sig_gen)
              doc.put("sig_rev", sc.sig_rev)
              doc.put("src_country", sc.src_country)
              doc.put("src_region", sc.src_region)
              doc.put("dest_country", sc.dest_country)
              doc.put("dest_region", sc.dest_region)
              doc
            }).asJava)
          })
        }
      }
    }

    //    val writerMongoSig =
    def writerMongoSig(urlConnectMongo : String) : ForeachWriter[Commons.SteviaObjSec] = {
      return new ForeachWriter[Commons.SteviaObjSec] {

        val writeConfig: WriteConfig = WriteConfig(Map("uri" -> urlConnectMongo))
        var mongoConnector: MongoConnector = _
        var events: mutable.ArrayBuffer[Commons.SteviaObjSec] = _

        override def open(partitionId: Long, version: Long): Boolean = {
          mongoConnector = MongoConnector(writeConfig.asOptions)
          events = new mutable.ArrayBuffer[Commons.SteviaObjSec]()
          true
        }

        override def process(value: Commons.SteviaObjSec): Unit = {
          events.append(value)
        }

        override def close(errorOrNull: Throwable): Unit = {
          if (events.nonEmpty) {
            mongoConnector.withCollectionDo(writeConfig, { collection: MongoCollection[Document] =>
              collection.insertMany(events.map(sc => {
                val doc = new Document()
                doc.put("company", sc.company)
                doc.put("year", sc.year)
                doc.put("month", sc.month)
                doc.put("day", sc.day)
                doc.put("hour", sc.hour)
                doc.put("minute", sc.minute)
                doc.put("second", sc.second)
                doc.put("alert_message", sc.alert_msg)
                doc.put("src_ip", sc.src_ip)
                doc.put("src_country", sc.src_country)
                doc.put("dest_ip", sc.dest_ip)
                doc.put("dest_country", sc.dest_country)
                doc.put("value", sc.value)
                doc
              }).asJava)
            })
          }
        }
      }
    }

    //======================================================CASSANDRA WRITER======================================
    val writerEvent = new ForeachWriter[Commons.EventObj] {
      override def open(partitionId: Long, version: Long): Boolean = true

      override def process(value: Commons.EventObj): Unit = {
        PushArtifact.pushRawData(value, connector)
      }

      override def close(errorOrNull: Throwable): Unit = {}
    }

    val writerEventHitCompanySec = new ForeachWriter[Commons.EventHitCompanyObjSec] {
      override def open(partitionId: Long, version: Long): Boolean = true

      override def process(value: Commons.EventHitCompanyObjSec): Unit = {
        PushArtifact.pushEventHitCompanySec(value, connector)
      }

      override def close(errorOrNull: Throwable): Unit = {}
    }

    val writerEventHitCompanyMin = new ForeachWriter[Commons.EventHitCompanyObjMin] {
      override def open(partitionId: Long, version: Long): Boolean = true

      override def process(value: Commons.EventHitCompanyObjMin): Unit = {
        PushArtifact.pushEventHitCompanyMin(value, connector)
      }

      override def close(errorOrNull: Throwable): Unit = {}
    }

    val writerEventHitCompanyHour = new ForeachWriter[Commons.EventHitCompanyObjHour] {
      override def open(partitionId: Long, version: Long): Boolean = true

      override def process(value: Commons.EventHitCompanyObjHour): Unit = {
        PushArtifact.pushEventHitCompanyHour(value, connector)
      }

      override def close(errorOrNull: Throwable): Unit = {}
    }

    val writerEventHitDeviceIdSec = new ForeachWriter[Commons.EventHitDeviceIdObjSec] {
      override def open(partitionId: Long, version: Long): Boolean = true

      override def process(value: Commons.EventHitDeviceIdObjSec): Unit = {
        PushArtifact.pushEventHitDeviceIdSec(value, connector)
      }

      override def close(errorOrNull: Throwable): Unit = {}
    }

    val writerEventHitDeviceIdMin = new ForeachWriter[Commons.EventHitDeviceIdObjMin] {
      override def open(partitionId: Long, version: Long): Boolean = true

      override def process(value: Commons.EventHitDeviceIdObjMin): Unit = {
        PushArtifact.pushEventHitDeviceIdMin(value, connector)
      }

      override def close(errorOrNull: Throwable): Unit = {}
    }

    val writerEventHitDeviceIdHour = new ForeachWriter[Commons.EventHitDeviceIdObjHour] {
      override def open(partitionId: Long, version: Long): Boolean = true

      override def process(value: Commons.EventHitDeviceIdObjHour): Unit = {
        PushArtifact.pushEventHitDeviceIdHour(value, connector)
      }

      override def close(errorOrNull: Throwable): Unit = {}
    }

    //====================================================WRITE QUERY=================================

    val eventPushMongo = eventDs
      .writeStream
      .outputMode("append")
      .queryName("Event Push Mongo")
      .foreach(writerMongo)
      .start()

    val eventPushHDFS = eventDs
      .writeStream
      .format("json")
      .option("path", PropertiesLoader.hadoopEventFilePath)
      .option("checkpointLocation", PropertiesLoader.checkpointLocation)
      .start()

    val eventHitCompanySecQuery = eventHitCompanySecDs
      .writeStream
      .outputMode("update")
      .queryName("EventHitCompanyPerSec")
      .foreach(writerEventHitCompanySec)
      .start()

    val eventHitCompanyMinQuery = eventHitCompanyMinDs
      .writeStream
      .outputMode("update")
      .queryName("EventHitCompanyPerMin")
      .foreach(writerEventHitCompanyMin)
      .start()

    val eventHitCompanyHourQuery = eventHitCompanyHourDs
      .writeStream
      .outputMode("update")
      .queryName("EventHitCompanyPerHour")
      .foreach(writerEventHitCompanyHour)
      .start()

    val eventHitDeviceIdSecQuery = eventHitDeviceIdSecDs
      .writeStream
      .outputMode("update")
      .queryName("EventHitDeviceIdPerSec")
      .foreach(writerEventHitDeviceIdSec)
      .start()

    val eventHitDeviceIdMinQuery = eventHitDeviceIdMinDs
      .writeStream
      .outputMode("update")
      .queryName("EventHitDeviceIdPerMin")
      .foreach(writerEventHitDeviceIdMin)
      .start()

    val eventHitDeviceIdHourQuery = eventHitDeviceIdHourDs
      .writeStream
      .outputMode("update")
      .queryName("EventHitDeviceIdPerHour")
      .foreach(writerEventHitDeviceIdHour)
      .start()    

    val eventPushMongoSig1s = signature1sDs
      .writeStream
      .outputMode("update")
      .queryName("Event Push Mongo 1s Window")
      .foreach(writerMongoSig("mongodb://admin:jarkoM@127.0.0.1:27017/stevia.event1s?replicaSet=rs0&authSource=admin"))
      .start()

//    val eventPushMongoSig2s = signature2sDs
//      .writeStream
//      .outputMode("update")
//      .queryName("Event Push Mongo 2s Window")
//      .foreach(writerMongoSig("mongodb://admin:jarkoM@127.0.0.1:27017/stevia.event2s?replicaSet=rs0&authSource=admin"))
//      .start()
//
//    val eventPushMongoSig3s = signature3sDs
//      .writeStream
//      .outputMode("update")
//      .queryName("Event Push Mongo 3s Window")
//      .foreach(writerMongoSig("mongodb://admin:jarkoM@127.0.0.1:27017/stevia.event3s?replicaSet=rs0&authSource=admin"))
//      .start()
//
//    val eventPushMongoSig5s = signature5sDs
//      .writeStream
//      .outputMode("update")
//      .queryName("Event Push Mongo 5s Window")
//      .format("console")
//      .foreach(writerMongoSig("mongodb://admin:jarkoM@127.0.0.1:27017/stevia.event5s?replicaSet=rs0&authSource=admin"))
//      .start()

    // eventPushHDFS.awaitTermination()
    //    eventPushMongo.awaitTermination()
    //    eventPushMongoSig1s.awaitTermination()
    //    eventPushMongoSig2s.awaitTermination()
    //    eventPushMongoSig3s.awaitTermination()
    //    eventPushMongoSig5s.awaitTermination()

    sparkSession.streams.awaitAnyTermination()
  }
}
