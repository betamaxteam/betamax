package co.freeside.betamax.proxy.handler

import java.util.logging.Logger
import co.freeside.betamax.Recorder
import co.freeside.betamax.message.*
import static co.freeside.betamax.proxy.jetty.BetamaxProxy.X_BETAMAX
import static java.net.HttpURLConnection.HTTP_FORBIDDEN
import static java.util.logging.Level.INFO
/**
 * Reads the tape to find a matching exchange, returning the response if found otherwise proceeding the request, storing
 * & returning the new response.
 */
class TapeReader extends ChainedHttpHandler {

	private final Recorder recorder

	private static final Logger log = Logger.getLogger(TapeReader.name)

	TapeReader(Recorder recorder) {
		this.recorder = recorder
	}

	Response handle(Request request) {
		def tape = recorder.tape
		if (!tape) {
			throw new ProxyException(HTTP_FORBIDDEN, 'No tape')
		} else if (tape.readable && (tape.sequential || tape.seek(request))) {
			log.log INFO, "Playing back from '$tape.name'"
			def response = tape.play(request)
			response.addHeader(X_BETAMAX, 'PLAY')
			response
		} else if (tape.writable) {
			chain(request)
		} else {
			throw new ProxyException(HTTP_FORBIDDEN, 'Tape is read-only')
		}
	}

}
