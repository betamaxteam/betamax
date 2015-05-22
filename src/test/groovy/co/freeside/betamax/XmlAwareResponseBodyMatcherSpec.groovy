package co.freeside.betamax

import co.freeside.betamax.util.message.*
import co.freeside.betamax.message.*
import java.nio.charset.Charset
import spock.lang.Specification
import spock.lang.Ignore

class XmlAwareResponseBodyMatcherSpec extends Specification {

  def response(String body = null) {
    def resp = new BasicResponse()
    resp.body = body?.getBytes(Charset.forName("UTF-8"));
    resp.status = 200
    resp
  }

  void 'can ignore diffs in specific XML elements in the body'(){
    given:
    def response1 = response("<a>1</a><b>2</b><c>3</c>")
    def response2 = response("<a>9</a><b>4</b><c>3</c>")

    when:
    def rule = new XmlAwareResponseBodyMatcher("a", "b")
    def result = rule.compare(response1, response2)

    then:
    result == 0
  }

  void 'does not ignore diffs in other elements in the body'(){
    given:
    def response1 = response("<a>1</a><b>2</b><c>3</c>")
    def response2 = response("<a>9</a><b>4</b><c>9</c>")

    when:
    def rule = new XmlAwareResponseBodyMatcher("a", "b")
    def result = rule.compare(response1, response2)

    then:
    result != 0
  }

  void 'works when no elements specified'(){
    given:
    def response1 = response("<a>1</a><b>2</b>")
    def response2 = response("<a>1</a><b>2</b>")

    when:
    def rule = new XmlAwareResponseBodyMatcher()
    def result = rule.compare(response1, response2)

    then:
    result == 0
  }
}
