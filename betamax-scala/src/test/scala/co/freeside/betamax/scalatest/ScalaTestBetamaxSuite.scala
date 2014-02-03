package co.freeside.betamax.scalatest

import org.scalatest.FunSuite
import scala.io.Source
import java.net.URL
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import co.freeside.betamax.ProxyConfiguration

@RunWith(classOf[JUnitRunner])
class ScalaTestBetamaxSuite extends FunSuite with Betamax {
  
  implicit val config = ProxyConfiguration.builder().build()

  test("plain scala using read") _ using betamax ("scala support test"){
    assert(Source.fromURL(new URL("http://localhost:6666")).mkString === "scala test")
  }

}