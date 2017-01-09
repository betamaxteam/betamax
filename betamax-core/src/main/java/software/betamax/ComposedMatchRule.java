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

import software.betamax.message.Request;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ComposedMatchRule implements MatchRule {

    public static MatchRule of(MatchRule... rules) {
        return new ComposedMatchRule(Arrays.asList(rules));
    }

    public static MatchRule of(Iterable<MatchRule> rules) {
        List<MatchRule> rulesList = new LinkedList<>();
        for (MatchRule rule : rules) {
            rulesList.add(rule);
        }
        return new ComposedMatchRule(rulesList);
    }

    private final List<MatchRule> rules;

    private ComposedMatchRule(List<MatchRule> rules) {
        this.rules = rules;
    }

    @Override
    public boolean isMatch(final Request a, final Request b) {
        for (MatchRule rule : rules) {
            if (!rule.isMatch(a, b)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return rules.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComposedMatchRule that = (ComposedMatchRule) o;

        return rules.equals(that.rules);
    }
}
