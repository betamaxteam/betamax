package co.freeside.betamax

import co.freeside.betamax.proxy.netty.ProxyServer
import java.util.Comparator
import co.freeside.betamax.message.Request
 
object BetamaxScalaSupport {
  def betamax[T](tape: String, mode: Option[TapeMode], matchRules: Option[List[Comparator[Request]]] = None)(t: => T) = {
    betamaxImpl[T,T](tape, mode, matchRules)(r => r)(t)
  }

  
  private[betamax] def betamaxImpl[T, R](tape: String, mode: Option[TapeMode], matchRules: Option[List[Comparator[Request]]] = None)(resultTransformer: T => R)(t: => T) = {
    synchronized {
      val recorder = new ProxyRecorder
      val proxyServer = new ProxyServer(recorder)
      recorder.insertTape(tape)
      recorder.getTape.setMode(mode.getOrElse(recorder.getDefaultMode()))
      val rules = matchRules.getOrElse(List(MatchRule.method, MatchRule.uri)).toArray
      recorder.getTape.setMatchRules(rules: _*)
      proxyServer.start()
      try {
        resultTransformer(t)
      } finally {
        recorder.ejectTape()
        proxyServer.stop()
      }
    }
  }
}