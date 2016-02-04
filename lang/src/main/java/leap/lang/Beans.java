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
package leap.lang;

import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.beans.DynaBean;
import leap.lang.convert.Converts;
import leap.lang.exception.ObjectNotFoundException;

import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;

/**
 * java bean utils
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Beans {
	
	public static final String SETTER_PREFIX = "set";
	public static final String GETTER_PREFIX = "get";
	public static final String IS_PREFIX     = "is";
	
	/**
	 * Check if the given type represents a "simple" property:
	 * a primitive, a String or other CharSequence, a Number, a Date,
	 * a URI, a URL, a Locale, a Class, or a corresponding array.
	 * <p>Used to determine properties to check for a "simple" dependency-check.
	 * @param clazz the type to check
	 * @return whether the given type represents a "simple" property
	 */
	public static boolean isSimpleProperty(Class<?> clazz) {
		Args.notNull(clazz, "class");
		return Classes.isSimpleValueType(clazz) || (clazz.isArray() && Classes.isSimpleValueType(clazz.getComponentType()));
	}
	
	public static boolean isSetterMethod(String methodName){
		return null != methodName && 
			 methodName.startsWith(SETTER_PREFIX) && methodName.length() > SETTER_PREFIX.length() &&
			 Character.isUpperCase(methodName.charAt(SETTER_PREFIX.length()));
	}
	
	public static boolean isGetterMethod(String methodName){
		return null != methodName &&
				methodName.startsWith(GETTER_PREFIX) && methodName.length() > GETTER_PREFIX.length() &&
				Character.isUpperCase(methodName.charAt(GETTER_PREFIX.length()));
	}
	
	public static String extractPropertyFromSetter(String methodName) {
		String propName = methodName.substring(SETTER_PREFIX.length());
		
		if(propName.length() == 1){
			return propName.toLowerCase();
		}else{
			return Character.toLowerCase(propName.charAt(0)) + propName.substring(1);	
		}
	}
	
	public static String extraPropertyFromGetter(String methodName){
		String propName = methodName.substring(GETTER_PREFIX.length());
		
		if(propName.length() == 1){
			return propName.toLowerCase();
		}else{
			return Character.toLowerCase(propName.charAt(0)) + propName.substring(1);
		}
	}
	
    public static Map<String,Object> toMap(Object bean){
		if(null == bean){
			return new HashMap<>();
		}else if(bean instanceof Map){
			return (Map)bean;
		}else if(bean instanceof DynaBean){
			return ((DynaBean) bean).getProperties();
		}else{
			return BeanType.of(bean.getClass()).toMap(bean);
		}
	}
    
    public static void copyProperties(Object from,Object to) {
    	if(null == from || null == to){
    		return;
    	}
    	
    	boolean toDyna = to instanceof DynaBean; 
    	if(toDyna){
    		DynaBean toDynaBean = (DynaBean)to;
    		boolean  fromDyna   = from instanceof DynaBean;
    		
    		if(fromDyna) {
    			for(Entry<String, Object> entry : ((DynaBean)from).getProperties().entrySet()){
    				toDynaBean.setProperty(entry.getKey(),entry.getValue());
    			}
    		}else{
    			BeanType fromBeanType = BeanType.of(from.getClass());
    			for(BeanProperty bp : fromBeanType.getProperties()){
    				toDynaBean.setProperty(bp.getName(), bp.getValue(from));
    			}
    		}
    	}else{
        	BeanType toBeanType = BeanType.of(to.getClass());
        	
        	boolean fromDyna = from instanceof DynaBean; 
        	if(fromDyna){
        		DynaBean fromDynaBean = (DynaBean)from;
        		for(BeanProperty bp : toBeanType.getProperties()){
        			if(bp.isWritable()){
        				bp.setValue(to, fromDynaBean.getProperty(bp.getName()));
        			}
        		}
        	}else{
            	BeanType fromBeanType = BeanType.of(from.getClass());
            	for(BeanProperty toBp : toBeanType.getProperties()){
            		if(toBp.isWritable()){
            			BeanProperty fromBp = fromBeanType.tryGetProperty(toBp.getName());
            			if(null != fromBp){
            				toBp.setValue(to, fromBp.getValue(from));
            			}
            		}
            	}
        	}
    	}
    }
    
    /**
     * @throws ObjectNotFoundException if the given property not exists in the given bean.
     */
    public static Object getProperty(BeanType beanType,Object bean,String property) throws ObjectNotFoundException{
    	if(bean instanceof DynaBean){
    		return ((DynaBean) bean).getProperty(property);
    	}
    	
    	BeanProperty bp = beanType.tryGetProperty(property);
    	if(null != bp){
    		return bp.getValue(bean);
    	}
    	
    	throw new ObjectNotFoundException("Property '" + property + "' not found in bean class '" + bean.getClass().getName());
    }
	
	public static Object getNestableProperty(Object bean,String property){
		if(bean instanceof Map){
			return getNestableProperty((Map)bean, property);
		}else if(bean instanceof DynaBean){
			return getNestablePropety(((DynaBean) bean).getProperties(), property);
		}else{
			return getNestableProperty(BeanType.of(bean.getClass()), bean, property);
		}
	}
	
	public static Object getNestablePropety(Map map,String property){
		if(map.containsKey(property)){
			return map.get(property);
		}
		
		int dotIndex = property.indexOf('.');
		if(dotIndex > 0){
			String namePrefix = property.substring(0,dotIndex);
			
			Object nestedBean = map.get(namePrefix);
			if(null == nestedBean){
				return null;
			}else{
				return getNestableProperty(nestedBean, property.substring(dotIndex+1));
			}
		}else if(property.charAt(property.length() - 1) == ']'){
			int index1 = property.indexOf('[');
			
			if(index1 > 0){
				String namePrefix = property.substring(0,index1);
				
				Object nestedBean = map.get(namePrefix);
				
				if(null == nestedBean){
					//TODO : throw IndexOutOfBoundsException?
					return null;
				}else{
					int arrayIndex = Integer.parseInt(property.substring(index1 + 1, property.length() - 2));
					return getArrayProperty(nestedBean, arrayIndex);
				}
			}else{
				throw new IllegalArgumentException("Invalid arrray property name '" + property + ", must be 'property[index]', i.e. 'values[0]'");
			}
		}else{
			return null;
		}
	}
	
	public static Object getNestableProperty(BeanType beanType, Object bean, String property){
		int dotIndex = property.indexOf('.');
		
		if(dotIndex > 0){
			String namePrefix = property.substring(0,dotIndex);
			
			BeanProperty bp = beanType.getProperty(namePrefix);
			
			return getNestedProperty(beanType, bean, bp, property.substring(dotIndex+1));
		}else if(property.charAt(property.length() - 1) == ']'){
			int index1 = property.indexOf('[');
			
			if(index1 > 0){
				String namePrefix = property.substring(0,index1);
				
				Object nestedBean = beanType.getProperty(namePrefix).getValue(bean);
				
				if(null == nestedBean){
					//TODO : throw IndexOutOfBoundsException?
					return null;
				}else{
					int arrayIndex = Integer.parseInt(property.substring(index1 + 1, property.length() - 2));
					return getArrayProperty(nestedBean, arrayIndex);
				}
			}else{
				throw new IllegalArgumentException("Invalid arrray property '" + property + ", must be 'property[index]', i.e. 'values[0]'");
			}
		}else{
			return beanType.getProperty(property).getValue(bean);
		}
	}
	
    public static <T> T fromMapNestable(BeanType beanType, Map<String, Object> map) {
    	return setPropertiesNestableInternal(beanType, null, map);
    }
	
	protected static Object getNestedProperty(BeanType beanType,Object bean,BeanProperty bp,String nestedProperty){
		Class<?> nestedBeanClass = bp.getType();
		
		if(Map.class.isAssignableFrom(nestedBeanClass)){
			return getNestedMapProperty(beanType, nestedBeanClass, bp, nestedProperty);
		}
		
		Object nestedBean = bp.getValue(bean);
		
		if(null == nestedBean){
			return null;
		}
		
		return getNestableProperty(BeanType.of(nestedBeanClass), nestedBean, nestedProperty);
	}
	
    protected static Object getNestedMapProperty(BeanType beanType,Object bean,BeanProperty bp,String nestedProperty) {
		Map map = (Map)bp.getValue(bean);

		if(null == map){
			return null;
		}
		
		if(map.containsKey(nestedProperty)){
			return map.get(nestedProperty);
		}
		
		return getNestableProperty(map,nestedProperty);
	}
    
    public static void setProperties(BeanType beanType,Object bean,Map<String, Object> map){
    	for(Entry<String, Object> entry : map.entrySet()){
    		String name = entry.getKey();
    		BeanProperty bp = beanType.tryGetProperty(name);
    		if(null != bp){
    			bp.setValue(bean, entry.getValue());
    		}
    	}
    }
    
	public static void setPropertiesNestable(BeanType beanType, Object bean, Map<String, Object> map) {
		Args.notNull(bean,"bean");
		setPropertiesNestableInternal(beanType, bean, map);
	}
	
	private static <T> T setPropertiesNestableInternal(BeanType beanType, T bean, Map<String, Object> map) {
	    
	    Map mapBean = null;
	    boolean isMap = bean instanceof Map;
	    if(isMap) {
	        mapBean = (Map)bean;
	    }
	    
		for(Entry<String, Object> entry : map.entrySet()){
			String name = entry.getKey();
			
			BeanProperty bp = beanType.tryGetProperty(name);
			if(null == bp){
				int dotIndex = name.indexOf('.');
				
				if(dotIndex > 0){
					String namePrefix = name.substring(0,dotIndex);
					
					bp = beanType.tryGetProperty(namePrefix);
					
					if(null != bp){
						if(null == bean){
							bean = beanType.newInstance();
						}
						
						setNestedProperty(beanType, bean, bp, name.substring(dotIndex+1), entry.getValue());
					}
					
				}else if(name.charAt(name.length() - 1) == ']') {
					int index1 = name.indexOf('[');
					
					if(index1 > 0){
						String namePrefix = name.substring(0,index1);
						
						bp = beanType.tryGetProperty(namePrefix);
						
						if(null != bp){
							if(null == bean){
								bean = beanType.newInstance();
							}
							if(index1 + 1 > name.length() - 1){
								Object value = entry.getValue();
								bp.setValue(bean, value);
							}else{
								String p = name.substring(index1 + 1, name.length() - 1).trim();
								try{
									int arrayIndex = Integer.parseInt(p);
									setArrayProperty(beanType, bean, bp, arrayIndex, entry.getValue());
								}catch(NumberFormatException e) {
									bp.setValue(bean, entry.getValue());
								}
							}
						}
					}else{
						throw new IllegalArgumentException("Invalid arrray property name '" + name + ", must be 'property[index]', i.e. 'values[0]'");
					}
				}else if(isMap) {
				    mapBean.put(name, entry.getValue());
				}
				
				continue;
			}else{
				if(null == bean){
					bean = beanType.newInstance();
				}
				
				Object value = entry.getValue();
				if(null == value) {
				    bp.setValue(bean, null);
				}else{
				    Class<?> valueType = value.getClass();
				    
				    if(valueType.isArray()) {
				        int length = Array.getLength(value);
				        for(int i=0;i<length;i++) {
				            setArrayProperty(beanType, bean, bp, i, Array.get(value, i));
				        }
				        continue;
				    }
				    
				    if(Iterable.class.isAssignableFrom(valueType)) {
				        int i=0;
				        for(Object item : (Iterable)value) {
				            setArrayProperty(beanType, bean, bp, i, item);
				            i++;
				        }
				        continue;
				    }
				    
				    bp.setValue(bean, value);
				}
			}
		}
		
		return bean;
	}

    public static void setProperty(BeanType beanType,Object bean,String property,Object value){
    	setProperty(beanType, bean, property, value, false);
    }
    
    public static void setProperty(BeanType beanType,Object bean,String property,Object value, boolean ignorecase){
    	if(ignorecase){
    		if(beanType.trySetIgnoreCase(bean, property, value)){
    			return;
    		}
    	}else{
    		if(beanType.trySet(bean, property, value)){
    			return;
    		}
    	}
    	
    	if(bean instanceof DynaBean){
    		((DynaBean) bean).setProperty(property, value);
    	}
    }
    
	protected static void setNestedProperty(BeanType beanType,Object bean,BeanProperty bp,String nestedPropertyName,Object value){
		Class<?> nestedBeanClass = bp.getType();
		
		if(Map.class.isAssignableFrom(nestedBeanClass)){
			setNestedMapProperty(beanType, bean, bp, nestedPropertyName, value);
			return;
		}
		
		BeanType nestedBeanType = BeanType.of(nestedBeanClass);
		
		BeanProperty nestedProperty = nestedBeanType.tryGetProperty(nestedPropertyName);
		
		if(null == nestedProperty){
			int dotIndex = nestedPropertyName.indexOf('.');
			
			if(dotIndex > 0){
				String namePrefix = nestedPropertyName.substring(0,dotIndex);
				
				nestedProperty = nestedBeanType.tryGetProperty(namePrefix);
				
				if(null != nestedProperty){
					Object nestedBean = nestedProperty.getValue(bean);
					
					if(null == nestedBean){
						nestedBean = nestedBeanType.newInstance();
						nestedProperty.setValue(bean, nestedBean);
					}
					
					setNestedProperty(nestedBeanType, nestedBean, nestedProperty, nestedPropertyName.substring(dotIndex+1), value);
				}
			}else if(nestedPropertyName.charAt(nestedPropertyName.length() - 1) == ']') {
				int index1 = nestedPropertyName.indexOf('[');
				
				if(index1 > 0){
					
					String namePrefix = nestedPropertyName.substring(0,index1);
					
					nestedProperty = nestedBeanType.tryGetProperty(namePrefix);
					
					if(null != nestedProperty){
						int arrayIndex = Integer.parseInt(nestedPropertyName.substring(index1 + 1, nestedPropertyName.length() - 2));
						
						Object nestedBean = nestedProperty.getValue(bean);
						
						if(null == nestedBean){
							nestedBean = nestedBeanType.newInstance();
							nestedProperty.setValue(bean, nestedBean);
						}
						
						setArrayProperty(nestedBeanType, nestedBean, nestedProperty, arrayIndex, value);
					}
				}else{
					throw new IllegalArgumentException("Invalid arrray property name '" + nestedPropertyName + ", must be 'property[index]', i.e. 'values[0]'");
				}
			}
			
		}else{
			Object nestedBean = nestedProperty.getValue(bean);
			
			if(null == nestedBean){
				nestedBean = nestedBeanType.newInstance();
				nestedProperty.setValue(bean, nestedBean);
			}
			
			nestedProperty.setValue(nestedBean, value);
		}
	}
	
    protected static void setNestedMapProperty(BeanType beanType,Object bean,BeanProperty bp,String nestedPropertyName,Object value){
		Map map = (Map)bp.getValue(bean);
		
		if(null == map){
			Class<?> type = bp.getType();
			if(type.equals(Map.class) || type.equals(HashMap.class)){
				map = new HashMap();
			} else if(type.equals(LinkedHashMap.class)){
				map = new LinkedHashMap();
			}else{
				throw new IllegalStateException("Map type '" + type.getName() + "' not supported in class '" + beanType.getBeanClass().getName() + "");
			}
			bp.setValue(bean, map);
		}
		
		map.put(nestedPropertyName, value);
	}
	
	protected static Object getArrayProperty(Object object,int index){
		return Enumerables.of(object).get(index);
	}
	
	protected static void setArrayProperty(BeanType beanType,Object bean,BeanProperty bp,int index,Object value){
		if(bp.isWritable()){
			Object propertyVal = bp.getValue(bean);
			Class<?> type = bp.getType();
			if(propertyVal == null){
				if(type.isAssignableFrom(List.class)){
					ArrayList arr = New.arrayList();
					tryIncreaseSize(arr,index);
					arr.set(index, Converts.convert(value, bp.getElementType()));
					bp.setValue(bean, arr);
				}else if(type.isArray()){
				    if(type.getComponentType().isPrimitive()) {
                        Object arr =  Array.newInstance(type.getComponentType(), index+1);
                        Array.set(arr, index, Converts.convert(value, type.getComponentType()));
                        bp.setValue(bean, arr);
				    }else{
	                    Object[] arr =  New.array(type.getComponentType(), index+1);
	                    arr[index] = Converts.convert(value, type.getComponentType());
	                    bp.setValue(bean, arr);
				    }
				}else{
					throw new  IllegalStateException(Strings.format("bean property type '{0}' is not an array or list", bp.getName()));
				}
			}else{
				if(type.isAssignableFrom(List.class)){
					List<Object> arr = (List<Object>)propertyVal;
					tryIncreaseSize(arr,index);
					arr.set(index, Converts.convert(value, bp.getElementType()));
				}else if(type.isArray()){
				    if(type.getComponentType().isPrimitive()) {
	                    Object arr = propertyVal;
	                    int len = Array.getLength(arr);
	                    if(len <= index){
	                        List<Object> newArr = New.arrayList();
	                        for(int i=0;i<len;i++){
	                            newArr.add(Array.get(arr,i));
	                        }
	                        tryIncreaseSize(newArr,index);
	                        newArr.set(index, Converts.convert(value, type.getComponentType()));
	                        bp.setValue(bean, newArr.toArray());
	                    }else{
	                        Array.set(arr, index, value);
	                    }
				    }else{
	                    Object[] arr = (Object[])propertyVal;
	                    if(arr.length <= index){
	                        List<Object> newArr = New.arrayList();
	                        for(Object obj : arr){
	                            newArr.add(obj);
	                        }
	                        tryIncreaseSize(newArr,index);
	                        newArr.set(index, Converts.convert(value, type.getComponentType()));
	                        bp.setValue(bean, newArr.toArray());
	                    }else{
	                        arr[index] = value;
	                    }
				    }
				}else{
					throw new  IllegalStateException(Strings.format("bean property type '{0}' is not an array or list", bp.getName()));
				}
			}
		}else{
			throw new IllegalStateException(Strings.format("Array property {0} is not writable", bp.getName()));
		}
	}
	
    protected static void tryIncreaseSize(List list, int maxIndex) {
    	if(maxIndex >= list.size()) {
    		for(int i=list.size();i<=maxIndex;i++) {
    			list.add(null);
    		}
    	}
	}
	
	protected Beans(){
		
	}
}