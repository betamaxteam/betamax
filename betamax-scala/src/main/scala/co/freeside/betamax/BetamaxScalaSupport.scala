package co.freeside.betamax

import co.freeside.betamax.proxy.netty.ProxyServer
import java.util.Comparator
import co.freeside.betamax.message.Request
 
object BetamaxScalaSupport {
  
  val defaultMatchRules = List(MatchRule.method, MatchRule.uri)
  
  def betamax[T](tape: String, mode: TapeMode = null, matchRules: List[Comparator[Request]] = defaultMatchRules)(t: => T) = {
    betamaxImpl[T,T](tape, Option(mode), matchRules)(r => r)(t)
  }
  
  private[betamax] def betamaxImpl[T, R](tape: String, mode: Option[TapeMode], matchRules: List[Comparator[Request]])(resultTransformer: T => R)(t: => T) = {
    synchronized {
      val recorder = new ProxyRecorder
      val proxyServer = new ProxyServer(recorder)
      recorder.insertTape(tape)
      recorder.getTape.setMode(mode.getOrElse(recorder.getDefaultMode()))
      recorder.getTape.setMatchRules(matchRules.toArray: _*)
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