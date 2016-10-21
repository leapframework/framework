/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.lang;

import leap.junit.TestBase;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

public class OrderedTest extends TestBase {

    @Test
    public void testTreeSet() {
        Set<OrderedBase> set = new TreeSet<>(Comparators.ORDERED_COMPARATOR);

        set.add(new OrderedBase(10));
        set.add(new OrderedBase(9));
        set.add(new OrderedBase(9.1f));

        OrderedBase[] items = set.toArray(new OrderedBase[0]);

        assertTrue(items[0].getSortOrder() == 9.0f);
        assertTrue(items[1].getSortOrder() == 9.1f);
        assertTrue(items[2].getSortOrder() == 10.0f);
    }

}
