/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.web.api.query;

public class OrderBy {

    private final Item[] items;

    OrderBy(Item[] items) {
        this.items = items;
    }

    public Item[] items() {
        return items;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for(int i=0;i<items.length;i++) {

            if(i > 0) {
                s.append(',');
            }

            Item item = items[i];

            s.append(item.name);

            if(!item.isAscending()) {
                s.append(" desc");
            }
        }

        return s.toString();
    }

    public static final class Item {

        private String  name;
        private boolean ascending = true;

        public Item(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public boolean isAscending() {
            return ascending;
        }

        public void asc() {
            this.ascending = true;
        }

        public void desc() {
            this.ascending = false;
        }
    }

}
