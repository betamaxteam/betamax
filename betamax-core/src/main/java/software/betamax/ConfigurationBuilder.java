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

package software.betamax;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public abstract class ConfigurationBuilder<T extends ConfigurationBuilder<T>> {

    protected abstract T self();

    public Configuration build() {
        return new Configuration(this);
    }

    protected File tapeRoot = new File(Configuration.DEFAULT_TAPE_ROOT);
    protected TapeMode defaultMode = Configuration.DEFAULT_MODE;
    protected MatchRule defaultMatchRule = Configuration.DEFAULT_MATCH_RULE;
    protected ImmutableCollection<String> ignoreHosts = ImmutableList.of();
    protected boolean ignoreLocalhost;

    protected T configureFromPropertiesFile() {
        try {
            URL propertiesFile = Configuration.class.getResource("/betamax.properties");
            if (propertiesFile != null) {
                Properties properties = new Properties();
                properties.load(Files.newReader(new File(propertiesFile.toURI()), Charsets.UTF_8));
                withProperties(properties);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return self();
    }

    public T withProperties(Properties properties) {
        if (properties.containsKey("betamax.tapeRoot")) {
            tapeRoot(new File(properties.getProperty("betamax.tapeRoot")));
        }

        if (properties.containsKey("betamax.defaultMode")) {
            defaultMode(TapeMode.valueOf(properties.getProperty("betamax.defaultMode")));
        }

        if (properties.containsKey("betamax.defaultMatchRules")) {
            List<MatchRule> rules = Lists.transform(Splitter.on(",").splitToList(properties.getProperty("betamax.defaultMatchRules")), new Function<String, MatchRule>() {
                @Override
                public MatchRule apply(String input) {
                    return MatchRules.valueOf(input);
                }
            });
            defaultMatchRule(ComposedMatchRule.of(rules));
        }

        if (properties.containsKey("betamax.ignoreHosts")) {
            ignoreHosts(Splitter.on(",").splitToList(properties.getProperty("betamax.ignoreHosts")));
        }

        if (properties.containsKey("betamax.ignoreLocalhost")) {
            ignoreLocalhost(Boolean.valueOf(properties.getProperty("betamax.ignoreLocalhost")));
        }

        return self();
    }

    public T withConfig(final Configuration configuration) {
        tapeRoot(configuration.getTapeRoot());
        defaultMode(configuration.getDefaultMode());
        defaultMatchRule(configuration.getDefaultMatchRule());
        ignoreHosts(configuration.getIgnoreHosts());
        ignoreLocalhost(configuration.isIgnoreLocalhost());

        return self();
    }

    public T tapeRoot(File tapeRoot) {
        this.tapeRoot = tapeRoot;
        return self();
    }

    public T defaultMode(TapeMode defaultMode) {
        this.defaultMode = defaultMode;
        return self();
    }

    public T defaultMatchRule(MatchRule defaultMatchRule) {
        this.defaultMatchRule = defaultMatchRule;
        return self();
    }

    public T defaultMatchRules(MatchRule... defaultMatchRules) {
        this.defaultMatchRule = ComposedMatchRule.of(defaultMatchRules);
        return self();
    }

    public T ignoreHosts(Iterable<String> ignoreHosts) {
        this.ignoreHosts = ImmutableList.copyOf(ignoreHosts);
        return self();
    }

    public T ignoreLocalhost(boolean ignoreLocalhost) {
        this.ignoreLocalhost = ignoreLocalhost;
        return self();
    }

}
