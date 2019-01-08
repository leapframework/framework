/*
 *  Copyright 2019 the original author or authors.
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
 */

package leap.lang;

import leap.junit.TestBase;
import leap.lang.path.AntPathPattern;
import leap.lang.path.PathPattern;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

public class PathPatternTest extends TestBase {

    @Test
    public void testSortAntPathPattern() {
        Set<PathPattern> set = new TreeSet<>();

        set.add(new AntPathPattern("*"));
        set.add(new AntPathPattern("/api/**"));
        set.add(new AntPathPattern("/api/user"));

        PathPattern[] a = set.toArray(new PathPattern[0]);
        assertEquals("/api/user", a[0].pattern());
        assertEquals("/api/**", a[1].pattern());
        assertEquals("*", a[2].pattern());
    }

}
