package co.freeside.betamax.scalatest

import co.freeside.betamax.TapeMode
import co.freeside.betamax.proxy.netty.ProxyServer
import co.freeside.betamax.ProxyRecorder
import java.util.Comparator
import co.freeside.betamax.message.Request
import co.freeside.betamax.MatchRule
import co.freeside.betamax.BetamaxScalaSupport

trait Betamax extends Wrapped {
 
  def betamax(tape: String, mode: TapeMode = null, matchRules: List[Comparator[Request]] = BetamaxScalaSupport.defaultMatchRules)(testFun: => Unit) = 
    BetamaxScalaSupport.betamax(tape, mode, matchRules)(testFun)
 
}