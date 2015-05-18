package co.freeside.betamax

import co.freeside.betamax.message.Response

/**
 * Implements a request matching rule for matching responses on a tape.
 */
enum ResponseMatchRule implements Comparator<Response> {
        status {
               @Override
               int compare(Response a, Response b) {
                 a.status <=> b.status
               }
        },
        body {
               @Override
               int compare(Response a, Response b) {
                 (a.hasBody() ? a.bodyAsText.text : null) <=>
                 (b.hasBody() ? b.bodyAsText.text : null)
               }
        }

	int compare(Response a, Response b) {
		throw new UnsupportedOperationException()
	}
}
