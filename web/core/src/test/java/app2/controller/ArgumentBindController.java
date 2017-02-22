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

import leap.lang.json.JsonParsable;
import leap.lang.json.JsonValue;
import leap.web.annotation.Path;
import leap.web.annotation.ResolveByJson;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by kael on 2017/2/19.
 */
public class ArgumentBindController {
    
    public boolean testDate(Date date, Timestamp timestamp){
        if(date == null || timestamp == null){
            return false;
        }
        return true;
    }
    
    public String testJsonArgumentResolver(@ResolveByJson List<Map<String, Object>> json){
        if(json == null || json.isEmpty() || json.get(0).get("name") == null){
            return "";
        }
        return json.get(0).get("name").toString();
    }
    
    public boolean testJsonParseAble(JsonParseAbleImpl json){
        if(json.ext != null && json.ext.equals("ext")){
            return true;
        }
        return false;
    }
    
    public static class JsonParseAbleImpl implements JsonParsable{
        
        private String name;
        private String ext;
        
        @Override
        public void parseJson(JsonValue json) {
            this.name = json.asJsonObject().get("name");
            this.ext = "ext";
        }
    }
    
}
