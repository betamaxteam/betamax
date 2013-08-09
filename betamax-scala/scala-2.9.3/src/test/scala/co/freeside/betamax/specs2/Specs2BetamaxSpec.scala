package co.freeside.betamax.specs2

import org.specs2.SpecificationWithJUnit
import scala.io.Source
import java.net.URL

class Specs2BetamaxSpec extends SpecificationWithJUnit {
  
  def is =

    "This is a specification to check betamax is working"                  ^
                                                                           p^
    "When using betamax in specs2, a request to the internet should"       ^
    "  be proxied and returned"                                            ! Betamax("scala support test") {e1} ^
                                                                           end
                                                                 
  def e1 = Source.fromURL(new URL("http://localhost:5555")).mkString must be_=== ("scala test")
                                                                                
}