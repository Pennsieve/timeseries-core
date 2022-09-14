/**
**   Copyright (c) 2017 Blackfynn, Inc. All Rights Reserved.
**/

package com.pennsieve.streaming

/**
  * Created by jsnavely on 1/18/17.
  */

import scalikejdbc._

case class LookupResultRow(
  id: Long,
  min: Long,
  max: Long,
  sampleRate: Double,
  channel: String,
  file: String
)

class RangeLookUp(rangeQuery:String, s3_baseUrl : String) {

  var autoSession: DBSession = AutoSession
  val rangeQuerySql = SQL(rangeQuery)

  def mapLookupResult(rs: WrappedResultSet): Option[LookupResultRow] = {
    for {
      id <- rs.longOpt("id")
      lower <- rs.longOpt("lo")
      upper <- rs.longOpt("hi")
      rate <- rs.doubleOpt("rate")
      chan <- rs.stringOpt("channel")
      f <- rs.stringOpt("location")
    } yield LookupResultRow(id, lower, upper, rate, chan, s3_baseUrl + f)
  }

  def lookup(qstart: Long, qend: Long, channel: String)(implicit session: DBSession = autoSession): List[LookupResultRow] =
    rangeQuerySql
      .bindByName(Symbol("channel") -> channel, Symbol("qstart") -> qstart, Symbol("qend") -> qend)
      .map(mapLookupResult)
      .list()
      .apply()
      .flatten

  def makeRangeString(min: Long, max: Long): String = s"[$min, $max)"

  def addRangeLookup(l: LookupResultRow)(implicit session: DBSession = autoSession): Long =
    sql"""INSERT INTO timeseries.ranges (channel, location, rate, range, follows_gap) VALUES (?, ?, ?, ?::int8range, false)"""
      .bind(l.channel, l.file, l.sampleRate, makeRangeString(l.min, l.max))
      .updateAndReturnGeneratedKey()
      .apply()

  def updateRangeLookup(l: LookupResultRow)(implicit session: DBSession = autoSession): Int =
    sql"""UPDATE timeseries.ranges set location = ?, range = ?) where id = ?"""
      .bind(l.file, makeRangeString(l.min, l.max), l.id)
      .update()
      .apply()

  def get(channelId: String)(implicit session: DBSession = autoSession): List[LookupResultRow] =
    sql"""SELECT id, location, channel, rate, lower(range) as lo, upper(range) as hi from timeseries.ranges where channel = $channelId"""
      .map(mapLookupResult)
      .list()
      .apply()
      .flatten

  def deleteRangeLookups(channelId: String)(implicit session: DBSession = autoSession): Int =
    sql"DELETE FROM timeseries.ranges where channel = $channelId"
      .update()
      .apply()
}
