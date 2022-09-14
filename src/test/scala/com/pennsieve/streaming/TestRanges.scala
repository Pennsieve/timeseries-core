/**
**   Copyright (c) 2017 Blackfynn, Inc. All Rights Reserved.
**/

package com.pennsieve.streaming

import org.junit.runner.Description
import org.scalatest.{ BeforeAndAfterAll, DoNotDiscover, FlatSpec }
import com.pennsieve.test.{PersistantTestContainers, PostgresDockerContainer}
import scalikejdbc.AutoSession
import scalikejdbc.ConnectionPool

/**
  * Created by jsnavely on 4/26/17.
  */
class TestRanges extends FlatSpec with BeforeAndAfterAll
    with PersistantTestContainers
    with PostgresDockerContainer {

  implicit val autoSession = AutoSession

  override def afterStart(): Unit = {
    Class.forName("org.postgresql.Driver")
    ConnectionPool.singleton(
      s"jdbc:postgresql://${postgresContainer.containerIpAddress}:${postgresContainer.mappedPort()}/postgres",
      "postgres",
      "password"
    )
    super.afterStart()
  }

  val rangeUnitQuery = "select id, count, channel, tsindex, tsblob, lower(range) as lo, upper(range) as hi from timeseries.unit_ranges where (channel = {channel}) and (range && int8range({qstart},{qend}))"

  val unitRangeLookup = new UnitRangeLookUp(rangeUnitQuery,"s3://test/")

  "unit range lookups" should "include only intersected ranges" in {

    unitRangeLookup.addRangeLookup(UnitRangeEntry(0,100,200,"channela",123L,"tsindex1","tsblob1"))
    unitRangeLookup.addRangeLookup(UnitRangeEntry(0,200,300,"channela",123L,"tsindex2","tsblob2"))
    unitRangeLookup.addRangeLookup(UnitRangeEntry(0,300,400,"channela",123L,"tsindex3","tsblob3"))
    unitRangeLookup.addRangeLookup(UnitRangeEntry(0,400,500,"channela",123L,"tsindex4","tsblob4"))

    val results = unitRangeLookup.lookup(220,320,"channela")

    assert(results.head.tsindex == "s3://test/tsindex2")
    assert(results.last.tsindex == "s3://test/tsindex3")
    assert(results.size == 2)

  }
}
