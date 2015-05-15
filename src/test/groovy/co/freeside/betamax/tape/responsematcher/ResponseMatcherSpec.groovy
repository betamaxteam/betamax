package co.freeside.betamax.proxy.handler

import co.freeside.betamax.tape.responsematcher.ResponseMatcher
import co.freeside.betamax.util.message.*
import co.freeside.betamax.message.*
import spock.lang.Specification
import spock.lang.Ignore

class ResponseMatcherSpec extends Specification {
  ResponseMatcher matcher = new ResponseMatcher()
  Response response1 = new BasicResponse()
  Response response2 = new BasicResponse()

  void 'matches two identical responses'() {
    given:

    when:
    def result = matcher.match(response1, response2)

    then:
    result == true
  }

  // Can look at status, content type, headers, body, content type, charset, encoding

  @Ignore
  void 'matches responses that differ only by [...]'() {
    given:
    true

    when:
    true

    then:
    true
  }
}
