package co.freeside.betamax.tape

import co.freeside.betamax.tape.ResponseMatcher
import co.freeside.betamax.util.message.*
import co.freeside.betamax.message.tape.RecordedResponse
import co.freeside.betamax.message.*
import java.nio.charset.Charset
import spock.lang.Specification
import spock.lang.Ignore

class ResponseMatcherSpec extends Specification {
  ResponseMatcher matcher(Response response1) {
    new ResponseMatcher(response1)
  }

  def response(String body, int status) {
    def resp = new BasicResponse()
    resp.body = body.getBytes(Charset.forName("UTF-8"));
    resp.status = status
    resp
  }

  def recordedResponse(String body, int status) {
    def resp = new RecordedResponse()
    resp.body = body.getBytes(Charset.forName("UTF-8"));
    resp.status = status
    resp
  }

  void 'matches two identical responses'() {
    given:
    def response1 = response("abc", 200)
    def response2 = recordedResponse("abc", 200)

    when:
    def result = matcher(response1).matches(response2)

    then:
    result == true
  }

  void 'does not match responses whose status differ'(){
    given:
    def response1 = response("abc", 200)
    def response2 = recordedResponse("abc", 404)

    when:
    def result = matcher(response1).matches(response2)

    then:
    result == false
  }

  @Ignore
  void 'does not match responses whose bodies differ'(){
    given:
    def response1 = response("abc", 200)
    def response2 = recordedRsponse("def", 200)

    when:
    def result = matcher(response1).matches(response2)

    then:
    result == false
  }

  // Can look at status, content type, headers, body, content type, charset, encoding
}
