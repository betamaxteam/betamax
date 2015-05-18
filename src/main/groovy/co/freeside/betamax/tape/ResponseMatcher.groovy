package co.freeside.betamax.tape

import co.freeside.betamax.message.Response
import co.freeside.betamax.message.tape.RecordedResponse

import static co.freeside.betamax.ResponseMatchRule.status

class ResponseMatcher {
  private final Comparator<Response>[] rules
  private final Response response

  ResponseMatcher(Response response) {
    this(response, [status] as Comparator<Response>[])
  }

  ResponseMatcher(Response response, Comparator<Response>... rules) {
    this.response = response
    this.rules = rules
  }

  boolean matches(RecordedResponse recordedResponse) {
    rules.every { it.compare(response, recordedResponse) == 0 }
  }
}
