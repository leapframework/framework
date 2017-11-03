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

import leap.junit.concurrent.ConcurrentTestCase;
import leap.lang.Beans;
import leap.lang.New;
import leap.lang.io.IO;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class JSONDecodeTest extends ConcurrentTestCase {
    
    @Test
    public void testDecodeLarge() throws Exception {
        Resource          resource = Resources.getResource("classpath:json/json.json");
        InputStreamReader reader = null;
        try{
            reader = resource.getInputStreamReader();
            JsonValue json = JSON.parse(reader);
            
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
                    
                    JsonValue json = JSON.parse(reader);
                    
                    assertNotNull(json);
                    
                    String string = JSON.encode(json.raw());
                    
                    assertNotNull(string);
                    
                    json = JSON.parse(string);
                    
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
    	
    	Bean[] decoded = JSON.decodeArray(json, Bean.class);
    	
    	assertEquals(2, decoded.length);
    	
    	for(int i=0;i<decoded.length;i++){
    		assertEquals(beans.get(i).name,decoded[i].name);
    	}
    }
    
    @Test
    public void testDecode1(){
    	JSON.parse("[{ id : \"\", fields:[{ id:\"\",attrs:{}}]}]");
    }
    
    @Test
    public void testDecodeToGenericType(){
        Map<String, Bean> beanMap = New.hashMap();
        beanMap.put("bean1",new Bean());

        String json = JSON.encode(beanMap);
        Map<String, Bean> beanMap1 = JSON.decodeMap(json,Bean.class);
        assertEquals(beanMap.size(), beanMap1.size());
        assertEquals(beanMap.get("bean1").getClass(), beanMap1.get("bean1").getClass());
        assertEquals(beanMap.get("bean1").name, beanMap1.get("bean1").name);
        
        List<Bean> beanList = New.arrayList();
        beanList.add(new Bean());
        json = JSON.encode(beanList);
        List<Bean> beanList1 = JSON.decodeList(json,Bean.class);
        assertEquals(beanList.size(), beanList1.size());
        assertEquals(beanList.get(0).name, beanList1.get(0).name);
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

    @Test
    public void testJsonWriterWithJsonSetting(){
        JsonWriter writer = JSON.createWriter(new JsonSettings(true,true,
                true,true,true, false,null, null));
        writer.map(Beans.toMap(new JsonWriterBean()));
        Map map = JSON.decode(writer.toString());
        assertNull(map.get("strEmpty"));
        assertFalse(map.containsKey("strEmpty"));
        assertFalse(map.containsKey("strNull"));
        assertFalse(map.containsKey("arrayEmpty"));
        assertFalse(map.containsKey("boolFalse"));
        assertTrue(map.containsKey("str"));
        assertTrue(map.containsKey("boolTrue"));
    }

    @Test
    public void testJsonSingleLineComment() {
        //single
        assertEquals("1", JSON.encode(JSON.decode("//comment\n1")));
        assertEquals("1", JSON.encode(JSON.decode("1\n//comment")));
        assertEquals("1", JSON.encode(JSON.decode("1 //comment")));
        assertEquals("1", JSON.encode(JSON.decode("1//comment")));

        assertEquals("1.0", JSON.encode(JSON.decode("//comment\n1.0")));
        assertEquals("1.0", JSON.encode(JSON.decode("1.0\n//comment")));

        assertEquals("\"s\"", JSON.encode(JSON.decode("//comment\n\"s\"")));
        assertEquals("\"s\"", JSON.encode(JSON.decode("\"s\"\n//comment")));

        //object
        assertEquals("{\"k\":\"v\"}", JSON.encode(JSON.decode("{//comment\n\"k\":\"v\"}")));
        assertEquals("{\"k\":\"v\"}", JSON.encode(JSON.decode("{\"k\":\"v\"//comment\n}")));
        assertEquals("{\"k\":1}", JSON.encode(JSON.decode("{\"k\":1//comment\n}")));
        assertEquals("{\"k\":1.0}", JSON.encode(JSON.decode("{\"k\":1.0//comment\n}")));

        //array
        assertEquals("[1,2]", JSON.encode(JSON.decode("[//comment\n1,2]")));
        assertEquals("[1,2]", JSON.encode(JSON.decode("[1,2//comment\n]")));
    }

    @Test
    public void testJsonMultiLineComment() {
        //single number
        assertEquals("1", JSON.encode(JSON.decode("/*comment\n*/1")));
        assertEquals("1", JSON.encode(JSON.decode("1\n/*comment\n*/")));
        assertEquals("1", JSON.encode(JSON.decode("1 /*comment*/")));
        assertEquals("1", JSON.encode(JSON.decode("1/*comment*/")));

        //object
        assertEquals("{\"k\":\"v\"}", JSON.encode(JSON.decode("{/*comment*/\"k\":\"v\"}")));
        assertEquals("{\"k\":\"v\"}", JSON.encode(JSON.decode("{\"k\":\"v\"/*comment*/}")));
        assertEquals("{\"k\":1}", JSON.encode(JSON.decode("{\"k\":1/*comment*/}")));
        assertEquals("{\"k\":1.0}", JSON.encode(JSON.decode("{\"k\":1.0/*comment*/}")));

        //array
        assertEquals("[1,2]", JSON.encode(JSON.decode("[/*comment\n*/1,2]")));
        assertEquals("[1,2]", JSON.encode(JSON.decode("[1,2/*comment\n*/]")));
    }
    
    private static  class JsonWriterBean{
        public String str = "str";
        public String strEmpty = "";
        public String strNull = null;
        public String[] arrayEmpty = {};
        public boolean boolFalse = false;
        public boolean boolTrue = true;
    }
    
    private static class Bean {
    	public String name = UUID.randomUUID().toString();
    }
    
    private static class Bean1 {
    	@JsonName("Name")
    	public String name;
    }
}
