package co.freeside.betamax

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfterAll
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.eclipse.jetty.server.Request
import org.scalatest.BeforeAndAfterEach
import co.freeside.betamax.util.server.IncrementingHandler
import scala.io.Source
import java.net.URL
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BetamaxScalaSupportSuite extends FunSuite {
  
  test("plain scala using read") {
    BetamaxScalaSupport.betamax("scala support test") {
      assert(Source.fromURL(new URL("http://localhost:5555")).mkString === "scala test")
    }
  }

}