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

import leap.lang.Arrays2;
import leap.lang.Assert;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.json.JSON;
import leap.lang.json.JsonWriter;
import leap.web.Request;
import leap.web.action.ControllerBase;
import leap.web.annotation.Arguments;
import leap.web.annotation.Path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgumentController extends ControllerBase {
	
	public String int1(int value){
		return String.valueOf(value);
	}
	
	public String int2(Integer intValue){
		return String.valueOf(intValue);
	}
	
	public String intArray1(int[] value){
		return Converts.toString(value);
	}
	
	public String intArray2(Integer[] value){
		return Converts.toString(value);
	}
	
	public String beanArray(Item[] items) {
		return JSON.stringify(items);
	}
	
	public String beanArray1(Item[] items0,Item[] items1) {
		return JSON.stringify(Arrays2.concat(items0, items1));
	}
	
	public String beanList(List<Item> items) {
		return JSON.stringify(items);
	}
	
	public String mapArray(Map<String,Object>[] items) {
		return JSON.stringify(items);
	}
	
	public String mapList(List<Map<String,String>> items) {
		return JSON.stringify(items);
	}
	
    public String mapModelList(List<MapModel> items) {
        return JSON.stringify(items);
    }	

    public String collectionModel(CollecitonModel cm) {
        Assert.notNull(cm);
        Assert.notNull(cm.getSimpleModels());
        Assert.isTrue(cm.getSimpleModels().size() > 0);
        
        SimpleModel sm = cm.getSimpleModels().get(0);
        Assert.notEmpty(sm.getName(), "name must not be empty");
        
        return JSON.stringify(cm);
    }
    
	@Path("path_var/{id}")
	public void pathVar(String id){
		text(id); 
	}
	
	@Path("path_var1/{TEST_ID}")
	public void pathVar1(String testId) {
		text(testId);
	}

    @Path("args_bean/{id}")
    public void argsBean(@Arguments ArgsBean bean) {
        Assert.notNull(bean);
        Assert.notNull(bean.request);
        Assert.isFalse(Strings.isEmpty(bean.id));
        Assert.isFalse(Strings.isEmpty(bean.name));
    }

    @Path("args_bean1/{id}/{parent_id}")
    public void argsBean1(ArgsBean1 bean) {
        Assert.notNull(bean);
        Assert.notNull(bean.request);
        Assert.isFalse(Strings.isEmpty(bean.id));
        Assert.isFalse(Strings.isEmpty(bean.parentId));
        Assert.isFalse(Strings.isEmpty(bean.name));
    }
	
	public String testForm(TestForm form){
		return JSON.stringify(form);
	}
	
	public String testForm1(String name, TestForm form){
		JsonWriter json = JSON.createWriter();
		
		json.startObject()
		    .property("name", name)
		    .property("form", form)
		    .endObject();
		
		return json.toString();
	}
	
	public String controllerPath() {
		return Request.current().getActionContext().getRoute().getControllerPath();
	}
	
	public static final class SimpleModel {
	    protected String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
	}
	
	public static final class CollecitonModel {
	    protected List<SimpleModel> simpleModels;

        public List<SimpleModel> getSimpleModels() {
            return simpleModels;
        }

        public void setSimpleModels(List<SimpleModel> simpleModels) {
            this.simpleModels = simpleModels;
        }
	}
	
	public static final class Item {
		
		private String        strValue;
		private Integer       intValue;
		private int[]	      intArray;
		private int[][]		  intArrays;
		private List<Integer> intList;
		private Item[]        itemArray;
		private List<Item>    itemList;
		
		public String getStrValue() {
			return strValue;
		}

		public void setStrValue(String strValue) {
			this.strValue = strValue;
		}

		public Integer getIntValue() {
			return intValue;
		}

		public void setIntValue(Integer intValue) {
			this.intValue = intValue;
		}
		
		public int[] getIntArray() {
			return intArray;
		}

		public void setIntArray(int[] intArray) {
			this.intArray = intArray;
		}
		
		public int[][] getIntArrays() {
			return intArrays;
		}

		public void setIntArrays(int[][] intArrays) {
			this.intArrays = intArrays;
		}

		public List<Integer> getIntList() {
			return intList;
		}

		public void setIntList(List<Integer> intList) {
			this.intList = intList;
		}

		public Item[] getItemArray() {
			return itemArray;
		}

		public void setItemArray(Item[] itemArray) {
			this.itemArray = itemArray;
		}

		public List<Item> getItemList() {
			return itemList;
		}

		public void setItemList(List<Item> itemList) {
			this.itemList = itemList;
		}
	}
	
	public static final class TestForm {
		
		private String		        name;
		private int[]			    intArray;
		private Item				item;
		private Map<String, Object>	testParameters	= new HashMap<String, Object>();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
		public int[] getIntArray() {
			return intArray;
		}

		public void setIntArray(int[] intArray) {
			this.intArray = intArray;
		}

		public Item getItem() {
			return item;
		}

		public void setItem(Item item) {
			this.item = item;
		}

		public Map<String, Object> getTestParameters() {
			return testParameters;
		}

		public void setTestParameters(Map<String, Object> testParameters) {
			this.testParameters = testParameters;
		}
	}
	
	public static final class MapModel extends HashMap<String, Object> {

        private static final long serialVersionUID = -8042407294075758985L;

        public MapModel() {
            super();
            put("__key", "__v");
        }
        
	}

	public static class ArgsBean {
        public Request request;
        public String  id;
        public String  name;
    }

    @Arguments
    public static final class ArgsBean1{
        public Request request;
        public String  id;
        public String  parentId;
        public String  name;
    }
}