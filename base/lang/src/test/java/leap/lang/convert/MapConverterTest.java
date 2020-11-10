/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.lang.convert;

import leap.lang.New;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapConverterTest {

    @Test
    public void testConcreteMap() {
        final Map<String, Object> from = New.hashMap("k", New.hashMap("name", "x"));
        Converts.convert(from, ConcreteMap.class);
        Converts.convert(New.hashMap("map", from), Bean.class);
    }

    public static class ConcreteMap extends LinkedHashMap<String, Bean> {

    }

    public static class Bean {

        protected String      name;
        protected ConcreteMap map;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ConcreteMap getMap() {
            return map;
        }

        public void setMap(ConcreteMap map) {
            this.map = map;
        }
    }
}
