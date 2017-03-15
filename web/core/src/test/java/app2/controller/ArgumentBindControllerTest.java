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

package app2.controller;

import leap.lang.New;
import leap.lang.json.JSON;
import leap.webunit.WebTestBase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kael on 2017/2/19.
 */
public class ArgumentBindControllerTest extends WebTestBase {
    
    private static final String CONTEXT="/app2/mvc/argument_bind";
    
    @Test
    public void testDateBind(){
        String res = forPost(CONTEXT+"/test_date")
                .addFormParam("date","2017-02-17")
                .addFormParam("timestamp","2017-02-17")
                .send().assertOk().getContent();
        assertEquals("true",res);
    }
    @Test
    public void testJsonArgumentResolver(){
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(New.hashMap("name","name"));
        Map<String, ArgumentBindController.JsonParseAbleImpl> map = new HashMap<>();
        ArgumentBindController.JsonParseAbleImpl impl = new ArgumentBindController.JsonParseAbleImpl();
        impl.setName("name");
        map.put("impl",impl);
        
        
        String res = forPost(CONTEXT+"/test_json_argument_resolver")
                .addFormParam("listMap", JSON.encode(list))
                .addFormParam("map",JSON.encode(map))
                .addFormParam("impl",JSON.encode(impl))
                .send().assertOk().getContent();
        assertEquals(res,"name");
    }
    @Test
    public void testJsonParseAble(){
        Map<String,Object> map = New.hashMap("name","name");
        String content = postJson(CONTEXT+"/test_json_parse_able",map).assertOk().getContent();
        assertEquals("true",content);
    }
    @Test
    public void testDefaultValue(){
        String content = forPost(CONTEXT+"/test_default_value")
                .addFormParam("string1","str").send().assertOk().getContent();
        assertEquals("true",content);
    }
    
}
