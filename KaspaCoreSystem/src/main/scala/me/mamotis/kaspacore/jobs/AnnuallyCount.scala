package me.mamotis.kaspacore.jobs

import me.mamotis.kaspacore.util.PropertiesLoader
import java.time.LocalDate

import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

object AnnuallyCount extends Utils {

  def main(args: Array[String]): Unit = {
    val sparkSession = getSparkSession(args)
    val sparkContext = getSparkContext(sparkSession)
    val schema = StructType(
      Array(
        StructField("ts", StringType, nullable = true),
        StructField("company", StringType, nullable = true),
        StructField("device_id", StringType, nullable = true),
        StructField("year", IntegerType, nullable = true),
        StructField("month", IntegerType, nullable = true),
        StructField("day", IntegerType, nullable = true),
        StructField("hour", IntegerType, nullable = true),
        StructField("minute", IntegerType, nullable = true),
        StructField("second", IntegerType, nullable = true),
        StructField("protocol", StringType, nullable = true),
        StructField("ip_type", StringType, nullable = true),
        StructField("src_mac", StringType, nullable = true),
        StructField("dest_mac", StringType, nullable = true),
        StructField("src_ip", StringType, nullable = true),
        StructField("dest_ip", StringType, nullable = true),
        StructField("src_port", IntegerType, nullable = true),
        StructField("dest_port", IntegerType, nullable = true),
        StructField("alert_msg", StringType, nullable = true),
        StructField("classification", IntegerType, nullable = true),
        StructField("priority", IntegerType, nullable = true),
        StructField("sig_id", IntegerType, nullable = true),
        StructField("sig_gen", IntegerType, nullable = true),
        StructField("sig_rev", IntegerType, nullable = true),
        StructField("src_country", StringType, nullable = true),
        StructField("src_region", StringType, nullable = true),
        StructField("dest_country", StringType, nullable = true),
        StructField("dest_region", StringType, nullable = true)
      )
    )

    val connector = getCassandraSession(sparkContext)

    import sparkSession.implicits._
    sparkContext.setLogLevel("ERROR")

    val rawDf = sparkSession
      .read.schema(schema).json(PropertiesLoader.hadoopEventFilePath)
      .select($"company", $"device_id", $"protocol", $"src_port", $"dest_port", $"src_ip", $"dest_ip", $"src_country",
        $"dest_country", $"alert_msg", $"year")
      .na.fill(Map("src_country" -> "UNDEFINED", "dest_country" -> "UNDEFINED"))
      .filter($"year" === LocalDate.now.getYear)
      .filter($"month" === LocalDate.now.getMonthValue)
      .filter($"day" === LocalDate.now.getDayOfMonth)
      .cache()

    // ======================================Company==============================
    // Hit Count
    val countedHitCompanyDf = rawDf
      .groupBy($"company")
      .count()
      .sort($"company".desc, $"count".desc)

    val pushHitCompanyDf = countedHitCompanyDf
      .select(
        $"company",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    // Signature
    val countedSignatureCompanyDf = rawDf
      .groupBy($"company", $"alert_msg")
      .count()
      .sort($"company".desc, $"count".desc)

    val pushSignatureCompanyDf = countedSignatureCompanyDf
      .select(
        $"company", $"alert_msg",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    //Protocol
    val countedProtocolCompanyDf = rawDf
      .groupBy($"company", $"protocol")
      .count()
      .sort($"company".desc, $"count".desc)

    val pushProtocolCompanyDf = countedProtocolCompanyDf
      .select(
        $"company", $"protocol",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    //ProtocolBySPort
    val countedProtocolBySPortCompanyDf = rawDf
      .groupBy($"company", $"protocol", $"src_port")
      .count()
      .sort($"company".desc, $"count".desc)

    val pushProtocolBySPortCompanyDf = countedProtocolBySPortCompanyDf
      .select(
        $"company", $"protocol", $"src_port",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    //ProtocolByDPort
    val countedProtocolByDPortCompanyDf = rawDf
      .groupBy($"company", $"protocol", $"dest_port")
      .count()
      .sort($"company".desc, $"count".desc)

    val pushProtocolByDPortCompanyDf = countedProtocolByDPortCompanyDf
      .select(
        $"company", $"protocol", $"dest_port",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    //IpSrc
    val countedIpSrcCompanyDf = rawDf
      .groupBy($"company", $"src_country", $"src_ip")
      .count()
      .sort($"company".desc, $"count".desc)

    val pushIpSrcCompanyDf = countedIpSrcCompanyDf
      .select(
        $"company", $"src_country".alias("country").as[String], $"src_ip",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    //IpDest
    val countedIpDestCompanyDf = rawDf
      .groupBy($"company", $"dest_country", $"dest_ip")
      .count()
      .sort($"company".desc, $"count".desc)

    val pushIpDestCompanyDf = countedIpDestCompanyDf
      .select(
        $"company", $"dest_country".alias("country").as[String], $"dest_ip",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    //CountrySrc
    val countedCountrySrcCompanyDf = rawDf
      .groupBy($"company", $"src_country")
      .count()
      .sort($"company".desc, $"count".desc)

    val pushCountrySrcCompanyDf = countedCountrySrcCompanyDf
      .select(
        $"company", $"src_country",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    //CountryDest
    val countedCountryDestCompanyDf = rawDf
      .groupBy($"company", $"dest_country")
      .count()
      .sort($"company".desc, $"count".desc)

    val pushCountryDestCompanyDf = countedCountryDestCompanyDf
      .select(
        $"company", $"dest_country",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))




    // ======================================Company===============================


    // ======================================Device ID===============================
    // Hit Count
    val countedHitDeviceIdDf = rawDf
      .groupBy($"device_id")
      .count()
      .sort($"device_id".desc, $"count".desc)

    val pushHitDeviceIdDf = countedHitDeviceIdDf
      .select(
        $"device_id",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    // Signature
    val countedSignatureDeviceIdDf = rawDf
      .groupBy($"device_id", $"alert_msg")
      .count()
      .sort($"device_id".desc, $"count".desc)

    val pushSignatureDeviceIdDf = countedSignatureDeviceIdDf
      .select(
        $"device_id", $"alert_msg",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    // Protocol
    val countedProtocolDeviceIdDf = rawDf
      .groupBy($"device_id", $"protocol")
      .count()
      .sort($"device_id".desc, $"count".desc)

    val pushProtocolDeviceIdDf = countedProtocolDeviceIdDf
      .select(
        $"device_id", $"protocol",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    //ProtocolBySPort
    val countedProtocolBySPortDeviceIdDf = rawDf
      .groupBy($"device_id", $"protocol", $"src_port")
      .count()
      .sort($"device_id".desc, $"count".desc)

    val pushProtocolBySPortDeviceIdDf = countedProtocolBySPortDeviceIdDf
      .select(
        $"device_id", $"protocol", $"src_port",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    //ProtocolByDPort
    val countedProtocolByDPortDeviceIdDf = rawDf
      .groupBy($"device_id", $"protocol", $"dest_port")
      .count()
      .sort($"device_id".desc, $"count".desc)

    val pushProtocolByDPortDeviceIdDf = countedProtocolByDPortDeviceIdDf
      .select(
        $"device_id", $"protocol", $"dest_port",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    //IpSrc
    val countedIpSrcDeviceIdDf = rawDf
      .groupBy($"device_id", $"src_country", $"src_ip")
      .count()
      .sort($"device_id".desc, $"count".desc)

    val pushIpSrcDeviceIdDf = countedIpSrcDeviceIdDf
      .select(
        $"device_id", $"src_country".alias("country").as[String], $"src_ip",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    //IpDest
    val countedIpDestDeviceIdDf = rawDf
      .groupBy($"device_id", $"dest_country", $"dest_ip")
      .count()
      .sort($"device_id".desc, $"count".desc)

    val pushIpDestDeviceIdDf = countedIpDestDeviceIdDf
      .select(
        $"device_id", $"dest_country".alias("country").as[String], $"dest_ip",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    //CountrySrc
    val countedCountrySrcDeviceIdDf = rawDf
      .groupBy($"device_id", $"src_country")
      .count()
      .sort($"device_id".desc, $"count".desc)

    val pushCountrySrcDeviceIdDf = countedCountrySrcDeviceIdDf
      .select(
        $"device_id", $"src_country",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    //CountryDest
    val countedCountryDestDeviceIdDf = rawDf
      .groupBy($"device_id", $"dest_country")
      .count()
      .sort($"device_id".desc, $"count".desc)

    val pushCountryDestDeviceIdDf = countedCountryDestDeviceIdDf
      .select(
        $"device_id", $"dest_country",
        $"count".alias("value").as[Long]
      )
      .withColumn("year", lit(LocalDate.now.getYear))



    // ======================================Device ID===============================

    // ======================================Query Company===============================
    // Event Hit
    pushHitCompanyDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "event_hit_on_company_year"))
      .mode(SaveMode.Append)
      .save()

    // Signature
    pushSignatureCompanyDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "signature_hit_on_company_year"))
      .mode(SaveMode.Append)
      .save()

    // Protocol
    pushProtocolCompanyDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "protocol_hit_on_company_year"))
      .mode(SaveMode.Append)
      .save()

    // Protocol Source Port
    pushProtocolBySPortCompanyDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "protocol_by_sport_hit_on_company_year"))
      .mode(SaveMode.Append)
      .save()

    // Protocol Dest Port
    pushProtocolByDPortCompanyDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "protocol_by_dport_hit_on_company_year"))
      .mode(SaveMode.Append)
      .save()

    // IP Source
    pushIpSrcCompanyDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "ip_source_hit_on_company_year"))
      .mode(SaveMode.Append)
      .save()

    // IP Destination
    pushIpDestCompanyDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "ip_dest_hit_on_company_year"))
      .mode(SaveMode.Append)
      .save()

    // Source Country
    pushCountrySrcCompanyDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "country_source_hit_on_company_year"))
      .mode(SaveMode.Append)
      .save()

    // Destination Country
    pushCountryDestCompanyDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "country_dest_hit_on_company_year"))
      .mode(SaveMode.Append)
      .save()

    // ======================================Query Device ID===============================
    // Event Hit
    pushHitDeviceIdDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "event_hit_on_device_id_year"))
      .mode(SaveMode.Append)
      .save()

    // Signature
    pushSignatureDeviceIdDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "signature_hit_on_device_id_year"))
      .mode(SaveMode.Append)
      .save()

    // Protocol
    pushProtocolDeviceIdDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "protocol_hit_on_device_id_year"))
      .mode(SaveMode.Append)
      .save()

    // Protocol Source Port
    pushProtocolBySPortDeviceIdDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "protocol_by_sport_hit_on_device_id_year"))
      .mode(SaveMode.Append)
      .save()

    // Protocol Dest Port
    pushProtocolByDPortDeviceIdDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "protocol_by_dport_hit_on_device_id_year"))
      .mode(SaveMode.Append)
      .save()

    // IP Source
    pushIpSrcDeviceIdDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "ip_source_hit_on_device_id_year"))
      .mode(SaveMode.Append)
      .save()

    // IP Destination
    pushIpDestDeviceIdDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "ip_dest_hit_on_device_id_year"))
      .mode(SaveMode.Append)
      .save()

    // Source Country
    pushCountrySrcDeviceIdDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "country_source_hit_on_device_id_year"))
      .mode(SaveMode.Append)
      .save()

    // Destination Country
    pushCountryDestDeviceIdDf
      .write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> PropertiesLoader.cassandraKeyspace, "table" -> "country_dest_hit_on_device_id_year"))
      .mode(SaveMode.Append)
      .save()
  }

}
