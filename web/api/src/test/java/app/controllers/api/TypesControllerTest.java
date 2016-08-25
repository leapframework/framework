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

package app.controllers.api;

import leap.lang.Dates;
import leap.lang.time.DateFormats;
import leap.webunit.WebTestBase;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TypesControllerTest extends WebTestBase {

    @Test
    public void testDateType() {
        String s = get("/api/types/date").getContent();

        assertNotEmpty(s);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Date date = Dates.tryParse(s, format);
        assertNotNull(date);
    }

    @Test
    public void testPlainText() {
        assertEquals("Hello", get("/api/types/plain_text").getContent());
        assertEquals("Hello1", get("/api/types/plain_text1").getContent());
    }

    @Test
    public void testJsonText() {
        assertEquals("\"Hello\"", get("/api/types/json_text").getContent());
        assertEquals("\"Hello1\"", get("/api/types/json_text1").getContent());
    }

}
