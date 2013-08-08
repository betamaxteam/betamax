package co.freeside.betamax.scalatest

trait Wrapped {
  implicit def wrapPartialFunction(f: (=> Unit) => Unit) = new wrapped(f)
 
  class wrapped(f: (=> Unit) => Unit) {
    def using(f1: => Unit) = f {
      f1
    }
  }
}
