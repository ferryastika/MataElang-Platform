package me.mamotis.kaspacore.util

import java.util.UUID.randomUUID

import com.datastax.spark.connector.cql.CassandraConnector

object PushArtifact {

  def pushRawData(value: Commons.EventObj, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_raw_data_by_company(randomUUID(), value.ts, value.company, value.device_id, value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.protocol, value.ip_type, value.src_mac, value.dest_mac,
          value.src_ip, value.dest_ip, value.src_port, value.dest_port, value.alert_msg, value.classification,
          value.priority, value.sig_id, value.sig_gen, value.sig_rev, value.src_country, value.src_region, value.dest_country, value.dest_region))

        session.execute(Statements.push_raw_data_by_device_id(randomUUID(), value.ts, value.company, value.device_id, value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.protocol, value.ip_type, value.src_mac, value.dest_mac,
          value.src_ip, value.dest_ip, value.src_port, value.dest_port, value.alert_msg, value.classification,
          value.priority, value.sig_id, value.sig_gen, value.sig_rev, value.src_country, value.src_region, value.dest_country, value.dest_region))
    }
  }

  def pushEventHitCompanySec(value: Commons.EventHitCompanyObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_event_hit_company_second(value.company, value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushEventHitCompanyMin(value: Commons.EventHitCompanyObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_event_hit_company_minute(value.company, value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushEventHitCompanyHour(value: Commons.EventHitCompanyObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_event_hit_company_hour(value.company, value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushEventHitCompanyDay(value: Commons.EventHitCompanyObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_event_hit_company_day(value.company, value.year, value.month, value.day, value.value))
    }
  }

  def pushEventHitDeviceIdSec(value: Commons.EventHitDeviceIdObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_event_hit_device_id_second(value.device_id, value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushEventHitDeviceIdMin(value: Commons.EventHitDeviceIdObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_event_hit_device_id_minute(value.device_id, value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushEventHitDeviceIdHour(value: Commons.EventHitDeviceIdObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_event_hit_device_id_hour(value.device_id, value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushEventHitDeviceIdDay(value: Commons.EventHitDeviceIdObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_event_hit_device_id_day(value.device_id, value.year, value.month, value.day, value.value))
    }
  }

  //  Signature Hit Push Function

  def pushSignatureHitCompanySec(value: Commons.SignatureHitCompanyObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_signature_hit_company_second(value.company, value.alert_msg, value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushSignatureHitCompanyMin(value: Commons.SignatureHitCompanyObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_signature_hit_company_minute(value.company, value.alert_msg, value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushSignatureHitCompanyHour(value: Commons.SignatureHitCompanyObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_signature_hit_company_hour(value.company, value.alert_msg, value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushSignatureHitCompanyDay(value: Commons.SignatureHitCompanyObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_signature_hit_company_day(value.company, value.alert_msg, value.year, value.month, value.day, value.value))
    }
  }

  def pushSignatureHitDeviceIdSec(value: Commons.SignatureHitDeviceIdObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_signature_hit_device_id_second(value.device_id, value.alert_msg, value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushSignatureHitDeviceIdMin(value: Commons.SignatureHitDeviceIdObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_signature_hit_device_id_minute(value.device_id, value.alert_msg, value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushSignatureHitDeviceIdHour(value: Commons.SignatureHitDeviceIdObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_signature_hit_device_id_hour(value.device_id, value.alert_msg, value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushSignatureHitDeviceIdDay(value: Commons.SignatureHitDeviceIdObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_signature_hit_device_id_day(value.device_id, value.alert_msg, value.year, value.month, value.day, value.value))
    }
  }

  //  Protocol Hit Push Function

  def pushProtocolHitCompanySec(value: Commons.ProtocolHitCompanyObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_hit_company_second(value.company, value.protocol, value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushProtocolHitCompanyMin(value: Commons.ProtocolHitCompanyObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_hit_company_minute(value.company, value.protocol, value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushProtocolHitCompanyHour(value: Commons.ProtocolHitCompanyObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_hit_company_hour(value.company, value.protocol, value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushProtocolHitCompanyDay(value: Commons.ProtocolHitCompanyObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_hit_company_day(value.company, value.protocol, value.year, value.month, value.day, value.value))
    }
  }

  def pushProtocolHitDeviceIdSec(value: Commons.ProtocolHitDeviceIdObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_hit_device_id_second(value.device_id, value.protocol, value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushProtocolHitDeviceIdMin(value: Commons.ProtocolHitDeviceIdObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_hit_device_id_minute(value.device_id, value.protocol, value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushProtocolHitDeviceIdHour(value: Commons.ProtocolHitDeviceIdObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_hit_device_id_hour(value.device_id, value.protocol, value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushProtocolHitDeviceIdDay(value: Commons.ProtocolHitDeviceIdObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_hit_device_id_day(value.device_id, value.protocol, value.year, value.month, value.day, value.value))
    }
  }

  //  Protocol + Port Push Function

  def pushProtocolBySPortHitCompanySec(value: Commons.ProtocolBySPortHitCompanyObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_sport_hit_company_second(value.company, value.protocol, value.src_port, value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushProtocolBySPortHitCompanyMin(value: Commons.ProtocolBySPortHitCompanyObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_sport_hit_company_minute(value.company, value.protocol, value.src_port,value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushProtocolBySPortHitCompanyHour(value: Commons.ProtocolBySPortHitCompanyObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_sport_hit_company_hour(value.company, value.protocol, value.src_port,value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushProtocolBySPortHitCompanyDay(value: Commons.ProtocolBySPortHitCompanyObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_sport_hit_company_day(value.company, value.protocol, value.src_port,value.year, value.month, value.day, value.value))
    }
  }

  def pushProtocolBySPortHitDeviceIdSec(value: Commons.ProtocolBySPortHitDeviceIdObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_sport_hit_device_id_second(value.device_id, value.protocol, value.src_port, value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushProtocolBySPortHitDeviceIdMin(value: Commons.ProtocolBySPortHitDeviceIdObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_sport_hit_device_id_minute(value.device_id, value.protocol, value.src_port,value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushProtocolBySPortHitDeviceIdHour(value: Commons.ProtocolBySPortHitDeviceIdObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_sport_hit_device_id_hour(value.device_id, value.protocol, value.src_port,value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushProtocolBySPortHitDeviceIdDay(value: Commons.ProtocolBySPortHitDeviceIdObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_sport_hit_device_id_day(value.device_id, value.protocol, value.src_port,value.year, value.month, value.day, value.value))
    }
  }

  def pushProtocolByDPortHitCompanySec(value: Commons.ProtocolByDPortHitCompanyObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_dport_hit_company_second(value.company, value.protocol, value.dest_port,value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushProtocolByDPortHitCompanyMin(value: Commons.ProtocolByDPortHitCompanyObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_dport_hit_company_minute(value.company, value.protocol, value.dest_port,value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushProtocolByDPortHitCompanyHour(value: Commons.ProtocolByDPortHitCompanyObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_dport_hit_company_hour(value.company, value.protocol, value.dest_port,value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushProtocolByDPortHitCompanyDay(value: Commons.ProtocolByDPortHitCompanyObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_dport_hit_company_day(value.company, value.protocol, value.dest_port,value.year, value.month, value.day, value.value))
    }
  }

  def pushProtocolByDPortHitDeviceIdSec(value: Commons.ProtocolByDPortHitDeviceIdObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_dport_hit_device_id_second(value.device_id, value.protocol, value.dest_port,value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushProtocolByDPortHitDeviceIdMin(value: Commons.ProtocolByDPortHitDeviceIdObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_dport_hit_device_id_minute(value.device_id, value.protocol, value.dest_port,value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushProtocolByDPortHitDeviceIdHour(value: Commons.ProtocolByDPortHitDeviceIdObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_dport_hit_device_id_hour(value.device_id, value.protocol, value.dest_port,value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushProtocolByDPortHitDeviceIdDay(value: Commons.ProtocolByDPortHitDeviceIdObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_protocol_by_dport_hit_device_id_day(value.device_id, value.protocol, value.dest_port,value.year, value.month, value.day, value.value))
    }
  }

  //  IP + Country Push Function

  def pushIPSourceHitCompanySec(value: Commons.IPSourceHitCompanyObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_source_hit_company_second(value.company, value.src_ip, value.country,value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushIPSourceHitCompanyMin(value: Commons.IPSourceHitCompanyObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_source_hit_company_minute(value.company, value.src_ip, value.country,value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushIPSourceHitCompanyHour(value: Commons.IPSourceHitCompanyObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_source_hit_company_hour(value.company, value.src_ip, value.country,value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushIPSourceHitCompanyDay(value: Commons.IPSourceHitCompanyObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_source_hit_company_day(value.company, value.src_ip, value.country,value.year, value.month, value.day, value.value))
    }
  }

  def pushIPSourceHitDeviceIdSec(value: Commons.IPSourceHitDeviceIdObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_source_hit_device_id_second(value.device_id, value.src_ip, value.country,value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushIPSourceHitDeviceIdMin(value: Commons.IPSourceHitDeviceIdObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_source_hit_device_id_minute(value.device_id, value.src_ip, value.country,value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushIPSourceHitDeviceIdHour(value: Commons.IPSourceHitDeviceIdObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_source_hit_device_id_hour(value.device_id, value.src_ip, value.country,value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushIPSourceHitDeviceIdDay(value: Commons.IPSourceHitDeviceIdObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_source_hit_device_id_day(value.device_id, value.src_ip, value.country,value.year, value.month, value.day, value.value))
    }
  }

  def pushIPDestHitCompanySec(value: Commons.IPDestHitCompanyObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_dest_hit_company_second(value.company, value.dest_ip, value.country,value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushIPDestHitCompanyMin(value: Commons.IPDestHitCompanyObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_dest_hit_company_minute(value.company, value.dest_ip, value.country,value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushIPDestHitCompanyHour(value: Commons.IPDestHitCompanyObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_dest_hit_company_hour(value.company, value.dest_ip, value.country,value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushIPDestHitCompanyDay(value: Commons.IPDestHitCompanyObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_dest_hit_company_day(value.company, value.dest_ip, value.country,value.year, value.month, value.day, value.value))
    }
  }

  def pushIPDestHitDeviceIdSec(value: Commons.IPDestHitDeviceIdObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_dest_hit_device_id_second(value.device_id, value.dest_ip, value.country,value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushIPDestHitDeviceIdMin(value: Commons.IPDestHitDeviceIdObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_dest_hit_device_id_minute(value.device_id, value.dest_ip, value.country,value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushIPDestHitDeviceIdHour(value: Commons.IPDestHitDeviceIdObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_dest_hit_device_id_hour(value.device_id, value.dest_ip, value.country,value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushIPDestHitDeviceIdDay(value: Commons.IPDestHitDeviceIdObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_ip_dest_hit_device_id_day(value.device_id, value.dest_ip, value.country,value.year, value.month, value.day, value.value))
    }
  }

  def pushCountrySrcHitCompanySec(value: Commons.CountrySrcHitCompanyObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_source_hit_company_second(value.company, value.src_country, value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushCountrySrcHitCompanyMin(value: Commons.CountrySrcHitCompanyObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_source_hit_company_minute(value.company, value.src_country, value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushCountrySrcHitCompanyHour(value: Commons.CountrySrcHitCompanyObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_source_hit_company_hour(value.company, value.src_country, value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushCountrySrcHitCompanyDay(value: Commons.CountrySrcHitCompanyObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_source_hit_company_day(value.company, value.src_country, value.year, value.month, value.day, value.value))
    }
  }

  def pushCountrySrcHitDeviceIdSec(value: Commons.CountrySrcHitDeviceIdObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_source_hit_device_id_second(value.device_id, value.src_country, value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushCountrySrcHitDeviceIdMin(value: Commons.CountrySrcHitDeviceIdObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_source_hit_device_id_minute(value.device_id, value.src_country, value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushCountrySrcHitDeviceIdHour(value: Commons.CountrySrcHitDeviceIdObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_source_hit_device_id_hour(value.device_id, value.src_country, value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushCountrySrcHitDeviceIdDay(value: Commons.CountrySrcHitDeviceIdObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_source_hit_device_id_day(value.device_id, value.src_country, value.year, value.month, value.day, value.value))
    }
  }

  def pushCountryDestHitCompanySec(value: Commons.CountryDestHitCompanyObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_dest_hit_company_second(value.company, value.dest_country, value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushCountryDestHitCompanyMin(value: Commons.CountryDestHitCompanyObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_dest_hit_company_minute(value.company, value.dest_country, value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushCountryDestHitCompanyHour(value: Commons.CountryDestHitCompanyObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_dest_hit_company_hour(value.company, value.dest_country, value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushCountryDestHitCompanyDay(value: Commons.CountryDestHitCompanyObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_dest_hit_company_day(value.company, value.dest_country, value.year, value.month, value.day, value.value))
    }
  }

  def pushCountryDestHitDeviceIdSec(value: Commons.CountryDestHitDeviceIdObjSec, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_dest_hit_device_id_second(value.device_id, value.dest_country, value.year, value.month, value.day, value.hour,
          value.minute, value.second, value.value))
    }
  }

  def pushCountryDestHitDeviceIdMin(value: Commons.CountryDestHitDeviceIdObjMin, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_dest_hit_device_id_minute(value.device_id, value.dest_country, value.year, value.month, value.day, value.hour,
          value.minute, value.value))
    }
  }

  def pushCountryDestHitDeviceIdHour(value: Commons.CountryDestHitDeviceIdObjHour, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_dest_hit_device_id_hour(value.device_id, value.dest_country, value.year, value.month, value.day, value.hour, value.value))
    }
  }

  def pushCountryDestHitDeviceIdDay(value: Commons.CountryDestHitDeviceIdObjDay, connector: CassandraConnector) = {
    connector.withSessionDo{
      session =>
        session.execute(Statements.push_country_dest_hit_device_id_day(value.device_id, value.dest_country, value.year, value.month, value.day, value.value))
    }
  }
}
