package co.freeside.betamax.tape
import co.freeside.betamax.encoding.DeflateEncoder
import co.freeside.betamax.encoding.GzipEncoder
import co.freeside.betamax.message.Request
import co.freeside.betamax.message.Response
import co.freeside.betamax.tape.yaml.YamlTapeLoader
import co.freeside.betamax.util.message.BasicRequest
import co.freeside.betamax.util.message.BasicResponse
import com.google.common.io.Files
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static co.freeside.betamax.TapeMode.READ_WRITE
import static com.google.common.net.HttpHeaders.*

@Unroll
class TapeWithZipDataSpec extends Specification {
	@Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
	@Shared def loader = new YamlTapeLoader(tapeRoot)
	@Shared Tape tape = loader.loadTape('tape_spec')

	Request getRequest = new BasicRequest('GET', 'http://qwantz.com/')
    Response encodedGZipResponse;
    static final PLAIN_TEXT_BODY = 'O HAI!'

	void setup() {
        encodedGZipResponse = new BasicResponse(status: 200, reason: 'OK')
		encodedGZipResponse.addHeader(CONTENT_TYPE, 'text/plain;charset=UTF-8')
		encodedGZipResponse.addHeader(CONTENT_LANGUAGE, 'en-GB')
	}

	void setupSpec() {
		tape.mode = READ_WRITE
	}

	void 'encoded text body with #encoding encoding is correctly stored on tape'() {
		when: 'the response body is encoded'
        encodedGZipResponse.addHeader(CONTENT_ENCODING, encoding)
		encodedGZipResponse.body = encoder.encode(PLAIN_TEXT_BODY.bytes)

		and: 'the HTTP interaction is recorded to tape'
		tape.record(getRequest, encodedGZipResponse)

		then: 'an interaction has occurred'
		def interaction = tape.interactions[-1]

		and: 'the request data is correctly stored'
		interaction.request.method == getRequest.method
		interaction.request.uri == getRequest.uri

		and: 'the response status and headers are correctly stored on tape'
		interaction.response.status == encodedGZipResponse.status
		interaction.response.headers[CONTENT_TYPE] == encodedGZipResponse.getHeader(CONTENT_TYPE)
		interaction.response.headers[CONTENT_LANGUAGE] == encodedGZipResponse.getHeader(CONTENT_LANGUAGE)
		interaction.response.headers[CONTENT_ENCODING] == encodedGZipResponse.getHeader(CONTENT_ENCODING)

		and: 'the response body is correctly stored as decoded on tape'
		interaction.response.body == PLAIN_TEXT_BODY

		where:
        encoderClass << [GzipEncoder, DeflateEncoder]
        encoding << ["gzip", "deflate"]
        encoder = encoderClass.newInstance()
	}

    void 'plain text body with #encoding encoding is correctly stored on tape, using fallback'() {
        when: 'the response body is encoded'
        encodedGZipResponse.addHeader(CONTENT_ENCODING, encoding)
        encodedGZipResponse.body = PLAIN_TEXT_BODY

        and: 'the HTTP interaction is recorded to tape'
        tape.record(getRequest, encodedGZipResponse)

        then: 'an interaction has occurred'
        def interaction = tape.interactions[-1]

        and: 'the request data is correctly stored'
        interaction.request.method == getRequest.method
        interaction.request.uri == getRequest.uri

        and: 'the response status and headers are correctly stored on tape'
        interaction.response.status == encodedGZipResponse.status
        interaction.response.headers[CONTENT_TYPE] == encodedGZipResponse.getHeader(CONTENT_TYPE)
        interaction.response.headers[CONTENT_LANGUAGE] == encodedGZipResponse.getHeader(CONTENT_LANGUAGE)
        interaction.response.headers[CONTENT_ENCODING] == encodedGZipResponse.getHeader(CONTENT_ENCODING)

        and: 'the response body is correctly stored as decoded on tape'
        interaction.response.body == PLAIN_TEXT_BODY

        where:
        encoding << ["gzip", "deflate"]
    }
}
