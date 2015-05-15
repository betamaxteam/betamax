package co.freeside.betamax.proxy.handler

import java.util.logging.Logger
import co.freeside.betamax.Recorder
import co.freeside.betamax.TapeMode
import co.freeside.betamax.message.*
import co.freeside.betamax.tape.Tape
import co.freeside.betamax.tape.ReconciliationException
import co.freeside.betamax.tape.NoSuchTapedRequestException
import co.freeside.betamax.tape.responsematcher.ResponseMatcher
import static co.freeside.betamax.proxy.jetty.BetamaxProxy.X_BETAMAX
import static java.net.HttpURLConnection.HTTP_FORBIDDEN
import static java.util.logging.Level.INFO

class TapeReconciler extends ChainedHttpHandler {
	private static final Logger log = Logger.getLogger(TapeReconciler.name)

        private final Recorder recorder
        private final TargetConnector connector
        private final ResponseMatcher responseMatcher

	TapeReconciler(Recorder recorder, TargetConnector connector, ResponseMatcher responseMatcher) {
		this.recorder = recorder
                this.connector = connector
                this.responseMatcher = responseMatcher
	}

	Response handle(Request request) {
		Tape tape = recorder.tape
		if (!tape) {
			throw new ProxyException(HTTP_FORBIDDEN, 'No tape')
		} else if (tape.mode == TapeMode.RECONCILE) {
                  def actualResponse = connector.handle(request)
                  def tapedResponse = findTapedResponse(tape, request)

                  if (!tapedResponse) {
                    throw new NoSuchTapedRequestException()
                  }

                  if(!responseMatcher.match(actualResponse, tapedResponse)) {
                    tape.recordReconciliationError(request, actualResponse)
                    throw new ReconciliationException()
                  }

                  actualResponse
                } else {
                  chain(request)
                }
        }

        private Response findTapedResponse(Tape tape, Request request) {
          if(tape.seek(request)) tape.play(request) else null
        }
}
