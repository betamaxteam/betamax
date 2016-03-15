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

import com.google.common.base.Strings;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import software.betamax.ComposedMatchRule;
import software.betamax.MatchRule;
import software.betamax.Recorder;
import software.betamax.TapeMode;

import java.util.logging.Logger;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

public class RecorderResource extends ExternalResource {

    private static final Logger LOG = Logger.getLogger(RecorderResource.class.getName());

    private final Description description;
    private final Recorder recorder;

    public RecorderResource(final Description description,
                            final Recorder recorder) {
        this.description = description;
        this.recorder = recorder;
    }

    @Override
    protected void before() throws Throwable {

        final Betamax annotation = description.getAnnotation(Betamax.class);
        if (annotation == null) {
            LOG.fine(String.format("no @Betamax annotation on '%s'", description.getDisplayName()));
            return;
        }

        LOG.fine(String.format("found @Betamax annotation on '%s'", description.getDisplayName()));

        String tapeName = annotation.tape();
        if (Strings.isNullOrEmpty(tapeName)) {
            tapeName = defaultTapeName(description);
        }

        final TapeMode tapeMode = annotation.mode();
        final MatchRule matchRule = ComposedMatchRule.of(annotation.match());

        recorder.start(tapeName, tapeMode, matchRule);
    }

    @Override
    protected void after() {

        // don't stop the recorder if there was no annotation on the class or method
        final Betamax annotation = description.getAnnotation(Betamax.class);
        if (annotation != null) {
            recorder.stop();
        }
    }

    protected String defaultTapeName(final Description description) {
        String name;
        if (description.getMethodName() != null) {
            name = LOWER_CAMEL.to(LOWER_UNDERSCORE, description.getMethodName());
        } else {
            name = UPPER_CAMEL.to(LOWER_UNDERSCORE, description.getTestClass().getSimpleName());
        }

        return name.replace('_', ' ');
    }
}