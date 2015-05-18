package co.freeside.betamax.proxy.handler

import co.freeside.betamax.Recorder
import co.freeside.betamax.message.*
import co.freeside.betamax.util.message.*
import co.freeside.betamax.tape.*
import co.freeside.betamax.TapeMode
import spock.lang.Specification
import spock.lang.Ignore
import co.freeside.betamax.tape.ResponseMatcher
import static java.net.HttpURLConnection.HTTP_FORBIDDEN

class TapeReconcilerSpec extends Specification {

	Recorder recorder = Mock(Recorder)
        TargetConnector connector = Mock(TargetConnector)
        ResponseMatcher responseMatcher = Mock(ResponseMatcher)
	TapeReconciler handler = new TapeReconciler(recorder, connector, responseMatcher)
	Request request = new BasicRequest()
	Response response = new BasicResponse()

	void 'throws an exception if there is no tape inserted'() {
		given:
                recorder.tape >> null

		when:
		handler.handle(request)

		then:
		def e = thrown(ProxyException)
		e.httpStatus == HTTP_FORBIDDEN
        }

        void 'forwards to the next handler if not in reconciliation mode'() {
                given:
                def nextHandler = Mock(HttpHandler)
		nextHandler.handle(_) >> response
		handler << nextHandler

                and:
                def tape = Mock(Tape)
                recorder.tape >> tape
                tape.getMode() >> TapeMode.READ_ONLY

                when:
                def result = handler.handle(request)

                then:
                result.is(response)
        }

        void 'return response if live response matched taped response'() {
                given:
                connector.handle(request) >> response

                and:
                def tape = Mock(Tape)
                recorder.tape >> tape
                tape.mode >> TapeMode.RECONCILE
                tape.seek(request) >> true

                def tapedResponse = new BasicResponse()
                tape.play(request) >> tapedResponse
                responseMatcher.match(response, tapedResponse) >> true

                when:
                handler.handle(request)

                then:
                def result = handler.handle(request)

		then:
		result.is(response)

		and:
		0 * tape.record(request, response)
                0 * tape.recordReconciliationError(request, response)
        }

        void 'writes reconciliation error and throws exception if live response didn\'t match taped response'() {
                given:
                connector.handle(request) >> response

                def tape = Mock(Tape)
                recorder.tape >> tape
                tape.mode >> TapeMode.RECONCILE
                tape.seek(request) >> true

                def tapedResponse = new BasicResponse()
                tape.play(request) >> tapedResponse

                responseMatcher.match(response, tapedResponse) >> false

                when:
                handler.handle(request)

                then:
                def e = thrown(ReconciliationException)
                e != null

                and:
                0 * tape.record(request, response)
                1 * tape.recordReconciliationError(request, response)
        }

        void 'throws exception if no taped response found'() {
                given:
                connector.handle(request) >> response

                def tape = Mock(Tape)
                recorder.tape >> tape
                tape.mode >> TapeMode.RECONCILE
                tape.seek(request) >> false

                when:
                handler.handle(request)

                then:
                def e = thrown(NoSuchTapedRequestException)
                e != null

                and:
                0 * tape.record(request, response)
                0 * tape.recordReconciliationError(request, response)
        }
}
