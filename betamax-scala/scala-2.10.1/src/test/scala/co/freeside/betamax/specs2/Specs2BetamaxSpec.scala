package co.freeside.betamax.specs2

import org.specs2.SpecificationWithJUnit
import scala.io.Source
import java.net.URL
import co.freeside.betamax.ProxyConfiguration

class Specs2BetamaxSpec extends SpecificationWithJUnit with Betamax {
  
  val configuration = ProxyConfiguration.builder().build()
  
  def is = s2"""
    This is a specification to check betamax is working

    When using betamax in specs2, a request to the internet should
      be proxied and returned                                            ${betamax("scala support test") {e1}}
    """
                                                                 
  def e1 = Source.fromURL(new URL("http://localhost:6666")).mkString must be_=== ("scala test")
                                                                                
}
