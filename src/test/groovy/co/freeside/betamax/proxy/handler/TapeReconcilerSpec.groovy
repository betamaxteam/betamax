package co.freeside.betamax.proxy.handler

import co.freeside.betamax.Recorder
import co.freeside.betamax.message.Request
import co.freeside.betamax.message.Response
import co.freeside.betamax.tape.Tape
import co.freeside.betamax.TapeMode
import co.freeside.betamax.util.message.BasicRequest
import co.freeside.betamax.util.message.BasicResponse
import spock.lang.Specification
import spock.lang.Ignore

import static java.net.HttpURLConnection.HTTP_FORBIDDEN

class TapeReconcilerSpec extends Specification {

	Recorder recorder = Mock(Recorder)
        TargetConnector connector = Mock(TargetConnector)

	TapeReconciler handler = new TapeReconciler(recorder, connector)
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

        @Ignore("Not implemented yet")
        void 'return response if live response matched taped response'() {
                given:
                def tape = Mock(Tape)
                recorder.tape >> tape
                tape.getMode() >> TapeMode.RECONCILE
                tape.seek(request) >> true
                tape.play(request) >> response // for reconciliation purposes

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

        @Ignore("Not implemented yet")
        void 'writes reconciliation error and throws exception if live response didn\'t match taped response'() {
          given:
          true

          when:
          true

          then:
          true
        }
}
