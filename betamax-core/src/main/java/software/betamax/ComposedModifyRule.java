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

package software.betamax;

import com.google.common.collect.ImmutableSet;
import software.betamax.message.Request;
import software.betamax.message.Response;

/**
 * Created by bismail on 12/21/2016.
 */
public class ComposedModifyRule implements ModifyRule {

    public ComposedModifyRule(ImmutableSet<ModifyRule> rules) {
        this.rules = rules;
    }

    public static ModifyRule of(ModifyRule... rules) {
        return new ComposedModifyRule(ImmutableSet.copyOf(rules));
    }

    public static ModifyRule of(Iterable<ModifyRule> rules) {
        return new ComposedModifyRule(ImmutableSet.copyOf(rules));
    }

    private final ImmutableSet<ModifyRule> rules;

    @Override
    public Response getModifiedResponse(Request request, Response response) {
        Response returnResp = response;
        for(ModifyRule rule : rules) {
            returnResp = rule.getModifiedResponse(request, returnResp);
        }
        return returnResp;
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

        ComposedModifyRule that = (ComposedModifyRule) o;

        return rules.equals(that.rules);
    }

}
