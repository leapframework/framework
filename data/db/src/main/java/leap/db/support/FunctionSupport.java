/*
 *  Copyright 2020 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package leap.db.support;

import leap.db.DbDialect;
import leap.lang.Strings;

public interface FunctionSupport {

    String FUNC_NOW                   = "now";
    String FUNC_TIMESTAMP_ADD_MILLIS  = "timestamp_add_millis";
    String FUNC_TIMESTAMP_ADD_SECONDS = "timestamp_add_seconds";

    String PARAM_INTERVAL  = ":interval";
    String PARAM_TIMESTAMP = ":timestamp";

    /**
     * Returns the {@link DbDialect}.
     */
    DbDialect dialect();

    /**
     * Returns the function definition or <code>null</code> if not defined.
     */
    String get(String name);

    /**
     * Returns the function definition or the given defaults if not defined.
     */
    default String get(String name, String defaults) {
        String s = get(name);
        if (Strings.isEmpty(s)) {
            return defaults;
        } else {
            return s;
        }
    }

    /**
     * Returns the function definition or throws {@link IllegalStateException} if not defined.
     */
    default String mustGet(String name) throws IllegalStateException {
        String f = get(name);
        if (Strings.isEmpty(f)) {
            throw new IllegalStateException("Function '" + name + "' not supported by '" + dialect() + "'");
        }
        return f;
    }

    /**
     * Returns the function of now (current_timestamp) without any parameters.
     */
    default String getNow() {
        return get("now", "CURRENT_TIMESTAMP");
    }

    /**
     * Returns function for adding interval in milliseconds from a timestamp expr.
     *
     * <p></p>
     * Parameters:
     * <pre>
     *  interval: the interval milliseconds to add
     *  ts:       the timestamp expr
     *  </pre>
     * <p>
     * Example: <code>TIMESTAMPADD(MS, :interval, :ts)</code>
     */
    default String mustGetTimestampAddMilliseconds() {
        return mustGet(FUNC_TIMESTAMP_ADD_MILLIS);
    }

    /**
     * See {@link #mustGetTimestampAddMilliseconds()}
     */
    default String getTimestampAddMilliseconds() {
        return get(FUNC_TIMESTAMP_ADD_MILLIS);
    }

    /**
     * Returns function for adding interval in seconds from a timestamp expr.
     *
     * <p></p>
     * Parameters:
     * <pre>
     *  interval: the interval milliseconds to add
     *  timestamp: the timestamp expr
     *  </pre>
     * <p>
     * Example: <code>TIMESTAMPADD(MS, :interval, :ts)</code>
     */
    default String mustGetTimestampAddSeconds() {
        return mustGet("timestamp_add_seconds");
    }

    /**
     * See {@link #mustGetTimestampAddSeconds()}.
     */
    default String getTimestampAddSeconds() {
        return get("timestamp_add_seconds");
    }
}
