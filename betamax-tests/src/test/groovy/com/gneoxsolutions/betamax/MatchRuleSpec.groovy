/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gneoxsolutions.betamax

import com.gneoxsolutions.betamax.message.tape.RecordedRequest
import com.gneoxsolutions.betamax.util.message.BasicRequest
import org.apache.http.auth.AUTH
import spock.lang.Issue
import spock.lang.Specification

import static com.gneoxsolutions.betamax.MatchRules.*
import static com.google.common.net.HttpHeaders.ACCEPT_ENCODING
import static com.google.common.net.HttpHeaders.AUTHORIZATION
import static com.google.common.net.HttpHeaders.CACHE_CONTROL

@Issue('https://github.com/robfletcher/betamax/issues/9')
class MatchRuleSpec extends Specification {

    void 'can match method and url'() {
        given:
        def request1 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI())
        def request2 = new RecordedRequest(method: 'HEAD', uri: 'http://freeside.co/betamax'.toURI())
        def request3 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/betamax?q=1'.toURI())

        and:
        def request = new BasicRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI())
        def rule = ComposedMatchRule.of(method, uri)

        expect:
        rule.isMatch(request, request1)
        !rule.isMatch(request, request2)
        !rule.isMatch(request, request3)
    }

    void 'can match host'() {
        given:
        def request1 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI())
        def request2 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/grails-fields'.toURI())
        def request3 = new RecordedRequest(method: 'HEAD', uri: 'http://freeside.co/betamax'.toURI())
        def request4 = new RecordedRequest(method: 'GET', uri: 'http://icanhascheezburger.com/'.toURI())

        and:
        def request = new BasicRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI())
        def rule = host

        expect:
        rule.isMatch(request, request1)
        rule.isMatch(request, request2)
        rule.isMatch(request, request3)
        !rule.isMatch(request, request4)
    }

    void 'can match path'() {
        given:
        def request1 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI())
        def request2 = new RecordedRequest(method: 'GET', uri: 'http://robfletcher.github.com/grails-enhanced-scaffolding'.toURI())
        def request3 = new RecordedRequest(method: 'HEAD', uri: 'http://freeside.co/betamax'.toURI())
        def request4 = new RecordedRequest(method: 'GET', uri: 'http://icanhascheezburger.com/betamax'.toURI())

        and:
        def request = new BasicRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI())
        def rule = path

        expect:
        rule.isMatch(request, request1)
        !rule.isMatch(request, request2)
        rule.isMatch(request, request3)
        rule.isMatch(request, request4)
    }

    void 'can match authorization'() {
        given:
        def request1 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI(),     headers: [(AUTHORIZATION): 'Basic 1234'])
        def request2 = new RecordedRequest(method: 'GET', uri: 'http://icanhascheezburger.com/'.toURI(), headers: [(AUTHORIZATION): 'Basic 1234', (CACHE_CONTROL): 'no-cache'])
        def request3 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI(),     headers: [(AUTHORIZATION): 'Basic 4321'])
        def request4 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI(),     headers: [(AUTHORIZATION): 'Basic 4321', (CACHE_CONTROL): 'no-cache'])

        and:
        def request = new BasicRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI(), headers: [(AUTHORIZATION): ['Basic 1234']])
        def rule = authorization

        expect:
        rule.isMatch(request, request1)
        rule.isMatch(request, request2)
        !rule.isMatch(request, request3)
        !rule.isMatch(request, request4)
    }

    void 'can match post body'() {
        given:
        def request1 = new RecordedRequest(method: 'POST', uri: 'http://freeside.co/betamax'.toURI(), body: 'q=1')
        def request2 = new RecordedRequest(method: 'POST', uri: 'http://freeside.co/betamax'.toURI(), body: 'q=2')
        def request3 = new RecordedRequest(method: 'POST', uri: 'http://freeside.co/betamax'.toURI(), body: 'q=1&r=1')

        and:
        def request = new BasicRequest(method: 'POST', uri: 'http://freeside.co/betamax'.toURI(), body: 'q=1')
        def rule = body

        expect:
        rule.isMatch(request, request1)
        !rule.isMatch(request, request2)
        !rule.isMatch(request, request3)
    }

}