package me.mamotis.kaspacore.util
import org.apache.spark.sql.types._

import cats.effect.IO
import com.snowplowanalytics.maxmind.iplookups.IpLookups
import org.apache.spark.SparkFiles

object Tools {

  val schema = new StructType()
    .add("ts", StringType, true)
    .add("company", StringType, true)
    .add("device_id", StringType, true)
    .add("year", IntegerType, true)
    .add("month", IntegerType, true)
    .add("day", IntegerType, true)
    .add("hour", IntegerType, true)
    .add("minute", IntegerType, true)
    .add("second", IntegerType, true)
    .add("protocol", StringType, true)
    .add("ip_type", StringType, true)
    .add("src_mac", StringType, true)
    .add("dest_mac", StringType, true)
    .add("src_ip", StringType, true)
    .add("dest_ip", StringType, true)
    .add("src_port", IntegerType, true)
    .add("dest_port", IntegerType, true)
    .add("alert_msg", StringType, true)
    .add("classification", IntegerType, true)
    .add("priority", IntegerType, true)
    .add("sig_id", IntegerType, true)
    .add("sig_gen", IntegerType, true)
    .add("sig_rev", IntegerType, true)
    .add("src_country", StringType, true)
    .add("src_region", StringType, true)
    .add("dest_country", StringType, true)
    .add("dest_region", StringType, true)

  def IpLookupCountry(ipAddress: String): String = {
    val result = (for {
      ipLookups <- IpLookups.createFromFilenames[IO](
        geoFile = Some(SparkFiles.get(PropertiesLoader.GeoIpFilename)),
        ispFile = None,
        domainFile = None,
        connectionTypeFile = None,
        memCache = false,
        lruCacheSize = 20000
      )

      lookup <- ipLookups.performLookups(ipAddress)
    } yield lookup).unsafeRunSync()

    result.ipLocation match {
      case Some(Right(loc)) =>
        if(loc.countryName == None) "UNDEFINED"
        else loc.countryName
      case _ =>
        "UNDEFINED"
    }
  }

  def IpLookupRegion(ipAddress: String): String = {
    val result = (for {
      ipLookups <- IpLookups.createFromFilenames[IO](
        geoFile = Some(SparkFiles.get(PropertiesLoader.GeoIpFilename)),
        ispFile = None,
        domainFile = None,
        connectionTypeFile = None,
        memCache = false,
        lruCacheSize = 20000
      )

      lookup <- ipLookups.performLookups(ipAddress)
    } yield lookup).unsafeRunSync()

    result.ipLocation match {
      case Some(Right(loc)) =>
        if(loc.regionName == None) "UNDEFINED"
        else loc.regionName.get
      case _ =>
        "UNDEFINED"
    }
  }
}
