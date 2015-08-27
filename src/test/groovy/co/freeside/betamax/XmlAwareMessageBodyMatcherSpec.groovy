package co.freeside.betamax

import co.freeside.betamax.message.Message
import spock.lang.Specification

class XmlAwareMessageBodyMatcherSpec extends Specification {
    def message(String body) {
        def msg = Mock(Message)
        if(body != null) {
            msg.hasBody() >> true
            msg.getBodyAsText() >> new StringReader(body)
        } else {
            msg.hasBody() >> false
            msg.getBodyAsText() >> null
        }
        msg
    }

    void 'can ignore diffs in specific XML elements in the body'() {
        given:
        def message1 = message("<a>1</a><b>2</b><c>3</c>")
        def message2 = message("<a>9</a><b>4</b><c>3</c>")

        when:
        def rule = new XmlAwareMessageBodyMatcher("a", "b")
        def result = rule.compare(message1, message2)

        then:
        result == 0
    }

    void 'does not ignore diffs in other elements in the body'() {
        given:
        def message1 = message("<a>1</a><b>2</b><c>3</c>")
        def message2 = message("<a>9</a><b>4</b><c>9</c>")

        when:
        def rule = new XmlAwareMessageBodyMatcher("a", "b")
        def result = rule.compare(message1, message2)

        then:
        result != 0
    }

    void 'works when no elements specified'() {
        given:
        def message1 = message("<a>1</a><b>2</b>")
        def message2 = message("<a>1</a><b>2</b>")

        when:
        def rule = new XmlAwareMessageBodyMatcher()
        def result = rule.compare(message1, message2)

        then:
        result == 0
    }

    void 'works when no body present in either message'() {
        given:
        def message1 = message(null)
        def message2 = message(null)

        when:
        def rule = new XmlAwareMessageBodyMatcher()
        def result = rule.compare(message1, message2)

        then:
        result == 0

    }

    void 'works when no body present in one message'() {
        given:
        def message1 = message(null)
        def message2 = message("<a/>")

        when:
        def rule = new XmlAwareMessageBodyMatcher()
        def result = rule.compare(message1, message2)

        then:
        result != 0
    }
}
