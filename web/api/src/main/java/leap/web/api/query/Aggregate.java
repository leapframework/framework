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

import leap.lang.Strings;

public class Aggregate {

    private final Aggregate.Item[] items;

    Aggregate(Item[] items) {
        this.items = items;
    }

    public Item[] items() {
        return items;
    }

    public boolean aliasContain(String expr) {
        for (Aggregate.Item item : items) {
            if (Strings.isNotEmpty(item.alias) && item.alias().equals(expr)) {
                return true;
            }
        }
        return false;
    }


    public static final class Item {

        private String name;
        private String function;
        private String alias;

        public Item(String name, String function, String alias) {
            this.name = name;
            this.function = function;
            this.alias = alias;
        }

        public String name() {
            return name;
        }

        public String function() {
            return function;
        }

        public String alias() {
            return alias;
        }
    }

}
