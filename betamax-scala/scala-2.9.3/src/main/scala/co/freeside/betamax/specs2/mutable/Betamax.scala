package co.freeside.betamax.specs2.mutable

import org.specs2.execute.Result
import org.specs2.execute.AsResult
import org.specs2.mutable.Around
import co.freeside.betamax.BetamaxScalaSupport
import co.freeside.betamax.TapeMode
import com.google.common.base.Optional
import co.freeside.betamax.MatchRule
import co.freeside.betamax.Configuration
import co.freeside.betamax.message.Request
 
trait Betamax {
  
  def configuration: Configuration
  
  def betamax(tape: String, mode: TapeMode = null, matchRule: MatchRule = null) = {
    new BetamaxAround(tape, mode, matchRule)(configuration)
  }
  
  class BetamaxAround(tape: String, mode: TapeMode = null, matchRule: MatchRule = null)(implicit config: Configuration) extends Around {
    def around[T <% Result](t: => T) = BetamaxScalaSupport.betamax(tape, mode, matchRule)(t)
  }
 
}
