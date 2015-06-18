package co.freeside.betamax.proxy.handler

import co.freeside.betamax.Recorder
import co.freeside.betamax.message.*
import co.freeside.betamax.message.tape.RecordedResponse
import co.freeside.betamax.util.message.*
import co.freeside.betamax.tape.*
import co.freeside.betamax.TapeMode
import spock.lang.Specification
import spock.lang.Ignore
import co.freeside.betamax.tape.ResponseMatcher
import static java.net.HttpURLConnection.HTTP_FORBIDDEN

class TapeReconcilerSpec extends Specification {

	Recorder recorder = Mock(Recorder)
        ChainedHttpHandler nextHandlerForReconciler = Mock(ChainedHttpHandler)
        HeaderFilter filterAndConnector = Mock(HeaderFilter)

        TapeReconciler reconciler = new TapeReconciler(recorder, filterAndConnector)
	Request request = new BasicRequest()
	Response response = new BasicResponse()

       	void 'throws an exception if there is no tape inserted'() {
		given:
                recorder.tape >> null

		when:
		reconciler.handle(request)

		then:
		def e = thrown(ProxyException)
		e.httpStatus == HTTP_FORBIDDEN
        }

        void 'forwards to the next handler if not in reconciliation mode'() {
                given:
                def nextHandler = Mock(HttpHandler)
		nextHandler.handle(_) >> response
		reconciler << nextHandler

                and:
                def tape = Mock(Tape)
                recorder.tape >> tape
                tape.getMode() >> TapeMode.READ_ONLY

                when:
                def result = reconciler.handle(request)

                then:
                result.is(response)
        }

        void 'return response if live response matched taped response'() {
                given:
                filterAndConnector.handle(request) >> response

                and:
                def tape = Mock(Tape)
                recorder.tape >> tape
                tape.mode >> TapeMode.RECONCILE
                tape.seek(request) >> true
                tape.seek(request, response) >> true

                def reconciliationTape = Mock(Tape)
                recorder.reconciliationTape >> reconciliationTape

                when:
                reconciler.handle(request)

                then:
                def result = reconciler.handle(request)

		then:
		result.is(response)

		and:
	        0 * reconciliationTape.record(request, response)
        }

        void 'writes reconciliation error and throws exception if live response didn\'t match taped response'() {
                given:
                filterAndConnector.handle(request) >> response

                def tape = Mock(Tape)
                recorder.tape >> tape
                tape.mode >> TapeMode.RECONCILE
                tape.seek(request) >> true // Found a taped response
                tape.seek(request, response) >> false // But it didn't match

                def reconciliationTape = Mock(Tape)
                recorder.reconciliationTape >> reconciliationTape


                when:
                reconciler.handle(request)

                then:
                1 * reconciliationTape.record(request, response)

                def e = thrown(ReconciliationException)
                e != null

        }

        void 'throws exception if no taped response found'() {
                given:
                filterAndConnector.handle(request) >> response

                def tape = Mock(Tape)
                recorder.tape >> tape
                tape.mode >> TapeMode.RECONCILE
                tape.seek(request) >> false

                def reconciliationTape = Mock(Tape)
                recorder.reconciliationTape >> reconciliationTape

                when:
                reconciler.handle(request)

                then:
                def e = thrown(NoSuchTapedRequestException)
                e != null

                and:
                0 * reconciliationTape.record(request, response)
        }
}
