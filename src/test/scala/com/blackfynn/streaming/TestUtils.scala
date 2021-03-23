// /**
// **   Copyright (c) 2017 Blackfynn, Inc. All Rights Reserved.
// **/

// package com.pennsieve.streaming

// import com.pennsieve.streaming.util.patch
// import org.scalatest.FlatSpec
// /**
//   * Created by jsnavely on 3/21/17.
//   */
// class TestUtils extends FlatSpec {

//   "patching a dataset with an overlapping dataset" should "combine them correctly" in {
//     val target = 1 to 10 toList
//     val fragment = List(30,40,50)
//     val patched = patch(target,fragment,3)
//     assert(patched == List(1,2,30,40,50,6,7,8,9,10))
//   }

//   "patching a dataset with one that covers the tail" should "totally replace the tail of the target" in {

//     val target = 1 to 10 toList
//     val fragment = List(60,70,80,90,100,110,120,130)
//     val patched = patch(target,fragment,6)

//     assert(patched == List(1,2,3,4,5,60,70,80,90,100,110,120,130))

//   }
// }
