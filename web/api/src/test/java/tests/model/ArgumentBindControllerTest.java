/*
 *
 *  * Copyright 2013 the original author or authors.
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

package tests.model;

import leap.webunit.WebTestBase;
import org.junit.Test;

/**
 * Created by kael on 2017/2/20.
 */
public class ArgumentBindControllerTest extends WebTestBase {
    @Test
    public void testDate(){
        String content = forPost("/api/arg_bind/date")
                .addFormParam("DATE_PATTERN","2017-02-20")
                .addFormParam("TIME_PATTERN","01:59:50")
                .addFormParam("DATETIME_PATTERN","2017-02-20 01:59:50")
                .addFormParam("TIMESTAMP_PATTERN","2017-02-20 01:59:50.443")
                .addFormParam("ISO8601_DATE_PATTERN","2017-02-20T01:59:50.96Z")
                .addFormParam("RFC3339_DATE_PATTERN1","2017-02-20T01:59:50.960Z")
                .addFormParam("T_DATE_PATTERN","2017-02-20")
                .addFormParam("T_TIME_PATTERN","01:59:50")
                .addFormParam("T_DATETIME_PATTERN","2017-02-20 01:59:50")
                .addFormParam("T_TIMESTAMP_PATTERN","2017-02-20 01:59:50.443")
                .addFormParam("T_ISO8601_DATE_PATTERN","2017-02-20T01:59:50.96Z")
                .addFormParam("T_RFC3339_DATE_PATTERN1","2017-02-20T01:59:50.960Z")
                .send().getContent();
        assertEquals("true",content);
    }
    
}
