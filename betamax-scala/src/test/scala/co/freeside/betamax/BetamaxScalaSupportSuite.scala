package co.freeside.betamax

import java.net.URL

import scala.io.Source

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BetamaxScalaSupportSuite extends FunSuite {
  
  implicit val config = ProxyConfiguration.builder().build()

  test("plain scala using read") {
    BetamaxScalaSupport.betamax("scala support test") {
      assert(Source.fromURL(new URL("http://localhost:6666")).mkString === "scala test")
    }
  }

}