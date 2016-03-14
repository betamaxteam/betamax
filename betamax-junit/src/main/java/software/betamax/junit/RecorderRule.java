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

package software.betamax.junit;

import com.google.common.base.Strings;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import software.betamax.Configuration;
import software.betamax.ProxyConfiguration;
import software.betamax.Recorder;
import software.betamax.proxy.ProxyServer;
import software.betamax.tape.Tape;

/**
 * This is an extension of {@link Recorder} that can be used as a
 * _JUnit @Rule_ allowing tests annotated with `@Betamax` to automatically
 * activate
 * Betamax recording.
 */
public class RecorderRule implements TestRule {

    private Recorder recorder;
    private ProxyServer proxyServer;

    public RecorderRule() {
        initServer(ProxyConfiguration.builder()
                .createProxyOnStartup(false)
                .build());
    }

    public RecorderRule(final Configuration configuration) {
        initServer(ProxyConfiguration.builder()
                .withConfig(configuration)
                .createProxyOnStartup(false)
                .build());
    }

    private void initServer(final ProxyConfiguration proxyConfiguration) {
        proxyServer = new ProxyServer(proxyConfiguration);

        recorder = new Recorder(proxyConfiguration);
        recorder.addListener(proxyServer);
    }

    @Override
    public Statement apply(final Statement statement, final Description description) {
        RuleChain ruleChain = RuleChain.emptyRuleChain();

        // class rule - start the proxy server
        if (Strings.isNullOrEmpty(description.getMethodName())) {
            ruleChain = ruleChain.around(new ProxyServerResource(proxyServer));

        } else if (!proxyServer.isRunning() && description.getAnnotation(Betamax.class) != null) {

            // method rule - start/stop the proxy if it isn't already started and there's a betamax annotation
            ruleChain = ruleChain.around(new ProxyServerResource(proxyServer));
        }

        // start the recorder for the given betamax tape on either the class or the test method
        return ruleChain.around(new RecorderResource(description, recorder)).apply(statement, description);
    }

    public Tape getTape() {
        return recorder.getTape();
    }

    public Configuration getConfiguration() {
        return recorder.getConfiguration();
    }
}