/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.lang;

import junit.framework.TestCase;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

public class TypesTest extends TestCase {

    @Test
    public void testGenericTypeLost() throws Exception {
        Method list = SubSub.class.getMethod("list");
        assertEquals(String.class, Types.getActualTypeArgument(SubSub.class, list.getGenericReturnType()));

    }

    public static class Super<T> {

    }

    public static class Sub<T> extends Super<T> {
        public List<T> list() {
            throw new UnsupportedOperationException("");
        }

        public T one() {
            throw new UnsupportedOperationException("");
        }
    }

    public static class SubSub extends Sub<String> {

    }
}
