package co.freeside.betamax.scalatest

import co.freeside.betamax.BetamaxScalaSupport
import co.freeside.betamax.MatchRule
import co.freeside.betamax.TapeMode
import co.freeside.betamax.Configuration

trait Betamax extends Wrapped {
 
  def betamax(tape: String, mode: TapeMode = null, matchRules: MatchRule = null)(testFun: => Unit)(implicit config: Configuration) = 
    BetamaxScalaSupport.betamax(tape, mode, matchRules)(testFun)
 
}