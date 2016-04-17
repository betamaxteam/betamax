/*
 * Copyright 2016 the original author or authors.
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

package software.betamax.junit;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import software.betamax.ProxyConfiguration;
import software.betamax.TapeMode;

public class StartupProxyOnceTest {

    @Rule
    @ClassRule
    public static RecorderRule recorder = new RecorderRule(ProxyConfiguration.builder()
            .defaultMode(TapeMode.WRITE_SEQUENTIAL)
            .proxyPort(0)
            .build());

    @Test
    @Betamax(tape = "test1")
    public void firstTest() {
        assert recorder.getTape().getName().equals("test1");
        assert recorder.getTape().getMode() == TapeMode.WRITE_SEQUENTIAL;

        assert recorder.getListenPort() > 0;
        assert System.getProperty("http.proxyPort").equals(Integer.toString(recorder.getListenPort()));
        assert System.getProperty("https.proxyPort").equals(Integer.toString(recorder.getListenPort()));
    }

    @Test
    @Betamax(tape = "test2", mode = TapeMode.READ_SEQUENTIAL)
    public void secondTest() {
        assert recorder.getTape().getName().equals("test2");
        assert recorder.getTape().getMode() == TapeMode.READ_SEQUENTIAL;

        assert recorder.getListenPort() > 0;
        assert System.getProperty("http.proxyPort").equals(Integer.toString(recorder.getListenPort()));
        assert System.getProperty("https.proxyPort").equals(Integer.toString(recorder.getListenPort()));
    }

    @Test
    @Betamax(tape = "test3")
    public void thirdTest() {
        assert recorder.getTape().getName().equals("test3");
        assert recorder.getTape().getMode() == TapeMode.WRITE_SEQUENTIAL;

        assert recorder.getListenPort() > 0;
        assert System.getProperty("http.proxyPort").equals(Integer.toString(recorder.getListenPort()));
        assert System.getProperty("https.proxyPort").equals(Integer.toString(recorder.getListenPort()));
    }
}