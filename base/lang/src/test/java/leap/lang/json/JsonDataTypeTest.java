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

package leap.lang.json;

import leap.junit.TestBase;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;

public class JsonDataTypeTest extends TestBase {

    @Test
    public void testLocalDate() {
        String s = "2016-11-01";

        LocalDate d = LocalDate.parse(s);
        assertEquals("\"" + s + "\"", JSON.encode(d));

        d = JSON.decode(s, LocalDate.class);

        assertEquals(s, d.toString());
    }

    @Test
    public void testLocalTime() {
        String s = "10:11:12";

        LocalTime time = LocalTime.parse(s);
        assertEquals("\"" + s + "\"", JSON.encode(time));

        time = JSON.decode(s, LocalTime.class);
        assertEquals(s, time.toString());
    }

    @Test
    public void testLocalDateTime() {
        String s = "2016-11-01T10:11:12";

        LocalDateTime dt = LocalDateTime.parse(s);
        assertEquals("\"" + s + "\"", JSON.encode(dt));

        dt = JSON.decode(s, LocalDateTime.class);
        assertEquals(s, dt.toString());
    }

    @Test
    public void testConcreteMap() {
        String s = "{'k':'v'}";

        ConcreteMap map = JSON.decode(s, ConcreteMap.class);
        assertEquals("v", map.get("k"));

        s = "[" + s + "]";
        List<ConcreteMap> list = JSON.decodeList(s, ConcreteMap.class);
        map = list.get(0);
        assertEquals("v", map.get("k"));
    }

    public static final class ConcreteMap extends LinkedHashMap{

    }
}
