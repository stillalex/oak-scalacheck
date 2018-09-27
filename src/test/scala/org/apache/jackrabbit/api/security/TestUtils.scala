package org.apache.jackrabbit.api.security

import org.junit.Assert.assertTrue
import org.scalacheck.Gen
import org.scalacheck.Gen.const
import org.scalacheck.Prop
import org.scalacheck.Test.Parameters
import org.scalacheck.Test.check
import scala.collection.mutable.ListBuffer

object TestUtils {

  def pathGen(root: String): Gen[String] = for {
    id <- Gen.identifier.filter(!_.isEmpty())
    sgm <- Gen.nonEmptyListOf(id)
  } yield root + "/" + sgm.mkString("/")

  def authIdGen: Gen[String] = Gen.identifier.filter(!_.isEmpty())

  def genUnique(gen: Gen[String]): Gen[String] = genUnique(gen, "")

  def genUnique[T](gen: Gen[T], init: T): Gen[T] = {
    val seen: ListBuffer[T] = new ListBuffer[T]
    seen.+=(init)
    gen.filter { i =>
      if (!seen.contains(i)) {
        seen.+=:(i)
        true
      } else {
        false
      }
    }
  }

  def assertProp(test: Prop, len: Int = 500) {
    assertTrue(check(Parameters.defaultVerbose.withMinSuccessfulTests(len), test).passed)
  }
}