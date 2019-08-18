package me.mamotis.kaspacore.util

import com.typesafe.config.{Config, ConfigFactory}


object PropertiesLoader {
  private val conf: Config = ConfigFactory.load("application.conf")

  val kafkaBrokerUrl: String = conf.getString("KAFKA_BROKER_URL")
  val schemaRegistryUrl: String = conf.getString("SCHEMA_REGISTRY_URL")
  val kafkaInputTopic: String = conf.getString("KAFKA_INPUT_TOPIC")
  val kafkaStartingOffset: String = conf.getString("KAFKA_STARTING_OFFSET")
  val kafkaBrokerUrlOutput: String = conf.getString("KAFKA_BROKER_URL_OUTPUT")
  val kafkaOutputTopic: String = conf.getString("KAFKA_OUTPUT_TOPIC")

  val sparkMaster: String = conf.getString("SPARK_MASTER")
  val sparkAppName: String = conf.getString("SPARK_APP_NAME")
  val sparkAppId: String = conf.getString("SPARK_APP_ID")
  val sparkCassandraConnectionHost: String = conf.getString("SPARK_CASSANDRA_CONNECTION_HOST")

  val GeoIpPath: String = conf.getString("GEOIP_PATH")
  val GeoIpFilename: String = conf.getString("GEOIP_FILENAME")

  val hadoopEventFilePath: String = conf.getString("HADOOP_EVENT_FILE_PATH")
  val hadoopSchemaFilePath: String = conf.getString("HADOOP_SCHEMA_FILE_PATH")
  val checkpointLocation: String = conf.getString("CHECKPOINT_LOCATION")
  val kafkaCheckpointLocation: String = conf.getString("KAFKA_CHECKPOINT_LOCATION")

  val cassandraUsername: String = conf.getString("CASSANDRA_USERNAME")
  val cassandraPassword: String = conf.getString("CASSANDRA_PASSWORD")
  val cassandraKeyspace: String = conf.getString("CASSANDRA_KEYSPACE")

  val mongodbUri: String = conf.getString("MONGODB_URI")
}
