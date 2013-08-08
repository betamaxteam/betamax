package co.freeside.betamax.specs2

import java.util.Comparator

import org.specs2.execute.AsResult
import org.specs2.execute.Result
import org.specs2.mutable.Around

import co.freeside.betamax.BetamaxScalaSupport
import co.freeside.betamax.ProxyRecorder
import co.freeside.betamax.TapeMode
import co.freeside.betamax.message.Request
import co.freeside.betamax.proxy.netty.ProxyServer
 
class Betamax(tape: String, mode: Option[TapeMode] = None, matchRules: Option[List[Comparator[Request]]] = None) extends Around {
  def around[T: AsResult](testFun: => T) = BetamaxScalaSupport.betamaxImpl[T,Result](tape, mode, matchRules)(t => AsResult(t))(testFun)
}
 
object Betamax {
  // syntactic sugar does away with 'new' in tests
  def apply(tape: String, mode: Option[TapeMode] = None, matchRules: Option[List[Comparator[Request]]] = None) = new Betamax(tape, mode, matchRules)
}
