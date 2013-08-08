package co.freeside.betamax.specs2

import org.specs2.execute.Result
import org.specs2.execute.AsResult
import org.specs2.mutable.Around
import co.freeside.betamax.BetamaxScalaSupport
import co.freeside.betamax.ProxyRecorder
import co.freeside.betamax.TapeMode
import co.freeside.betamax.proxy.netty.ProxyServer
import java.util.Comparator
import co.freeside.betamax.message.Request
 
class Betamax(tape: String, mode: Option[TapeMode] = None, matchRules: Option[List[Comparator[Request]]] = None) extends Around {
  def around[T <% Result](t: => T) = BetamaxScalaSupport.betamax(tape, mode, matchRules)(t)
}
 
object Betamax {
  // syntactic sugar does away with 'new' in tests
  def apply(tape: String, mode: Option[TapeMode] = None) = new Betamax(tape, mode)
}