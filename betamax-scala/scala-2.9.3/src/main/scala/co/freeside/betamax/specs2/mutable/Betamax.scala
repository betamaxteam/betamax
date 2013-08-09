package co.freeside.betamax.specs2.mutable

import org.specs2.execute.Result
import org.specs2.execute.AsResult
import org.specs2.mutable.Around
import co.freeside.betamax.BetamaxScalaSupport
import co.freeside.betamax.ProxyRecorder
import co.freeside.betamax.TapeMode
import co.freeside.betamax.proxy.netty.ProxyServer
import java.util.Comparator
import co.freeside.betamax.message.Request
 
class Betamax(tape: String, mode: TapeMode = null, matchRules: List[Comparator[Request]] = BetamaxScalaSupport.defaultMatchRules) extends Around {
  def around[T <% Result](t: => T) = BetamaxScalaSupport.betamax(tape, mode, matchRules)(t)
}
 
object Betamax {
  // syntactic sugar does away with 'new' in tests
  def apply(tape: String, mode: TapeMode = null, matchRules: List[Comparator[Request]] = BetamaxScalaSupport.defaultMatchRules) = 
    new Betamax(tape, mode, matchRules)
}