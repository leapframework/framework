/*
 * Copyright 2012 the original author or authors.
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
package leap.lang.json;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Map;

import leap.lang.Args;
import leap.lang.Enums;
import leap.lang.Exceptions;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.beans.DynaBean;
import leap.lang.exception.NestedIOException;
import leap.lang.reflect.ReflectClass;

class JsonEncoderImpl implements JsonEncoder {
	
	private static final Integer zero = new Integer(0);
	private static final int MAX_DEEP = 100;
    
    private final IdentityHashMap<Object,Integer> references = new IdentityHashMap<Object, Integer>();
    private final Object						  value;
    private final JsonSettings				      settings ;
    
    private int	deep = 0;

    public JsonEncoderImpl(Object value){
        this(value, JsonSettings.MAX);
    }
    
    public JsonEncoderImpl(Object value, JsonSettings settings){
    	this.value    = value;
    	this.settings = settings;
    }
    
    @Override
    public void encode(Appendable out) {
    	Args.notNull(out,"out");
    	if(null == value){
    		try {
	            out.append(JsonWriter.NULL_STRING);
            } catch (IOException e) {
            	throw Exceptions.wrap(e);
            }
    	}else{
    		JsonWriter writer = JSON.writer(out)
    								.setIgnoreNull(settings.isIgnoreNull())
    								.setKeyQuoted(settings.isKeyQuoted())
                                    .setNamingStyle(settings.getNamingStyle())
    								.create();
    		
    		if(value instanceof JsonStringable) {
    			((JsonStringable) value).toJson(writer);
    		}else{
        		encode(null,value,writer);
        		references.clear();
    		}
    	}
    }
    
    @Override
    public String encodeToString() throws NestedIOException {
        Appendable out = new StringBuilder();
    	encode(out);
	    return out.toString();
    }

	private void encode(String name,Object value, JsonWriter writer) {
    	deep++;
    	
    	if(deep >= MAX_DEEP){
    		throw new JsonException(
    				Strings.format("stack size reach '{0}', please check your object , may be some getter generate new object at every call",deep));
    	}
    	
        if (null == value) {
            writer.null_();
        } else if (value instanceof String) {
            writer.value((String) value);
        } else if (value instanceof Byte) {
            writer.value(((Byte) value).byteValue());
        } else if (value instanceof Boolean) {
            writer.value(((Boolean) value).booleanValue());
        } else if (value instanceof Character) {
            writer.value(((Character) value).charValue());
        } else if (value instanceof Number) {
            writer.value((Number) value);
        } else if (value instanceof Date) {
            writer.value((Date) value);
        } else if (value instanceof Class<?>) {
            writer.value(((Class<?>) value).getName());
        } else {
            //detect cyclic references
            if(references.containsKey(value)){
            	return;
            }
        	
        	references.put(value, zero);
        	
            if (value instanceof Object[]) {
                encode(name,(Object[]) value, writer);
            } else if (value.getClass().isArray()) {
                encodeArray(name,value, writer);
            } else if (value instanceof Map<?, ?>) {
                encode(name,(Map<?, ?>) value, writer);
            } else if (value instanceof Iterable<?>) {
                encode(name,(Iterable<?>) value, writer);
            } else if (value instanceof Enumeration<?>) {
                encode(name,(Enumeration<?>) value, writer);
            } else if (value instanceof Enum<?>) {
                encode(name,Enums.getValue(((Enum<?>) value)), writer);
            } else if (value instanceof DynaBean) {
            	encode(name, ((DynaBean) value).getProperties(), writer);
            } else {
                encodeBean(name,value, writer);
            }
            
            references.remove(value);
        }
        
        deep--;
    }

    private void encode(String name,Object[] array, JsonWriter writer) {
        writer.startArray();
        for (int i = 0; i < array.length; i++) {
        	Object value = array[i];
        	
            //detect cyclic references
            if(references.containsKey(value)){
            	continue;
            }
            
            if (i > 0) {
                writer.separator();
            }
            
            encode(name,array[i], writer);
        }
        writer.endArray();
    }

    private void encodeArray(String name,Object array, JsonWriter writer) {
    	ReflectClass rc = ReflectClass.of(array.getClass().getComponentType());
    	
        writer.startArray();
        
        int len = rc.getArrayLength(array);
        for (int i = 0; i < len; i++) {
        	Object value = rc.getArrayItem(array, i);
        	
            //detect cyclic references
//            if(references.containsKey(value)){
//            	continue;
//            }
            
            if (i > 0) {
                writer.separator();
            }
            
            encode(name,value, writer);
        }
        
        writer.endArray();
    }
    
    private void encode(String name,Iterable<?> iterable, JsonWriter writer) {
        writer.startArray();
        
        int index = 0;
        for (Object value : iterable) {
            //detect cyclic references
//            if(references.containsKey(value)){
//            	continue;
//            }
            
            if (index == 0) {
                index++;
            } else {
                writer.separator();
            }
            
            encode(name,value, writer);
        }
        
        writer.endArray();
    }

    private void encode(String name,Enumeration<?> enumeration, JsonWriter writer) {
        writer.startArray();
        
        int index = 0;
        while (enumeration.hasMoreElements()) {
        	
        	Object value = enumeration.nextElement();
        	
            //detect cyclic references
//            if(references.containsKey(value)){
//            	continue;
//            }
        	
            if (index == 0) {
                index++;
            } else {
                writer.separator();
            }
            
            encode(name,value, writer);
        }
        writer.endArray();
    }

    private void encode(String name,Map<?, ?> map, JsonWriter writer) {
        writer.startObject();

        for (Object key : map.keySet()) {
            String prop = String.valueOf(key);
            Object propValue = map.get(key);
            
            if(null == propValue && settings.isIgnoreNull()){
                continue;
            }
            
            if(settings.isIgnoreEmpty() && (propValue instanceof String) && ((String)propValue).trim().equals("")){
                continue;
            }
            
            //detect cyclic references
//            if(references.containsKey(propValue)){
//            	continue;
//            }            

            encodeNamedValue(prop, map.get(key), writer);
        }

        writer.endObject();
    }

    private void encodeBean(String name,Object bean, JsonWriter writer) {
        writer.startObject();
        
        try {
        	BeanType beanType = BeanType.of(bean.getClass());

            for(BeanProperty prop : beanType.getProperties()){
            	if(prop.isTransient()){
            		continue;
            	}
            	
            	if(!prop.isReadable() || !prop.isField()){
            		continue;
            	}
            	
            	JsonField jsonField = prop.getAnnotation(JsonField.class);
            	
                if(null != jsonField || !prop.isAnnotationPresent(JsonIgnore.class)){
                    String propName = prop.getName();
                    
                    JsonName named = prop.getAnnotation(JsonName.class);
                    
                    if(null != named){
                    	propName = named.value();
                    }
                    
                    Object propValue = prop.getValue(bean);
                    
                    if(null == propValue && settings.isIgnoreNull()){
                        continue;
                    }
                    
                    if(settings.isIgnoreEmpty() && Strings.isNullOrBlank(propValue)){
                        continue;
                    }
                    
                    //detect cyclic references
//                    if(references.containsKey(propValue)){
//                    	continue;
//                    }                    
                    
                    encodeNamedValue(propName, propValue, writer);
                }
            }
        } catch (JsonException e){
        	throw e;
        } catch (Exception e) {
            throw new JsonException("error encoding for value : " + bean.getClass().getName(), e);
        }
        
        writer.endObject();
    }

    private void encodeNamedValue(String name, Object value, JsonWriter writer) {
        writer.keyUseNamingStyle(name);
        encode(name,value, writer);
    }
}