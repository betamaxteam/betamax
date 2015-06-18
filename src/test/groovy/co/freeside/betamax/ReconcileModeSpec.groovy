package co.freeside.betamax

import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.EchoHandler
import co.freeside.betamax.util.server.HelloHandler
import co.freeside.betamax.tape.NoSuchTapedRequestException
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
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import static org.apache.http.HttpHeaders.VIA
import static co.freeside.betamax.MatchRule.*
import java.io.File

@Stepwise
class ReconcileModeSpec extends Specification {
        @Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
        @Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)

	@AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

	@Shared RESTClient http = new BetamaxRESTClient()

	@Betamax(tape = 'reconcilemode', mode = WRITE_ONLY, match = [method, uri, headers])
	void 'proxy makes a real HTTP request the first time it gets a request for a URI'() {
                given:
                endpoint.start(HelloHandler)

                when:
		List<HttpResponseDecorator> responses = ["", "?q=bar"].collect {http.get(uri: endpoint.url + it)}
                responses << http.get(uri: endpoint.url, headers: ["header1" : "value1"])

		then:
                responses.each {
                  it.status == HTTP_OK
                  it.getFirstHeader(VIA)?.value == 'Betamax'
                }

		recorder.tape.size() == responses.size
        }

        @Betamax(tape = 'reconcilemode', mode = RECONCILE, match = [method, uri, headers])
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

        @Betamax(tape = 'reconcilemode', mode = RECONCILE, match = [method, uri, headers])
        void 'Reconcile mode records reconciliation error tape when live response doesn\'t match taped response for the matching request (and uses same match rules as normal tape)'() {
               given:
               endpoint.start(EchoHandler) // Gives different response for same uri

               when:
               http.get(uri: endpoint.url)

               then:
               def e1 = thrown(HttpResponseException)
               e1.statusCode == HTTP_INTERNAL_ERROR

               when:
               http.get(uri: endpoint.url, headers: ["header1" : "value1"])

               then:
               def e2 = thrown(HttpResponseException)
               e2.statusCode == HTTP_INTERNAL_ERROR


               def recErrTape = recordedReconciliationTape()
               recErrTape.size() == 2

               // The unmatched response body was from the echo handler
               recErrTape.interactions.each {it.response.bodyAsText.text =~ /User-Agent/}
        }


        @Betamax(tape = 'reconcilemode', mode = RECONCILE, match = [method, uri, headers])
        void 'Reconcile mode produces error if no matching request found'()   {
          given:
          endpoint.start(HelloHandler)

          when:
          http.get(uri: endpoint.url + "?q=foo")

          then:
          def e = thrown(HttpResponseException)
          e.statusCode == HTTP_INTERNAL_ERROR
        }

        def recordedReconciliationTape() {
          Tape reconciliationTape = recorder.reconciliationTape
          recorder.ejectTape()

          File file = tapeFile(reconciliationTape)
          file.exists() == true
          YamlTape loadedTape = file.withReader(YamlTapeLoader.FILE_CHARSET) {YamlTape.readFrom(it)}
        }

        File tapeFile(Tape tape) {
          new YamlTapeLoader(recorder.tapeRoot).fileFor(tape.name)
        }
}
