/*
 * Copyright 2013 the original author or authors.
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
package app.controllers;

import java.util.Map;

import leap.junit.contexual.Contextual;
import leap.junit.contexual.ContextualIgnore;
import leap.lang.json.JSON;
import leap.web.WebTestCase;
import leap.webunit.client.THttpRequest;

import org.junit.Test;

import app.controllers.ArgumentController.CollecitonModel;
import app.controllers.ArgumentController.Item;
import app.controllers.ArgumentController.SimpleModel;
import app.controllers.ArgumentController.TestForm;

@Contextual
public class ArgumentControllerTest extends WebTestCase {

	@Test
	public void testInt1(){
		get("/argument/int1?value=100");
		assertEquals("100",response.getContent());
		
		get("/argument/int1");
		assertEquals("0", response.getContent());
		
		get("/argument/int1?value=");
		assertEquals("0", response.getContent());
	}
	
	@Test
	public void testInt2(){
		get("/argument/int2?intValue=100");
		assertEquals("100",response.getContent());
		
		get("/argument/int2");
		assertEquals("null", response.getContent());
		
		get("/argument/int2?intValue=");
		assertEquals("null", response.getContent());
	}
	
	@Test
	public void testIntArray1(){
		get("/argument/int_array1?value=100");
		assertEquals("100",response.getContent());
		
		get("/argument/int_array1");
		assertEquals("", response.getContent());
		
		get("/argument/int_array1?value=1&value=2");
		assertEquals("1,2", response.getContent());
		
		get("/argument/int_array1?value=1,2");
		assertEquals("1,2", response.getContent());
		
		get("/argument/int_array1?value[]=1&value[]=2");
		assertEquals("1,2", response.getContent());
		
		get("/argument/int_array1?value[0]=1");
		assertEquals("1", response.getContent());
		
		get("/argument/int_array1?value[0]=1&value[1]=2");
		assertEquals("1,2", response.getContent());
		
		get("/argument/int_array1?value[0]=1&value[2]=2");
		assertEquals("1,0,2", response.getContent());
	}
	
	@Test
	public void testIntArray2(){
		get("/argument/int_array2?value=100");
		assertEquals("100",response.getContent());
		
		get("/argument/int_array2");
		assertEquals("", response.getContent());
		
		get("/argument/int_array2?value=1&value=2");
		assertEquals("1,2", response.getContent());
		
		get("/argument/int_array2?value=1,2");
		assertEquals("1,2", response.getContent());
		
		get("/argument/int_array2?value[0]=1");
		assertEquals("1", response.getContent());
		
		get("/argument/int_array2?value[0]=1&value[1]=2");
		assertEquals("1,2", response.getContent());
		
		get("/argument/int_array2?value[0]=1&value[2]=2");
		assertEquals("1,null,2", response.getContent());		
	}
	
	@Test
	public void testBeanArray() {
		THttpRequest form = client().request("/argument/bean_array");
		assertArrayTest(form);
	}
	
	@Test
	public void testBeanArray1() {
		THttpRequest form = client().request("/argument/bean_array1");
		
		form.addFormParam("items0[0].strValue", "s1");
		form.addFormParam("items1[0].strValue", "s2");

		String json = form.send().getContent();
		Item[] items = JSON.decodeToArray(json, ArgumentController.Item.class);
		assertEquals(2,items.length);
		assertEquals("s1",items[0].getStrValue());
		assertEquals("s2",items[1].getStrValue());
	}
	
	@Test
	public void testBeanList() {
		THttpRequest form = client().request("/argument/bean_list");
		assertArrayTest(form);
	}
	
	@Test
	public void testMap() {
		THttpRequest form = client().request("/argument/map");
		assertMapTest(form);
	}

	@Test
	public void testMapArray() {
		THttpRequest form = client().request("/argument/map_array");
		assertArrayTest(form);
	}
	
	@Test
	public void testMapList() {
		THttpRequest form = client().request("/argument/map_list");
		assertArrayTest(form);
	}
	
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void testMapModelList() {
	    THttpRequest form = client().request("/argument/map_model_list");
	    
	    form.addFormParam("items[0].strValue", "s1");
	    
	    String json = form.send().getContent();
	    
	    Object[] items = JSON.decodeToArray(json);
	    
	    Map<String, Object> item0 = (Map)items[0];
	    assertEquals("__v", item0.get("__key"));
	    assertEquals("s1",  item0.get("strValue"));
	}
    
    @Test
    public void testCollectionModel() {
        THttpRequest form = client().request("/argument/collection_model");
        
        form.addFormParam("cm.simpleModels[0].name", "test");
        
        String json = form.send().getContent();
        
        CollecitonModel cm = JSON.decode(json, CollecitonModel.class);
        assertEquals(1, cm.getSimpleModels().size());
        
        SimpleModel sm = cm.getSimpleModels().get(0);
        assertEquals("test",sm.getName());
    }
	
	protected void assertArrayTest(THttpRequest form) {
		form.addFormParam("items[0].strValue", "s1");
		form.addFormParam("items[0][intValue]", "1");
		form.addFormParam("items[0].intArray[0]", "1");
		form.addFormParam("items[0].intArray[1]", "2");
		form.addFormParam("items[0].intArrays[0][0]", "1");
		
		form.addFormParam("items[1][strValue]", "s2");
		form.addFormParam("items[1].intValue", "2");
		
		form.addFormParam("items[2].strValue", "s3");
		form.addFormParam("items[2][intValue]", "3");	
		form.addFormParam("items[2][intArray][0]", "1");
		
		form.addFormParam("items[4].strValue", "s5");
		form.addFormParam("items[4].intValue", "5");
		form.addFormParam("items[4].itemArray[0].strValue", "s5-s1");
		form.addFormParam("items[4].itemArray[0].intValue", "51");

		String json = form.send().getContent();
		
		Item[] items = JSON.decodeToArray(json, ArgumentController.Item.class);
		assertEquals(5,items.length);
		assertEquals("s1",items[0].getStrValue());
		assertEquals(new Integer(1),items[0].getIntValue());
		assertEquals(2,items[0].getIntArray().length);
		assertEquals(1,items[0].getIntArray()[0]);
		assertEquals(2,items[0].getIntArray()[1]);
		assertEquals(1,items[0].getIntArrays().length);
		assertEquals(1,items[0].getIntArrays()[0].length);
		assertEquals(1,items[0].getIntArrays()[0][0]);
		
		assertEquals("s2",items[1].getStrValue());
		assertEquals(new Integer(2),items[1].getIntValue());
		assertEquals(1,items[2].getIntArray().length);
		assertEquals(1,items[2].getIntArray()[0]);
		
		assertNull(items[3]);
		
		Item item4 = items[4];
		assertEquals("s5-s1", item4.getItemArray()[0].getStrValue());
		assertEquals(new Integer(51), item4.getItemArray()[0].getIntValue());
	}
	private void assertMapTest(THttpRequest form) {
		form.addFormParam("strValue", "s1");
		form.addFormParam("intValue", "1");
		String json = form.send().getContent();
		
		Map<String, Object> result = JSON.decodeToMap(json);
		assertEquals("s1",result.get("strValue"));
		assertEquals("1",result.get("intValue"));
	}
	
	@Test
	@ContextualIgnore
	public void testPathVar(){
		get("argument/path_var/xxx");
		assertTrue(response.isOk());
		assertEquals("xxx", response.getContent());
		
		get("argument/path_var1/aaa").assertContentEquals("aaa");
	}
	
	@Test
	public void testForm(){
		THttpRequest form = client().request("argument/test_form");
		
		form.addFormParam("name", "test");
		form.addFormParam("intArray[0]", "1");
		form.addFormParam("intArray[1]", "2");
		form.addFormParam("testParameters.p1", "p1");
		form.addFormParam("testParameters.p2", "p2");
		
		String json = form.send().getContent();

		TestForm o = JSON.decode(json,TestForm.class);
		assertEquals("test",o.getName());
		assertEquals(2,o.getIntArray().length);
		assertEquals(1,o.getIntArray()[0]);
		assertEquals(2,o.getIntArray()[1]);
	}

    @Test
    @SuppressWarnings("unchecked")
	public void testForm1() {
		THttpRequest form = client().request("argument/test_form1");
		form.addFormParam("name", "test");
		form.addFormParam("form.name", "test1");
		
		Map<String, Object> json = form.send().getJson().asMap();
		assertEquals(json.get("name"),"test");
		
		Map<String, Object> formObject = (Map<String,Object>)json.get("form");
		assertEquals("test1",formObject.get("name"));
	}
	
	@Test
	public void testGetControllerPath() {
		get("/argument/controller_path").assertContentEquals("/argument");
	}
}
