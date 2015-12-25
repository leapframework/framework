/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.lang.json;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import leap.junit.concurrent.ConcurrentTestCase;
import leap.lang.io.IO;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class JSONDecodeTest extends ConcurrentTestCase {
    
    @Test
    public void testDecodeLarge() throws Exception {
        Resource          resource = Resources.getResource("classpath:json/json.json");
        InputStreamReader reader = null;
        try{
            reader = resource.getInputStreamReader();
            JsonValue json = JSON.decodeToJsonValue(reader);
            
            assertTrue(json.isArray());
            
            Object[] array = json.asList().toArray();
            
            assertEquals(2, array.length);
            
            Map<String, Object> map  = (Map<String,Object>)array[1];
            List<Object>        list = null;
            
            assertNotNull(map);
            assertNotNull(map  = (Map<String,Object>)map.get("data"));
            assertNotNull(list = (List<Object>)map.get("children"));
            assertNotNull(map  = (Map<String,Object>)list.get(0));
            
            assertNotNull(map  = (Map<String,Object>)map.get("data"));
            assertNotNull(map  = (Map<String,Object>)map.get("replies"));
            assertNotNull(map  = (Map<String,Object>)map.get("data"));
            assertNotNull(list = (List<Object>)map.get("children"));
            assertNotNull(map  = (Map<String,Object>)list.get(0));    
            
            assertNotNull(map  = (Map<String,Object>)map.get("data"));
            assertNotNull(map  = (Map<String,Object>)map.get("replies"));
            assertNotNull(map  = (Map<String,Object>)map.get("data"));
            assertNotNull(list = (List<Object>)map.get("children"));
            assertNotNull(map  = (Map<String,Object>)list.get(0));      
            
            assertNotNull(map  = (Map<String,Object>)map.get("data"));
            assertNotNull(map  = (Map<String,Object>)map.get("replies"));
            assertNotNull(map  = (Map<String,Object>)map.get("data"));
            assertNotNull(list = (List<Object>)map.get("children"));
            assertNotNull(map  = (Map<String,Object>)list.get(0)); 
            
            assertNotNull(map  = (Map<String,Object>)map.get("data"));
            assertNotNull(map  = (Map<String,Object>)map.get("replies"));
            assertNotNull(map  = (Map<String,Object>)map.get("data"));
            assertNotNull(list = (List<Object>)map.get("children"));
            assertNotNull(map  = (Map<String,Object>)list.get(0)); 
            
            assertNotNull(map  = (Map<String,Object>)map.get("data"));
            assertNotNull(map  = (Map<String,Object>)map.get("replies"));
            assertNotNull(map  = (Map<String,Object>)map.get("data"));
            assertNotNull(list = (List<Object>)map.get("children"));
            assertNotNull(map  = (Map<String,Object>)list.get(0));     
            
            assertNotNull(map  = (Map<String,Object>)map.get("data"));
            assertNotNull(map  = (Map<String,Object>)map.get("replies"));
            assertNotNull(map  = (Map<String,Object>)map.get("data"));
            assertNotNull(list = (List<Object>)map.get("children"));
            assertNotNull(map  = (Map<String,Object>)list.get(0));               
            
        }finally{
            IO.close(reader);
        }
    }

    @Test
    public void testDecodeEncodeAll() throws Exception {
        
        ResourceSet resources = Resources.scan("classpath:json/**/*.json");
        
        for(Resource resource : resources){
            if(resource.exists()){
                System.out.println("-> decoding json '" + resource.getURI().toString() + "'...") ;
                
                InputStream       stream = resource.getInputStream();
                InputStreamReader reader = null;
                
                try{
                    reader = new InputStreamReader(stream);
                    
                    JsonValue json = JSON.decodeToJsonValue(reader);
                    
                    assertNotNull(json);
                    
                    String string = JSON.encode(json.raw());
                    
                    assertNotNull(string);
                    
                    json = JSON.decodeToJsonValue(string);
                    
                    assertNotNull(json);
                }finally{
                    IO.close(reader);
                    IO.close(stream);
                }
            }
        }
    }
    
    @Test
    public void testDecodeList() throws Exception {
    	
    	List<Bean> beans = new ArrayList<Bean>();
    	
    	beans.add(new Bean());
    	beans.add(new Bean());
    	
    	String json = JSON.encode(beans);
    	
    	Bean[] decoded = JSON.decodeToArray(json, Bean.class);
    	
    	assertEquals(2, decoded.length);
    	
    	for(int i=0;i<decoded.length;i++){
    		assertEquals(beans.get(i).name,decoded[i].name);
    	}
    }
    
    @Test
    public void testDecode1(){
    	JSON.decodeToJsonValue("[{ id : \"\", fields:[{ id:\"\",attrs:{}}]}]");
    }
    
    @Test
    public void testDecodeUpperCaseProperties(){
    	Bean1 bean0 = new Bean1();
    	bean0.name = "xxx";
    	
    	String json = JSON.encode(bean0);
    	assertEquals("{\"Name\":\"xxx\"}", json);
    	
    	Bean1 bean1 = JSON.decode(json,Bean1.class);
    	assertEquals("xxx",bean1.name);
    }

    private static class Bean {
    	public String name = UUID.randomUUID().toString();
    }
    
    private static class Bean1 {
    	@JsonNamed("Name")
    	public String name;
    }
}
