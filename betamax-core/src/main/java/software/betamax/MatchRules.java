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

import software.betamax.message.Request;

import java.util.Arrays;

/**
 * Standard {@link MatchRule} implementations.
 */
public enum MatchRules implements MatchRule {
    method {
        @Override
        public boolean isMatch(Request a, Request b) {
            return a.getMethod().equalsIgnoreCase(b.getMethod());
        }
    }, uri {
        @Override
        public boolean isMatch(Request a, Request b) {
            return equal(a.getUri(), b.getUri());
        }
    }, host {
        @Override
        public boolean isMatch(Request a, Request b) {
            return equal(a.getUri().getHost(), b.getUri().getHost());
        }
    }, path {
        @Override
        public boolean isMatch(Request a, Request b) {
            return nullSafeEquals(a.getUri().getPath(), b.getUri().getPath());
        }
    }, port {
        @Override
        public boolean isMatch(Request a, Request b) {
            return a.getUri().getPort() == b.getUri().getPort();
        }
    }, query {
        @Override
        public boolean isMatch(Request a, Request b) {
            return nullSafeEquals(a.getUri().getQuery(), b.getUri().getQuery());
        }
    },
    /**
     * Compare query parameters instead of query string representation.
     */
    queryParams {
        @Override
        public boolean isMatch(Request a, Request b) {
            if((a.getUri().getQuery() != null) && (b.getUri().getQuery() != null)) {
                // both request have a query, split query params and compare
                String[] aParameters = a.getUri().getQuery().split("&");
                String[] bParameters = b.getUri().getQuery().split("&");
                Arrays.sort(aParameters);
                Arrays.sort(bParameters);
                return Arrays.equals(aParameters, bParameters);
            } else {
                return (a.getUri().getQuery() == null) && (b.getUri().getQuery() == null);
            }
        }
    }, authorization {
        @Override
        public boolean isMatch(Request a, Request b) {
            return nullSafeEquals(a.getHeader("Authorization"), b.getHeader("Authorization"));
        }
    }, accept {
        @Override
        public boolean isMatch(Request a, Request b) {
            return nullSafeEquals(a.getHeader("Accept"), b.getHeader("Accept"));
        }
    }, body {
        @Override
        public boolean isMatch(Request a, Request b) {
            return Arrays.equals(a.getBodyAsBinary(), b.getBodyAsBinary());
        }
    };

    // Should be replaced with Objects.equals once we use Java 8 language level
    private static boolean equal(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    private static boolean nullSafeEquals(String a, String b) {
        boolean aEmpty = (a == null) || (a.isEmpty());
        boolean bEmpty = (b == null) || (b.isEmpty());

        return (aEmpty && bEmpty) || equal(a, b);
    }
}
