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
package tested.beans;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("rawtypes")
public class TBean implements TBeanType1, TBeanType2, TBeanType3 {
	
	protected String       string;
	protected int          int1;
	protected int          int2;
	protected NopBean      simpleBean;
	protected List<String> listString;
	protected List<Object> listObject;
	protected Properties   properties;
    protected Map          map;
    protected Collection   collection;
    protected TBean        definedBean;

	public TBean(){
		
	}
	
	public TBean(String string){
		this.string = string;
	}
	
	public TBean(List<Object> listObject, Map map){
		this.map = map;
		this.listObject =listObject;
	}
	
	public TBean(Map map){
		this.map = map;
	}
	
	public TBean(TBean definedBean){
		this.definedBean = definedBean;
	}
	
	public TBean(TBean definedBean, TBean definedBean1){
		this.definedBean = definedBean;
	}
	
	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}
	
	public int getInt1() {
		return int1;
	}

	public void setInt1(int int1) {
		this.int1 = int1;
	}
	
	public int getInt2() {
		return int2;
	}

	public void setInt2(int int2) {
		this.int2 = int2;
	}

	public NopBean getSimpleBean() {
		return simpleBean;
	}

	public void setSimpleBean(NopBean simpleBean) {
		this.simpleBean = simpleBean;
	}

	public List<String> getListString() {
		return listString;
	}

	public void setListString(List<String> listString) {
		this.listString = listString;
	}
	
	public List<Object> getListObject() {
		return listObject;
	}

	public void setListObject(List<Object> listObject) {
		this.listObject = listObject;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}

	public Collection getCollection() {
		return collection;
	}

	public void setCollection(Collection collection) {
		this.collection = collection;
	}

	public TBean getDefinedBean() {
		return definedBean;
	}

	public void setDefinedBean(TBean definedBean) {
		this.definedBean = definedBean;
	}
}
