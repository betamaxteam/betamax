package co.freeside.betamax

import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.EchoHandler
import co.freeside.betamax.util.server.HelloHandler
import co.freeside.betamax.tape.yaml.YamlTapeLoader
import co.freeside.betamax.tape.yaml.YamlTape
import co.freeside.betamax.tape.Tape
import co.freeside.betamax.tape.RecordedInteraction
import groovyx.net.http.*
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.TapeMode.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA
import java.io.File

@Stepwise
class ReconcileModeSpec extends Specification {
        @Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
        @Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)

	@AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

	@Shared RESTClient http = new BetamaxRESTClient()

	@Betamax(tape = 'reconcilemode', mode = WRITE_ONLY)
	void 'proxy makes a real HTTP request the first time it gets a request for a URI'() {
                given:
                endpoint.start(HelloHandler)

                when:
		HttpResponseDecorator response = http.get(uri: endpoint.url)

		then:
                response.status == HTTP_OK
                response.getFirstHeader(VIA)?.value == 'Betamax'
		recorder.tape.size() == 1
	}

        @Betamax(tape = 'reconcilemode', mode = RECONCILE)
        void 'Reconcile mode plays response and records no errors when live response matches tape'()  {
                given:
                endpoint.start(HelloHandler)

                when:
		HttpResponseDecorator response = http.get(uri: endpoint.url)

		then:
                notThrown(HttpResponseException)
                response.status == HTTP_OK
                tapeFile(recorder.reconciliationTape).exists() == false
        }

        @Betamax(tape = 'reconcilemode', mode = RECONCILE)
        void 'Reconcile mode records reconciliation error tape when live response doesn\'t match taped response for the matching request'() {
               given:
               endpoint.start(EchoHandler) // Gives different response for same uri

               when:
               http.get(uri: endpoint.url)

               then:
               def e = thrown(HttpResponseException)
               e != null

               Tape reconciliationTape = recorder.reconciliationTape
               recorder.ejectTape() // Have to do this manually to test the file existence + contents
               File file = tapeFile(reconciliationTape)
               file.exists() == true
               YamlTape loadedTape = file.withReader(YamlTapeLoader.FILE_CHARSET) {YamlTape.readFrom(it)}
               loadedTape.size() == 1
               RecordedInteraction unmatched = loadedTape.interactions[0]

               // The unmatched response body was from the echo handler
               unmatched.response.bodyAsText.text =~ /User-Agent/
        }

        @Ignore('Not implemented yet')
        void 'Reconcile mode behaves identically to read-only mode if no matching request found'()   {
          // Spin up svr, make request to /otherpath, which svr responds to, but no
          // recorded request is on tape.  Should get an error.
        }

        File tapeFile(Tape tape) {
          new YamlTapeLoader(recorder.tapeRoot).fileFor(tape.name)
        }
}
