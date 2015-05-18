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
        }

	int compare(Response a, Response b) {
		throw new UnsupportedOperationException()
	}
}
