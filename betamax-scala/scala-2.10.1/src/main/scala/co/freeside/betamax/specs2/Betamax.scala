package co.freeside.betamax.specs2

import java.util.Comparator
import org.specs2.execute.AsResult
import org.specs2.execute.Result
import org.specs2.specification.Around
import co.freeside.betamax.BetamaxScalaSupport
import co.freeside.betamax.TapeMode
import co.freeside.betamax.message.Request
import co.freeside.betamax.MatchRule
import com.google.common.base.Optional
import co.freeside.betamax.Configuration

trait Betamax {
  
  def configuration: Configuration
  
  def betamax(tape: String, mode: TapeMode = null, matchRule: MatchRule = null) = {
    new BetamaxAround(tape, mode, matchRule)(configuration)
  }
  
  class BetamaxAround(tape: String, mode: TapeMode = null, matchRule: MatchRule = null)(implicit config: Configuration) extends Around {
    def around[T: AsResult](testFun: => T) = 
      BetamaxScalaSupport.betamaxImpl[T,Result](tape, Optional.fromNullable(mode), Optional.fromNullable(matchRule))(t => AsResult(t))(testFun)
  }
}
 