package co.freeside.betamax.proxy.handler

import java.util.logging.Logger
import co.freeside.betamax.Recorder
import co.freeside.betamax.TapeMode
import co.freeside.betamax.message.*
import co.freeside.betamax.tape.Tape
import co.freeside.betamax.tape.ReconciliationException
import co.freeside.betamax.tape.NoSuchTapedRequestException
import co.freeside.betamax.tape.ResponseMatcher
import static co.freeside.betamax.proxy.jetty.BetamaxProxy.X_BETAMAX
import static java.net.HttpURLConnection.HTTP_FORBIDDEN
import static java.util.logging.Level.INFO

class TapeReconciler extends ChainedHttpHandler {
	private static final Logger log = Logger.getLogger(TapeReconciler.name)

        private final Recorder recorder
        private final TargetConnector connector

	TapeReconciler(Recorder recorder, TargetConnector connector) {
		this.recorder = recorder
                this.connector = connector
       	}

	Response handle(Request request) {
		Tape tape = recorder.tape
		if (!tape) {
			throw new ProxyException(HTTP_FORBIDDEN, 'No tape')
		} else if (tape.mode == TapeMode.RECONCILE) {
                  def actualResponse = connector.handle(request)

                  if (!tape.seek(request)) {
                    throw new NoSuchTapedRequestException()
                  }

                  /*

                     Because we have changed the ResponseMatcher to mirror the RequestMatcher,
                     this means that we can't inject it into the TapeReconciler...that's not
                     how RequestMatcher works.  It is used by the tape.  See how tape.seek(request)
                     works.  We need to implement a tape.seek(request, response) which finds a
                     matching request and then checks that its response matches the one passed in.

                     Once we have that, we can tell the mock tape that .seek(request, response) >> false
                     in TapeReconcilerSpec.

                     After that, we'll change tape.recordReconciliationError.  Since we've said we
                     want to model the reconciliation errors as a tape, we have the question of where
                     this tape lives.  I was thinking the tape itself, e.g. tape.reconciliationErrorTape,
                     but now I'm wondering if it should go on the recorder, e.g.

                       recorder.tape
                       recorder.reconciliationErrorTape

                     Nice symmetry there, can just add the writing of the reconciliationErroTape to
                     the recorder's ejectTape method, e.g.

                       void ejectTape() {
                         [tape, reconciliationErrorTape].each {
	      	           it ? tapeLoader.writeTape(it)
                         }
                         tape = null
                         reconciliationErrorTape = null
	               }

                     This way, we don't have to add anything special to YamlTape, YamlTapeLoader, etc.
                     It's just an extra tape!  The Recorder is the main thing that changes, so it's tests
                     need to cover this.

                   */

                  if (!tape.seek(request, actualResponse)) {
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
