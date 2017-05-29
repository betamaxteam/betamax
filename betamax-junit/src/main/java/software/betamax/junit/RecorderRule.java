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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.betamax.*;

import static com.google.common.base.CaseFormat.*;

/**
 * This is an extension of {@link Recorder} that can be used as a
 * _JUnit @Rule_ allowing tests annotated with `@Betamax` to automatically
 * activate
 * Betamax recording.
 */
public class RecorderRule extends Recorder implements TestRule {

    private final Logger log = LoggerFactory.getLogger(RecorderRule.class.getName());

    public RecorderRule() {
        super();
    }

    public RecorderRule(Configuration configuration) {
        super(configuration);
    }

    @Override
    public Statement apply(final Statement statement, final Description description) {
        final Betamax annotation = description.getAnnotation(Betamax.class);
        if (annotation != null) {
            log.debug(String.format("found @Betamax annotation on '%s'", description.getDisplayName()));
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    try {
                        String tapeName = annotation.tape();
                        if (tapeName == null || tapeName.length() == 0) {
                            tapeName = defaultTapeName(description);
                        }

                        TapeMode tapeMode = annotation.mode();
                        if (tapeMode.equals(TapeMode.UNDEFINED)) {
                            tapeMode = null;
                        }

                        MatchRules[] matchRules = annotation.match();

                        MatchRule matchRule = null;
                        if (matchRules.length > 0) {
                            matchRule = ComposedMatchRule.of(matchRules);
                        }

                        start(tapeName, tapeMode, matchRule);

                        statement.evaluate();
                    } catch (Exception e) {
                        log.error("Caught exception starting Betamax", e);
                        throw e;
                    } finally {
                        stop();
                    }
                }
            };
        } else {
            log.debug(String.format("no @Betamax annotation on '%s'", description.getDisplayName()));
            return statement;
        }
    }

    private String defaultTapeName(Description description) {
        String name;
        if (description.getMethodName() != null) {
            name = LOWER_CAMEL.to(LOWER_UNDERSCORE, description.getMethodName());
        } else {
            name = UPPER_CAMEL.to(LOWER_UNDERSCORE, description.getTestClass().getSimpleName());
        }
        return name.replace('_', ' ');
    }

}
