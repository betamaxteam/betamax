package co.freeside.betamax.proxy.handler

import java.util.logging.Logger
import co.freeside.betamax.Recorder
import co.freeside.betamax.TapeMode
import co.freeside.betamax.message.*
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
		def tape = recorder.tape
		if (!tape) {
			throw new ProxyException(HTTP_FORBIDDEN, 'No tape')
		} else if (tape.mode == TapeMode.RECONCILE) {
                  def response = connector.handle(request)
                  response
                } else {
                  chain(request)
                }

        }
}
