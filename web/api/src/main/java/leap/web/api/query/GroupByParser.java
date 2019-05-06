/*
 *  Copyright 2019 the original author or authors.
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
import leap.web.exception.BadRequestException;
import java.util.ArrayList;
import java.util.List;

public class GroupByParser {

    public static GroupBy parse(String expr) {
        List<GroupBy.Item> items = new ArrayList<>();
        GroupBy.Item item;
        String[] names = Strings.split(expr, ',');
        for (String name : names) {
            String[] parts = Strings.splitWhitespaces(name);
            if (parts.length <= 2) {
                String[] join = Strings.split(parts[0], ".");
                if (join.length == 1) {
                    if (parts.length == 1) {
                        item = new GroupBy.Item(parts[0]);
                    } else {
                        item = new GroupBy.Item(parts[0], parts[1]);
                    }
                } else if (join.length == 2) {
                    if (parts.length == 1) {
                        item = new GroupBy.Item(join[1], null, join[0]);
                    } else {
                        item = new GroupBy.Item(join[1], parts[1], join[0]);
                    }
                } else {
                    throw new BadRequestException("Invalid join item '" + parts[0] + "'");
                }
            } else {
                throw new BadRequestException("Invalid select item '" + name + "'");
            }
            items.add(item);
        }
        return new GroupBy(items.toArray(new GroupBy.Item[items.size()]));
    }

}
