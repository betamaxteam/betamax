package co.freeside.betamax.specs2.mutable

import scala.io.Source
import java.net.URL
import org.specs2.mutable.SpecificationWithJUnit

class Specs2BetamaxSpecTest extends SpecificationWithJUnit {
  
  "When using betamax in specs2, a request to the internet" should {
    "be proxied and returned" in Betamax("scala support test") {
      Source.fromURL(new URL("http://localhost:5555")).mkString must be_=== ("scala test")
    }
  }
                                                                                
}