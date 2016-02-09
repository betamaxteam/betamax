/*
 * Copyright 2013 the original author or authors.
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

package com.gneoxsolutions.betamax.proxy

import com.gneoxsolutions.betamax.junit.Betamax
import com.gneoxsolutions.betamax.junit.RecorderRule

import javax.net.ssl.HttpsURLConnection
import com.gneoxsolutions.betamax.ProxyConfiguration
import com.gneoxsolutions.betamax.junit.*
import org.junit.Rule
import spock.lang.*
import static com.gneoxsolutions.betamax.Headers.X_BETAMAX
import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK

@Ignore
@Issue("https://github.com/robfletcher/betamax/issues/117")
class DisconnectedHttpsSpec extends Specification {

    static final TAPE_ROOT = new File(DisconnectedHttpsSpec.getResource("/betamax/tapes").toURI())
    def configuration = ProxyConfiguration.builder().tapeRoot(TAPE_ROOT).sslEnabled(true).build()
    @Rule RecorderRule recorder = new RecorderRule(configuration)

    @Betamax(tape = "disconnected https spec")
    void "can play back a recorded HTTPS response without contacting the original server"() {
        when:
        HttpsURLConnection connection = "https://freeside.bv/".toURL().openConnection()

        then:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == "Betamax"
        connection.getHeaderField(X_BETAMAX) == "PLAY"
        connection.inputStream.text == "O HAI!"
    }

}