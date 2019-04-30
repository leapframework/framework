/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package leap.web.api.query;

public class Aggregate {

    protected final String field;
    protected final String function;
    protected final String alias;

    public Aggregate(String field, String function, String alias) {
        this.field = field;
        this.function = function;
        this.alias = alias;
    }

    public String getField() {
        return field;
    }

    public String getFunction() {
        return function;
    }

    public String getAlias() {
        return alias;
    }
}
