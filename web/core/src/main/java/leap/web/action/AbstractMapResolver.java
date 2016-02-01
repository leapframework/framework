/*
 * Copyright 2014 the original author or authors.
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
package leap.web.action;

import leap.web.App;
import leap.web.route.RouteBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "rawtypes", "unchecked" }) 
public abstract class AbstractMapResolver extends AbstractArgumentResolver {

	public AbstractMapResolver(App app, RouteBase route, Argument arg) {
	    super(app, route, arg);
    }
	
	protected void putToMap(Map map, String name, Object value) {
		int dotIndex         = name.indexOf('.');
		int leftBracketIndex = name.indexOf('[');
		
		if(dotIndex > 0 && (leftBracketIndex < 0 || dotIndex < leftBracketIndex)) {
			putToMap(map, name, dotIndex, value);
			return;
		}else if(leftBracketIndex > 0 && (dotIndex < 0 || leftBracketIndex < dotIndex)) {
			int rightBracketIndex = name.indexOf(']', leftBracketIndex);
			if(rightBracketIndex > leftBracketIndex + 1) {
				putToMap(map, name, leftBracketIndex, rightBracketIndex, value);
				return;
			}
		}
		
		map.put(name,value);
	}
	
	protected void putToMap(Map map, String name, int dotIndex, Object value) {
		String objectName = name.substring(0,dotIndex);
		String nestedName = name.substring(dotIndex + 1);
		
		Map object = (Map)map.get(objectName);
		if(null == object){
			object = new HashMap();
			map.put(objectName,object);
		}
		
		putToMap(object,nestedName,value);
	}
	
	protected void putToMap(Map map, String name, int leftBracketIndex, int rightBracketIndex,Object value) {
		String objectName = name.substring(0,leftBracketIndex);
		String nestedKey  = name.substring(leftBracketIndex + 1,rightBracketIndex).trim();
		
		try{
			int arrayIndex = Integer.parseInt(nestedKey); 
			
			List list = (List)map.get(objectName);
			if(null == list){
				list = new ArrayList();
				map.put(objectName, list);
			}
			
			tryIncreaseSize(list,arrayIndex);
			
			String nestedName = leftBracketIndex == name.length() - 1 ? null : name.substring(rightBracketIndex + 1);
			
			addToList(list, arrayIndex, nestedName, value);
		}catch(NumberFormatException e){
			Map object = (Map)map.get(objectName);
			if(null == object){
				object = new HashMap();
				map.put(objectName,object);
			}
			putToMap(object,nestedKey,value);
		}
	}
	
    protected void addToList(List list, int arrayIndex, String nestedName, Object value) {
		if(nestedName == null || nestedName.length() == 0) {
			list.set(arrayIndex, value);
		}else{
			//nestedName : .a || [index] || [name]
			char c0 = nestedName.charAt(0);
			if(c0 == '.'){
				tryIncreaseSize(list,arrayIndex);
				Map nestedMap = (Map)list.get(arrayIndex);
				if(null == nestedMap){
					nestedMap = new HashMap();
					list.set(arrayIndex,nestedMap);
				}
				putToMap(nestedMap, nestedName.substring(1), value);
			}else if(c0 == '[') {
				int leftBracketIndex  = 0;
				int rightBracketIndex = nestedName.indexOf(']', 1);
				
				if(rightBracketIndex > 1) {
					addToListNested(list, arrayIndex, nestedName, leftBracketIndex, rightBracketIndex, value);
				}else{
					//TODO : illegal key
				}
			}
		}
	}
	
    protected void addToListNested(List list, int arrayIndex, String name, int leftBracketIndex, int rightBracketIndex , Object value) {
		String nestedKey = name.substring(leftBracketIndex + 1,rightBracketIndex).trim();
		try{
			int nestedArrayIndex = Integer.parseInt(nestedKey);
			List nestedList = (List)list.get(arrayIndex);
			if(null == nestedList){
				nestedList = new ArrayList();
				list.set(arrayIndex, nestedList);
			}
			
			tryIncreaseSize(nestedList, nestedArrayIndex);
			
			String nestedName = leftBracketIndex == name.length() - 1 ? null : name.substring(rightBracketIndex + 1);
			addToList(nestedList, nestedArrayIndex, nestedName, value);
		}catch(NumberFormatException e) {
			tryIncreaseSize(list, arrayIndex);
			Map nestedNestedObject = (Map)list.get(arrayIndex);
			if(null == nestedNestedObject){
				nestedNestedObject = new HashMap();
				list.set(arrayIndex,nestedNestedObject);
			}
			putToMap(nestedNestedObject, nestedKey, value);
		}
	}
	
    protected static void tryIncreaseSize(List list, int maxIndex) {
    	if(maxIndex >= list.size()) {
    		for(int i=list.size();i<=maxIndex;i++) {
    			list.add(null);
    		}
    	}
	}
}