/**
  * *   Copyright (c) 2017 Blackfynn, Inc. All Rights Reserved.
  **/

package com.pennsieve.streaming

import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.zip.GZIPOutputStream

import akka.util.ByteString

/**
  * Created by jsnavely on 3/20/17.
  */

case class CombinableSegment(startTime: Long, endTime: Long, data: Seq[Double], period: Long)

object util {

  def orderSegments(c1: CombinableSegment, c2: CombinableSegment): (CombinableSegment, CombinableSegment) = {
    if (c2.startTime > c1.startTime) {
      (c1, c2)
    } else {
      (c2, c1)
    }
  }

  def patch[T](target: Seq[T], fragment: Seq[T], startIndex: Int): Seq[T] = {
    target.take(startIndex - 1) ++ fragment ++ target.drop(startIndex + fragment.size - 1)
  }

  def combine(c1: CombinableSegment, c2: CombinableSegment): Option[CombinableSegment] = {
    val period = c1.period
    val thresh = Math.round(c1.period * 1.9)
    val (cs1, cs2) = orderSegments(c1, c2)

    if (cs1.period != cs2.period) {
      None
    } else if ((cs2.startTime > cs1.endTime) && (cs2.startTime - cs1.endTime < thresh)) {
      //c2 start where c1 ends, so we can stack them together
      Some(CombinableSegment(cs1.startTime, cs2.endTime, cs1.data ++ cs2.data, cs1.period))
    } else if (cs2.startTime > cs1.startTime && cs2.startTime < cs1.endTime) {
      //c2 overlaps with c1
      val startIndex = Math.round((cs2.startTime - cs1.startTime) / period)
      val newData: Seq[Double] = patch(cs1.data, cs2.data, startIndex)
      val newEnd = Math.max(cs1.endTime, cs2.endTime)
      Some(CombinableSegment(cs1.startTime, newEnd, newData, period))
    } else {
      None
    }
  }

  def writeBinaryFile(data: List[Double], filename: String) = {
    val f = new FileOutputStream(filename)
    val bytes = new Array[Byte](8)
    val bb = ByteBuffer.wrap(bytes)
    bb.rewind()
    data foreach {
      d => {
        bb.clear()
        bb.putDouble(d)
        f.write(bytes)
      }
    }
    f.close()
  }

  def writeGZippedBinaryFile(data: List[Double], filename: String) = {
    val f = new FileOutputStream(filename)
    val zipped = new GZIPOutputStream(f)
    val bytes = new Array[Byte](8)
    val bb = ByteBuffer.wrap(bytes)
    bb.rewind()
    data foreach {
      d => {
        bb.clear()
        bb.putDouble(d)
        zipped.write(bytes)
      }
    }
    zipped.close()
  }

  def getDouble(b: Array[Byte]): Option[Double] = {
    if (b.length == 8) {
      Some(ByteBuffer.wrap(b).getDouble)
    } else {
      None
    }
  }

  def getLong(b: Array[Byte]): Option[Long] = {
    if (b.length == 8) {
      Some(ByteBuffer.wrap(b).getLong)
    } else {
      None
    }
  }

  def getInt(b: Array[Byte]): Option[Int] = {
    if (b.length == 4) {
      Some(ByteBuffer.wrap(b).getInt)
    } else {
      None
    }
  }

  def getDoubleBytes(d: Double): ByteString = {
    val bytes = new Array[Byte](8)
    val bb = ByteBuffer.wrap(bytes).putDouble(d)
    ByteString(bytes)
  }

  def bytesToDouble(bytes: Iterator[Byte]): Vector[Double] = {
    bytes.grouped(8).flatMap { ba =>
      getDouble(ba.toArray)
    }.toVector
  }

  def bytesToDouble(bytes: List[Byte]): Vector[Double] = {
    bytes.grouped(8).flatMap { ba =>
      getDouble(ba.toArray)
    }.toVector
  }

  def bytesToDouble(bytes: Array[Byte]): Vector[Double] = {
    bytes.grouped(8).flatMap { ba =>
      getDouble(ba)
    }.toVector
  }

  def byteStringToTimeGroupPair(bs: ByteString): Option[(Long, Int)] = {
    if (bs.length == 12) {
      for {
        ts <- getLong(bs.take(8).toArray)
        group <- getInt(bs.drop(8).take(4).toArray)
      } yield (ts, group)
    } else {
      None
    }
  }

  def byteStringToTimeGroupPairs(bs: ByteString): Vector[(Long, Int)] =
    bs
      .grouped(12)
      .flatMap(bs => byteStringToTimeGroupPair(bs))
      .toVector

  def byteStringToDoubles(bs: ByteString): Vector[Double] =
    bs
      .grouped(8)
      .flatMap(bs => bytesToDouble(bs.toList))
      .toVector

  def middle[T](ls: Seq[T]): T =
    ls(ls.size / 2)

  def firstOfLast[T](ll: Vector[Vector[T]]): Option[T] = {
    for {
      last <- ll.lastOption
      first <- last.headOption
    } yield first
  }
}
