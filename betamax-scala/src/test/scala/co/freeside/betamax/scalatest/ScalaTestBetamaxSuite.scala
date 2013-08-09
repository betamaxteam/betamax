package co.freeside.betamax.scalatest

import org.scalatest.FunSuite
import scala.io.Source
import java.net.URL
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ScalaTestBetamaxSuite extends FunSuite with Betamax {
  
  test("plain scala using read") _ using betamax ("scala support test"){
    assert(Source.fromURL(new URL("http://localhost:5555")).mkString === "scala test")
  }

}