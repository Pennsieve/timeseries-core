/**
**   Copyright (c) 2017 Blackfynn, Inc. All Rights Reserved.
**/

package com.blackfynn.streaming

/**
  * Created by jsnavely on 1/18/17.
  */

import scalikejdbc._

case class UnitRangeEntry(id:Long, min:Long, max:Long, channel:String, count:Long, tsindex:String, tsblob:String)


class UnitRangeLookUp(rangeQuerySQL : String, s3_baseUrl : String) {

  val autoSession = AutoSession
  val rangeQuery = SQL(rangeQuerySQL)

  def mapLookupResult(rs: WrappedResultSet): Option[UnitRangeEntry] = {
    for {
      id <- rs.longOpt("id")
      lower <- rs.longOpt("lo")
      upper <- rs.longOpt("hi")
      count <- rs.longOpt("count")
      channel <- rs.stringOpt("channel")
      tsindex <- rs.stringOpt("tsindex")
      tsblob <- rs.stringOpt("tsblob")
    } yield UnitRangeEntry(id, lower, upper, channel,count, s3_baseUrl + tsindex, s3_baseUrl + tsblob)
  }

  def lookup(qstart: Long, qend: Long, channel: String)(implicit session: DBSession = autoSession): List[UnitRangeEntry] =
    rangeQuery
      .bindByName('channel -> channel, 'qstart -> qstart, 'qend -> qend)
      .map(mapLookupResult)
      .list
      .apply()
      .flatten

  def makeRangeString(min: Long, max: Long) = {
    s"[$min, $max)"
  }

  def addRangeLookup(l: UnitRangeEntry)(implicit session: DBSession = autoSession): Long = {
    sql"""INSERT INTO timeseries.unit_ranges (channel, range, count, tsindex, tsblob) VALUES (?, ?::int8range, ?, ?, ?)"""
      .bind(l.channel, makeRangeString(l.min, l.max), l.count,l.tsindex, l.tsblob)
      .updateAndReturnGeneratedKey
      .apply()
  }
}
