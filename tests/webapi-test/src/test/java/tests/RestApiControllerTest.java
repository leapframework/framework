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

package tests;

import app.models.api.RestApi;
import leap.webunit.WebTestBase;
import org.junit.Test;

/**
 * @author kael.
 */
public class RestApiControllerTest extends WebTestBase {
    @Test
    public void testGetPublished(){
        RestApi api1 = new RestApi();
        api1.setPublished(true);
        api1.setName("api1");
        api1.create();
        
        RestApi api2 = new RestApi();
        api2.setPublished(true);
        api2.setName("api2");
        api2.create();
        int length = useGet("/api/restapi/published").send().getJson().asArray().length;
        assertEquals(length, RestApi.count());
        assertTrue(length>0);
    }
    
}
