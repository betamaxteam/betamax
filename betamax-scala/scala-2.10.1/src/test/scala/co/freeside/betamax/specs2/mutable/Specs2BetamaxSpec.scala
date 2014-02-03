package co.freeside.betamax.specs2.mutable

import scala.io.Source
import java.net.URL
import org.specs2.mutable.SpecificationWithJUnit
import co.freeside.betamax.ProxyConfiguration
import co.freeside.betamax.Configuration

class Specs2BetamaxSpecTest extends SpecificationWithJUnit with Betamax {
  
  val configuration: Configuration = ProxyConfiguration.builder().build()

  "When using betamax in specs2, a request to the internet" should {
    "be proxied and returned" in betamax("scala support test") {
      Source.fromURL(new URL("http://localhost:6666")).mkString must be_=== ("scala test")
    }
  }
                                                                                
}