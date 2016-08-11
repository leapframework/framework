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

public class Filters {

    private final FiltersParser.Node[] nodes;

    Filters(FiltersParser.Node[] nodes) {
        this.nodes = nodes;
    }

    public FiltersParser.Node[] nodes() {
        return nodes;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for(int i=0;i<nodes.length;i++) {
            FiltersParser.Node node = nodes[i];

            if(i > 0) {
                s.append(' ');
            }

            if(node.isQuoted()) {
                s.append('\'');
            }
            s.append(node.literal());
            if(node.isQuoted()) {
                s.append('\'');
            }
        }

        return s.toString();
    }
}
