package me.mamotis.kaspacore.util

object ColsArtifact {
  //  Event Hit
  val colsEventObj = List("ts", "company", "device_id", "year", "month", "day", "hour", "minute", "second",
    "protocol", "ip_type", "src_mac", "dest_mac", "src_ip", "dest_ip",
    "src_port", "dest_port", "alert_msg", "classification", "priority",
    "sig_id", "sig_gen", "sig_rev", "src_country", "src_region", "dest_country", "dest_region")

  val colsEventObjTs = List("ts", "company", "device_id", "timestamp",
    "protocol", "ip_type", "src_mac", "dest_mac", "src_ip", "dest_ip",
    "src_port", "dest_port", "alert_msg", "classification", "priority",
    "sig_id", "sig_gen", "sig_rev", "src_country", "src_region", "dest_country", "dest_region")

  val colsEventHitCompanyObjSec = List("company", "year", "month", "day", "hour", "minute", "second", "value")

  val colsEventHitCompanyObjMin = List("company", "year", "month", "day", "hour", "minute", "value")

  val colsEventHitCompanyObjHour = List("company", "year", "month", "day", "hour", "value")

  val colsEventHitCompanyObjDay = List("company", "year", "month", "day", "value")

  val colsEventHitDeviceIdObjSec = List("device_id", "year", "month", "day", "hour", "minute", "second", "value")

  val colsEventHitDeviceIdObjMin = List("device_id", "year", "month", "day", "hour", "minute", "value")

  val colsEventHitDeviceIdObjHour = List("device_id", "year", "month", "day", "hour", "value")

  val colsEventHitDeviceIdObjDay = List("device_id", "year", "month", "day", "value")

  //  Signature Hit

  val colsSignatureHitCompanyObjSec = List("company", "alert_msg", "year", "month", "day", "hour", "minute", "second", "value")

  val colsSignatureHitCompanyObjMin = List("company", "alert_msg", "year", "month", "day", "hour", "minute", "value")

  val colsSignatureHitCompanyObjHour = List("company", "alert_msg", "year", "month", "day", "hour", "value")

  val colsSignatureHitCompanyObjDay = List("company", "alert_msg", "year", "month", "day", "value")

  val colsSignatureHitDeviceIdObjSec = List("device_id", "alert_msg", "year", "month", "day", "hour", "minute", "second", "value")

  val colsSignatureHitDeviceIdObjMin = List("device_id", "alert_msg", "year", "month", "day", "hour", "minute", "value")

  val colsSignatureHitDeviceIdObjHour = List("device_id", "alert_msg", "year", "month", "day", "hour", "value")

  val colsSignatureHitDeviceIdObjDay = List("device_id", "alert_msg", "year", "month", "day", "value")

  //  Protocol Hit

  val colsProtocolHitCompanyObjSec = List("company", "protocol", "year", "month", "day", "hour", "minute", "second", "value")

  val colsProtocolHitCompanyObjMin = List("company", "protocol", "year", "month", "day", "hour", "minute", "value")

  val colsProtocolHitCompanyObjHour = List("company", "protocol", "year", "month", "day", "hour", "value")

  val colsProtocolHitCompanyObjDay = List("company", "protocol", "year", "month", "day", "value")

  val colsProtocolHitDeviceIdObjSec = List("device_id", "protocol", "year", "month", "day", "hour", "minute", "second", "value")

  val colsProtocolHitDeviceIdObjMin = List("device_id", "protocol", "year", "month", "day", "hour", "minute", "value")

  val colsProtocolHitDeviceIdObjHour = List("device_id", "protocol", "year", "month", "day", "hour", "value")

  val colsProtocolHitDeviceIdObjDay = List("device_id", "protocol", "year", "month", "day", "value")

  //  Protocol + Port Hit

  val colsProtocolBySPortHitCompanyObjSec = List("company", "protocol", "src_port", "year", "month", "day", "hour", "minute", "second", "value")

  val colsProtocolBySPortHitCompanyObjMin = List("company", "protocol", "src_port", "year", "month", "day", "hour", "minute", "value")

  val colsProtocolBySPortHitCompanyObjHour = List("company", "protocol", "src_port", "year", "month", "day", "hour", "value")

  val colsProtocolBySPortHitCompanyObjDay = List("company", "protocol", "src_port", "year", "month", "day", "value")

  val colsProtocolBySPortHitDeviceIdObjSec = List("device_id", "protocol", "src_port", "year", "month", "day", "hour", "minute", "second", "value")

  val colsProtocolBySPortHitDeviceIdObjMin = List("device_id", "protocol", "src_port", "year", "month", "day", "hour", "minute", "value")

  val colsProtocolBySPortHitDeviceIdObjHour = List("device_id", "protocol", "src_port", "year", "month", "day", "hour", "value")

  val colsProtocolBySPortHitDeviceIdObjDay = List("device_id", "protocol", "src_port", "year", "month", "day", "value")

  val colsProtocolByDPortHitCompanyObjSec = List("company", "protocol", "dest_port", "year", "month", "day", "hour", "minute", "second", "value")

  val colsProtocolByDPortHitCompanyObjMin = List("company", "protocol", "dest_port", "year", "month", "day", "hour", "minute", "value")

  val colsProtocolByDPortHitCompanyObjHour = List("company", "protocol", "dest_port", "year", "month", "day", "hour", "value")

  val colsProtocolByDPortHitCompanyObjDay = List("company", "protocol", "dest_port", "year", "month", "day", "value")

  val colsProtocolByDPortHitDeviceIdObjSec = List("device_id", "protocol", "dest_port", "year", "month", "day", "hour", "minute", "second", "value")

  val colsProtocolByDPortHitDeviceIdObjMin = List("device_id", "protocol", "dest_port", "year", "month", "day", "hour", "minute", "value")

  val colsProtocolByDPortHitDeviceIdObjHour = List("device_id", "protocol", "dest_port", "year", "month", "day", "hour", "value")

  val colsProtocolByDPortHitDeviceIdObjDay = List("device_id", "protocol", "dest_port",  "year", "month", "day", "value")

  //  IP Source, Ip Dest, Country

  val colsIPSourceHitCompanyObjSec = List("company", "src_ip", "country", "year", "month", "day", "hour", "minute", "second", "value")

  val colsIPSourceHitCompanyObjMin = List("company", "src_ip", "country", "year", "month", "day", "hour", "minute", "value")

  val colsIPSourceHitCompanyObjHour = List("company", "src_ip", "country", "year", "month", "day", "hour", "value")

  val colsIPSourceHitCompanyObjDay = List("company", "src_ip", "country", "year", "month", "day", "value")

  val colsIPSourceHitDeviceIdObjSec = List("device_id", "src_ip", "country", "year", "month", "day", "hour", "minute", "second", "value")

  val colsIPSourceHitDeviceIdObjMin = List("device_id", "src_ip", "country", "year", "month", "day", "hour", "minute", "value")

  val colsIPSourceHitDeviceIdObjHour = List("device_id", "src_ip", "country", "year", "month", "day", "hour", "value")

  val colsIPSourceHitDeviceIdObjDay = List("device_id", "src_ip", "country", "year", "month", "day", "value")

  val colsIPDestHitCompanyObjSec = List("company", "dest_ip", "country", "year", "month", "day", "hour", "minute", "second", "value")

  val colsIPDestHitCompanyObjMin = List("company", "dest_ip", "country", "year", "month", "day", "hour", "minute", "value")

  val colsIPDestHitCompanyObjHour = List("company", "dest_ip", "country", "year", "month", "day", "hour", "value")

  val colsIPDestHitCompanyObjDay = List("company", "dest_ip", "country", "year", "month", "day", "value")

  val colsIPDestHitDeviceIdObjSec = List("device_id", "dest_ip", "country", "year", "month", "day", "hour", "minute", "second", "value")

  val colsIPDestHitDeviceIdObjMin = List("device_id", "dest_ip", "country", "year", "month", "day", "hour", "minute", "value")

  val colsIPDestHitDeviceIdObjHour = List("device_id", "dest_ip", "country", "year", "month", "day", "hour", "value")

  val colsIPDestHitDeviceIdObjDay = List("device_id", "dest_ip", "country", "year", "month", "day", "value")

  val colsCountrySrcHitCompanyObjSec = List("company", "src_country", "year", "month", "day", "hour", "minute", "second", "value")

  val colsCountrySrcHitCompanyObjMin = List("company", "src_country", "year", "month", "day", "hour", "minute", "value")

  val colsCountrySrcHitCompanyObjHour = List("company", "src_country", "year", "month", "day", "hour", "value")

  val colsCountrySrcHitCompanyObjDay = List("company", "src_country", "year", "month", "day", "value")

  val colsCountrySrcHitDeviceIdObjSec = List("device_id", "src_country", "year", "month", "day", "hour", "minute", "second", "value")

  val colsCountrySrcHitDeviceIdObjMin = List("device_id", "src_country", "year", "month", "day", "hour", "minute", "value")

  val colsCountrySrcHitDeviceIdObjHour = List("device_id", "src_country", "year", "month", "day", "hour", "value")

  val colsCountrySrcHitDeviceIdObjDay = List("device_id", "src_country", "year", "month", "day", "value")

  val colsCountryDestHitCompanyObjSec = List("company", "dest_country", "year", "month", "day", "hour", "minute", "second", "value")

  val colsCountryDestHitCompanyObjMin = List("company", "dest_country", "year", "month", "day", "hour", "minute", "value")

  val colsCountryDestHitCompanyObjHour = List("company", "dest_country", "year", "month", "day", "hour", "value")

  val colsCountryDestHitCompanyObjDay = List("company", "dest_country", "year", "month", "day", "value")

  val colsCountryDestHitDeviceIdObjSec = List("device_id", "dest_country", "year", "month", "day", "hour", "minute", "second", "value")

  val colsCountryDestHitDeviceIdObjMin = List("device_id", "dest_country", "year", "month", "day", "hour", "minute", "value")

  val colsCountryDestHitDeviceIdObjHour = List("device_id", "dest_country", "year", "month", "day", "hour", "value")

  val colsCountryDestHitDeviceIdObjDay = List("device_id", "dest_country", "year", "month", "day", "value")

  val colsSteviaObjSec = List("company", "alert_msg", "src_ip", "src_country", "dest_ip", "dest_country", "year", "month", "day", "hour", "minute", "second", "value")

}
