package co.freeside.betamax

import com.google.common.base.Present
import com.google.common.base.Optional
 
object BetamaxScalaSupport {
  
  def betamax[T](tape: String, mode: TapeMode = null, matchRule: MatchRule = null)(t: => T)(implicit config: Configuration) = {
    betamaxImpl[T,T](tape, Optional.fromNullable(mode), Optional.fromNullable(matchRule))(r => r)(t)
  }
  
  private[betamax] def betamaxImpl[T, R](tape: String, mode: Optional[TapeMode], matchRule: Optional[MatchRule])(resultTransformer: T => R)(t: => T)(implicit config: Configuration) = {
    synchronized {
      val recorder = new Recorder(config)
      recorder.start(tape, mode, matchRule)
      try {
        resultTransformer(t)
      } finally {
        recorder.stop()
      }
    }
  }
}