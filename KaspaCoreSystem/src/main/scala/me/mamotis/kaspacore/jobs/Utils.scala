package me.mamotis.kaspacore.jobs

import com.datastax.spark.connector.cql.CassandraConnector
import me.mamotis.kaspacore.util.PropertiesLoader
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

private[jobs] trait Utils {
  def getSparkContext(session: SparkSession): SparkContext = {
    session.sparkContext
  }

  def getSparkSession(strings: Array[String]): SparkSession = {
    val conf = new SparkConf(true)
      .setMaster(PropertiesLoader.sparkMaster)
      .setAppName(PropertiesLoader.sparkAppName)
      .set("spark.app.id", PropertiesLoader.sparkAppId)
      .set("spark.cassandra.connection.host", PropertiesLoader.sparkCassandraConnectionHost)
      .set("spark.cassandra.auth.username", PropertiesLoader.cassandraUsername)
      .set("spark.cassandra.auth.password", PropertiesLoader.cassandraPassword)
      .set("spark.cassandra.output.batch.grouping.key", "Partition")
      .set("spark.cassandra.output.concurrent.writes", "2000")
      .set("spark.mongodb.output.uri", PropertiesLoader.mongodbUri)

    val session = SparkSession.builder()
      .config(conf)
      .getOrCreate()

    session
  }

  def getCassandraSession(context: SparkContext): CassandraConnector = {
    val cassandraSession = CassandraConnector.apply(context.getConf)

    cassandraSession
  }
}
