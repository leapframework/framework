package leap.lang.json;
/*
 * Copyright 2010 the original author or authors.
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


import leap.lang.*;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.codec.Base64;
import leap.lang.naming.NamingStyle;
import leap.lang.time.DateFormats;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Time;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class JsonWriterImpl implements JsonWriter {
	
	private static final Integer zero = new Integer(0);
    
    static final char[] HEX_CHARS = new char[]{
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    }; 

    private final JsonSettings settings;
    private final Appendable   out;
    private final DateFormat   dateFormat;
	private final boolean      detectCyclicReferences;
	private final boolean	   ignoreCyclicReferences;
	private final int		   maxDepth;

	private boolean							startProperty;
	private int							    depth;
	private IdentityHashMap<Object,Integer> references;
	
	public JsonWriterImpl(JsonSettings settings,
                          Appendable out,
                          boolean detectCyclicReferences,
                          boolean ignoreCyclicReferences,
            			  int maxDepth) {
	    super();

        this.settings               = settings;
	    this.out 			        = out;
        this.dateFormat             = settings.getDateFormat();
	    this.detectCyclicReferences = detectCyclicReferences;
	    this.ignoreCyclicReferences = ignoreCyclicReferences;
	    this.maxDepth				= depth <= 0 ? MAX_DEPTH : maxDepth;

	    if(detectCyclicReferences) {
	    	references = new IdentityHashMap<>();
	    }
    }
	
	@Override
    public NamingStyle getNamingStyle() {
	    return settings.getNamingStyle();
    }
	
	@Override
    public int getMaxDepth() {
	    return maxDepth;
    }

	@Override
    public boolean isKeyQuoted() {
	    return settings.isKeyQuoted();
    }

	@Override
    public boolean isIgnoreEmptyString() {
	    return settings.isIgnoreEmptyString();
    }

	@Override
    public boolean isIgnoreEmptyArray() {
	    return settings.isIgnoreEmptyArray();
    }

	@Override
    public boolean isIgnoreFalse() {
	    return settings.isIgnoreFalse();
    }

	@Override
    public boolean isIgnoreNull() {
	    return settings.isIgnoreNull();
    }

	@Override
    public boolean isDetectCyclicReferences() {
	    return detectCyclicReferences;
    }

	@Override
    public boolean isIgnoreCyclicReferences() {
	    return ignoreCyclicReferences;
    }

	public JsonWriter startObject() {
        try {
    		out.append(OPEN_OBJECT);
    		startProperty = true;
        } catch (IOException e) {
        	wrapAndThrow(e);
        }	
        return this;
    }
	
	public JsonWriter startObject(String key) {
	    return key(key).startObject();
    }
	
	@Override
    public JsonWriter propertyIgnorable(String key, Object v) {
		if(isIgnoreNull() && null == v) {
			return this;
		}
		
		if(isIgnoreEmptyString()){
			if(v instanceof CharSequence) {
				CharSequence cs = (CharSequence)v;
				if(cs.length() == 0) {
					return this;
				}else{
					return key(key).value(cs.toString());
				}
			}
		}
		
		if(isIgnoreEmptyArray()) {
			if(v instanceof Object[]) {
				Object[] a = (Object[])v;
				if(a.length == 0){
					return this;
				}else{
					return key(key).array(a);
				}
			}
			
			if(v instanceof Iterable) {
				Iterator<?> it = ((Iterable<?>)v).iterator();
				if(!it.hasNext()) {
					return this;
				}else{
					return key(key).array(it);
				}
			}
			
			if(v.getClass().isArray()) {
				int i = Array.getLength(v);
				if(i == 0) {
					return this;
				}else{
					return key(key).objectArray(v);
				}
			}
		}
		
		if(isIgnoreFalse()) {
			if(v instanceof Boolean) {
				if(v == Boolean.FALSE) {
					return this;
				}else{
					return property(key,(Boolean)v);
				}
			}
		}
		
	    return property(key, v);
    }
	
	@Override
    public JsonWriter propertyIgnorable(String key, String s) {
		if(isIgnoreNull() && null == s) {
			return this;
		}
		
		if(isIgnoreEmptyString() && s.length() == 0){
			return this;
		}
		
	    return property(key, s);
    }

	@Override
    public JsonWriter propertyIgnorable(String key, boolean b) {
		if(isIgnoreFalse() && !b){
			return this;
		}
	    return property(key, b);
    }

	public JsonWriter property(String key,String stringValue) {
		if(settings.isNullToEmptyString() && null == stringValue) {
			stringValue = "";
		}
		return key(key).value(stringValue);
	}
	
	public JsonWriter property(String key,boolean boolValue) {
		return key(key).value(boolValue);
	}
	
	public JsonWriter property(String key,byte byteValue) {
		return key(key).value(byteValue);
	}
	
	public JsonWriter property(String key,short shortValue) {
		return key(key).value(shortValue);
	}
	
	public JsonWriter property(String key,int intValue) {
		return key(key).value(intValue);
	}
	
	public JsonWriter property(String key,long longValue) {
		return key(key).value(longValue);
	}
	
	public JsonWriter property(String key,float floatValue) {
		return key(key).value(floatValue);
	}
	
	public JsonWriter property(String key,double doubleValue) {
		return key(key).value(doubleValue);
	}
	
	public JsonWriter property(String key,BigDecimal decimalValue) {
		return key(key).value(decimalValue);
	}
	
	public JsonWriter property(String key, Number numberValue) {
	    return key(key).value(numberValue) ;
    }

	public JsonWriter property(String key,Date dateValue) {
		return key(key).value(dateValue);
	}
	
	@Override
    public JsonWriter property(String key, Object v) {
	    return key(key).value(v);
    }

    @Override
    public JsonWriter property(String key, Map v) {
        return key(key).map(v);
    }

    public JsonWriter endObject() {
        try {
    		out.append(CLOSE_OBJECT);
    		startProperty = false;
        } catch (IOException e) {
        	wrapAndThrow(e);
        }		
        return this;
    }
	
	public JsonWriter array(Date... array) {
		startArray();
		if(null != array) {
		    int len = array.length;
	        for(int i=0;i<len;i++){
	            if(i > 0){
	                separator();
	            }
	            value(array[i]);
	        }
		}
		endArray();
	    return this;
    }

	@Override
	public JsonWriter arrayString(Iterable<String> array) {
        startArray();
        if(null != array) {
            Iterator<String> it = array.iterator();
            int i=0;
            while(it.hasNext()) {
                if(i > 0) {
                    separator();
                }else{
                    i++;
                }
                value(it.next());
            }
        }
        endArray();
		return this;
	}

	public JsonWriter array(double... array) {
		startArray();
		if(null != array) {
		    int len = array.length;
	        for(int i=0;i<len;i++){
	            if(i > 0){
	                separator();
	            }
	            value(array[i]);
	        }
		}
		endArray();
	    return this;
    }

	public JsonWriter array(float... array) {
		startArray();
		if(null != array) {
		    int len = array.length;
	        for(int i=0;i<len;i++){
	            if(i > 0){
	                separator();
	            }
	            value(array[i]);
	        }
		}
		endArray();
	    return this;
    }

	public JsonWriter array(Number... array) {
		startArray();
		if(null != array) {
		    int len = array.length;
	        for(int i=0;i<len;i++){
	            if(i > 0){
	                separator();
	            }
	            value(array[i]);
	        }
		}
		endArray();
	    return this;
    }

	public JsonWriter array(short... array) {
		startArray();
		if(null != array) {
		    int len = array.length;
	        for(int i=0;i<len;i++){
	            if(i > 0){
	                separator();
	            }
	            value(array[i]);
	        }
		}
		endArray();
	    return this;
    }

	public JsonWriter array(int... array) {
		startArray();
		if(null != array) {
		    int len = array.length;
	        for(int i=0;i<len;i++){
	            if(i > 0){
	                separator();
	            }
	        }
		}
		endArray();
	    return this;
    }

	public JsonWriter array(long... array) {
		startArray();
		if(null != array) {
		    int len = array.length;
	        for(int i=0;i<len;i++){
	            if(i > 0){
	                separator();
	            }
	            value(array[i]);
	        }
		}
		endArray();
	    return this;
    }

	public JsonWriter array(String... array) {
	    startArray();
		if(null != array) {
		    int len = array.length;
	        for(int i=0;i<len;i++){
	            if(i > 0){
	                separator();
	            }
	            value(array[i]);
	        }
		}
		endArray();
	    return this;
    }
	
	public JsonWriter array(Iterable<?> array) {
		return array(null == array ? (Iterator<?>)null : array.iterator());
    }
	
	public JsonWriter array(Iterator<?> array) {
		startArray();
		if(null != array) {
			int i=0;
			while(array.hasNext()) {
				if(i > 0) {
					separator();
				}else{
					i++;
				}
				value(array.next());
			}
		}
		endArray();
	    return this;
    }
	
	public JsonWriter array(Object[] array) {
		startArray();
		if(null != array) {
			int len = array.length;
			for(int i=0;i<len;i++){
				if(i > 0){
					separator();
				}
				value(array[i]);
			}
		}
		endArray();
	    return this;
    }
	
	@Override
    public JsonWriter objectArray(Object array) throws IllegalStateException {
		startArray();
		if(null != array) {
			if(!array.getClass().isArray()) {
				throw new IllegalStateException("The given object is not an array");
			}
			int len = Array.getLength(array);
			for(int i=0;i<len;i++){
				if(i > 0){
					separator();
				}
				value(Array.get(array, i));
			}
		}
		endArray();
	    return this;
    }

	public JsonWriter arrayIgnoreEmptyItem(String... array) {
		startArray();
		if(null != array) {
		    int len = array.length;
	        for(int i=0;i<len;i++){
	            String s = array[i];
	            
	            if(Strings.isEmpty(s)){
	                continue;
	            }
	            
	            if(i > 0){
	                separator();
	            }
	            value(s);
	        }
		}
		endArray();
	    return this;
    }

	public JsonWriter startArray() {
        try {
    		out.append(OPEN_ARRAY);
        } catch (IOException e) {
        	wrapAndThrow(e);
        }		
        return this;
    }
	
	public JsonWriter startArray(String key) {
	    return key(key).startArray();
    }

	public JsonWriter endArray() {
        try {
    		out.append(CLOSE_ARRAY);
        } catch (IOException e) {
        	wrapAndThrow(e);
        }	
        return this;
    }
	
	public JsonWriter value(boolean bool) {
        try {
	        out.append(String.valueOf(bool));
        } catch (IOException e) {
        	wrapAndThrow(e);
        }
        return this;
    }
    
	public JsonWriter value(byte b) {
        try {
        	out.append(String.valueOf(b));
        } catch (IOException e) {
        	wrapAndThrow(e);
        }
        return this;
    }
	
	public JsonWriter value(char c) {
    	return value(String.valueOf(c));
    }
    
	public JsonWriter value(byte[] bytes) {
        try {
        	if(null == bytes || bytes.length == 0){
        		out.append(EMPTY_STRING);
        	}else{
        		out.append(DOUBLE_QUOTE)
        		   .append(Base64.encode(bytes))
        		   .append(DOUBLE_QUOTE);
        	}
        } catch (IOException e) {
        	wrapAndThrow(e);
        }
        return this;
    }
	
	public JsonWriter value(short s) {
		return raw(String.valueOf(s));
    }
	
	public JsonWriter value(int i) {
		return raw(String.valueOf(i));
    }
	
	public JsonWriter value(long l) {
		return raw(String.valueOf(l));
    }
	
	public JsonWriter value(float f) {
		return raw(String.valueOf(f));
    }
	
	public JsonWriter value(double d) {
		return raw(String.valueOf(d));
    }
	
	public JsonWriter value(BigDecimal decimal) {
        try {
	        out.append(null == decimal ? NULL_STRING : decimal.toString());
        } catch (IOException e) {
        	wrapAndThrow(e);
        }
        return this;
    }
    
	public JsonWriter value(Number number) {
        try {
	        out.append(null == number ? NULL_STRING : String.valueOf(number));
        } catch (IOException e) {
        	wrapAndThrow(e);
        }
        return this;
    }
    
	public JsonWriter value(Time time) {
        try {
            if(null == time) {
                out.append(NULL_STRING);
            }else{
                value(time.toLocalTime());
            }
        } catch (IOException e) {
        	wrapAndThrow(e);
        }
        return this;
    }

	public JsonWriter value(Date date) {
        try {
            if(null == date) {
                out.append(NULL_STRING);
            }else if(null != dateFormat){
				out.append(DOUBLE_QUOTE).append(dateFormat.format(date)).append(DOUBLE_QUOTE);
            }else{
                out.append(String.valueOf(date.getTime()));
            }
        } catch (IOException e) {
        	wrapAndThrow(e);
        }
        return this;
    }

    @Override
    public JsonWriter value(LocalDate date) {
        try {
			if(null == date) {
				out.append(NULL_STRING);
			}else {
				out.append(DOUBLE_QUOTE).append(date.toString()).append(DOUBLE_QUOTE);
			}
        } catch (IOException e) {
            wrapAndThrow(e);
        }
        return this;
    }

    @Override
    public JsonWriter value(LocalTime time) {
        try {
			if(null == time) {
				out.append(NULL_STRING);
			}else {
				out.append(DOUBLE_QUOTE).append(time.toString()).append(DOUBLE_QUOTE);
			}
        } catch (IOException e) {
            wrapAndThrow(e);
        }
        return this;
    }

    @Override
    public JsonWriter value(LocalDateTime dateTime) {
        try {
			if(null == dateTime) {
				out.append(NULL_STRING);
			}else {
				out.append(DOUBLE_QUOTE).append(dateTime.toString()).append(DOUBLE_QUOTE);
			}
        } catch (IOException e) {
            wrapAndThrow(e);
        }
        return this;
    }

    public JsonWriter key(String key) {
        try {
        	if(startProperty){
        		startProperty = false;
        	}else{
        		out.append(COMMA_CHAR);
        	}
        	
        	if(isKeyQuoted()){
        		out.append(DOUBLE_QUOTE).append(key).append(DOUBLE_QUOTE);
        	}else{
        		out.append(key);	
        	}
        	out.append(CLOSE_KEY);
        } catch (IOException e) {
        	wrapAndThrow(e);
        }
        return this;
    }
	
	@Override
    public JsonWriter keyUseNamingStyle(String key) {
	    return key(getNamingStyle().of(key));
    }

    public JsonWriter value(String string) {
        try {
            if (string == null) {
            	out.append(NULL_STRING);
            }else if(string.length() == 0){
            	out.append(EMPTY_STRING);
            }else{
                char c   = 0;
                int  len = string.length();

                out.append(DOUBLE_QUOTE);
                for (int i = 0; i < len; i++) {
                    c = string.charAt(i);
                    switch (c) {
                    case '\\':
                        out.append("\\\\");
                        break;
                    case '"':
                    	out.append("\\\"");
                        break;
                    case '\b':
                    	out.append("\\b");
                        break;
                    case '\t':
                    	out.append("\\t");
                        break;
                    case '\n':
                    	out.append("\\n");
                        break;
                    case '\f':
                    	out.append("\\f");
                        break;
                    case '\r':
                    	out.append("\\r");
                        break;
                    default:
                    	out.append(c);
                    }
                }
                out.append(DOUBLE_QUOTE);
            }
        } catch (IOException e) {
        	wrapAndThrow(e);
        }
        
        return this;
    }
	
    public JsonWriter null_() {
        try {
        	out.append(NULL_STRING);
        } catch (IOException e) {
        	wrapAndThrow(e);
        }
        return this;
    }
    
    @SuppressWarnings("rawtypes")
    public JsonWriter value(Object v) {
    	return value(v, this::bean);
    }

    protected JsonWriter value(Object v, Consumer<Object> beanWriter) {
        depth++;
        if(depth == maxDepth) {
            throw new JsonException("Exceed max depth " + depth);
        }

        try{
            if (null == v) {
                return null_();
            } else if (v instanceof String) {
                return value((String)v);
            } else if (v instanceof Byte) {
                return value(((Byte) v).byteValue());
            } else if (v instanceof Boolean) {
                return value(((Boolean) v).booleanValue());
            } else if (v instanceof Character) {
                return value(((Character) v).charValue());
            } else if (v instanceof Number) {
                return value((Number) v);
            } else if (v instanceof Time) {
                return value((Time) v);
            } else if (v instanceof Date) {
                return value((Date) v);
            }else if (v instanceof LocalDate) {
                return value((LocalDate) v);
            }else if (v instanceof LocalDateTime) {
                return value((LocalDateTime) v);
            }else if (v instanceof LocalTime) {
                return value((LocalTime) v);
            } else if (v instanceof Class<?>) {
                return value(((Class<?>) v).getName());
            } else if (v instanceof byte[]){
                return value((byte[])v);
            } else if(v instanceof Enum<?>){
                return value(Enums.getValue((Enum<?>)v));
            } else if(v instanceof Object[]) {
                return array((Object[])v);
            } else if(v instanceof Iterable) {
                return array((Iterable<?>)v);
            } else if(v instanceof Iterator) {
                return array((Iterator<?>)v);
            } else if(v.getClass().isArray()) {
                return objectArray(v);
            } else if(v instanceof JsonStringable){
                ((JsonStringable) v).toJson(this);
                return this;
            } else if(v instanceof Map) {
                return map((Map)v);
            } else {
                if(detectCyclicReferences) {
                    if(references.containsKey(v)) {

                        if(ignoreCyclicReferences) {
                            return null_(); //TODO : write null for cyclic reference
                        }

                        throw new JsonException("Found cyclic reference : " + v.toString());

                    }

                    references.put(v, zero);
                }

                beanWriter.accept(v);

                if(detectCyclicReferences) {
                    references.remove(v);
                }

                return this;
            }
        }finally{
            depth--;
        }
    }
	
    @Override
    @SuppressWarnings("rawtypes")
    public JsonWriter map(Map map) {
		if(null == map) {
			return null_();
		}else{
			startObject();
			for(Object item : map.entrySet()) {
				Entry  entry = (Entry)item;
				String key = ns(entry.getKey().toString());
				Object val = entry.getValue();
				if(isIgnoreNull() && val == null){
					continue;
				}
				if(val != null){
					if(val instanceof String){
						if(isIgnoreEmptyString() && Strings.isEmpty((String)val)){
							continue;
						}
					}
					if(val.getClass().isArray()){
						if(isIgnoreEmptyArray() && Arrays2.isEmpty((Object[])val)){
							continue;
						}
					}
					if(val instanceof Boolean){
						if(isIgnoreFalse() && Objects.equals(val,Boolean.FALSE)){
							continue;
						}
					}
				}
				if(settings.isNullToEmptyString() && val == null) {
					val = "";
				}
				property(key, val);
			}
			endObject();
		}
	    return this;
    }

	@Override
    public JsonWriter bean(Object bean) {
		return bean(bean, null);
    }

    protected JsonWriter bean(Object bean, JsonType type) {
        if(null == bean) {
            return null_();
        }else{
            startObject();

            try {
                BeanType beanType = BeanType.of(bean.getClass());

                //process type metadata.
                if(null == type && !bean.getClass().isInterface()) {
                    type = bean.getClass().getSuperclass().getAnnotation(JsonType.class);
                }

                if(null != type) {
                    String metaPropertyName = Strings.firstNotEmpty(type.property(),
                                                                    type.meta().getDefaultPropertyName());

                    //todo : cache
                    if(type.meta() == JsonType.MetaType.CLASS_NAME) {
                        property(metaPropertyName, bean.getClass().getName());
                    }else {
                        boolean typed = false;
                        for(JsonType.SubType subType : type.types()) {
                            if(subType.type().equals(bean.getClass())) {
                                typed = true;

                                if(!beanType.hasProperty(metaPropertyName)) {
                                    property(metaPropertyName, subType.name());
                                }

                                break;
                            }
                        }
                        if(!typed) {
                            throw new JsonException("No type name has been defined for class '" + bean.getClass() + "' in super class");
                        }
                    }
                }

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

                        Object propValue;

                        if(jsonField != null && jsonField.useGetter() == false){
                            propValue = prop.getReflectField().getValue(bean,false);
                        }else{
                            propValue = prop.getValue(bean);
                        }

                        if(null == propValue && isIgnoreNull()){
                            continue;
                        }

                        if(isIgnoreEmptyString() && Strings.isNullOrBlank(propValue)){
                            continue;
                        }
                        if(prop.getField().getType().equals(String.class) && settings.isNullToEmptyString() && null == propValue) {
                        	propValue = "";
						}
                        keyUseNamingStyle(propName);

                        if(!writeDateValue(prop, propValue)) {

                            //todo : performance
                            value(propValue, (v) -> {
                                bean(v,prop.getType().getAnnotation(JsonType.class));
                            });
                        }
                    }
                }
            } catch (JsonException e){
                throw e;
            } catch (Exception e) {
                throw new JsonException("Error writing json value : " + bean.getClass().getName(), e);
            }

            return endObject();
        }
    }

    protected boolean writeDateValue(BeanProperty bp, Object value) {

        if(value instanceof Date) {
            JsonFormat a = bp.getAnnotation(JsonFormat.class);

            if(null == a) {
                value((Date)value);
            }else{
                DateFormat dateFormat = DateFormats.getFormat(a.value());
                value(dateFormat.format((Date)value));
            }

            return true;
        }

        return false;
    }

	public JsonWriter separator() {
		try {
	        out.append(COMMA_CHAR);
        } catch (IOException e) {
        	wrapAndThrow(e);
        }
        return this;
    }
	
    public JsonWriter raw(String string){
    	try {
	        out.append(string);
        } catch (IOException e) {
        	wrapAndThrow(e);
        }
        return this;
    }
    
    @Override
    public String toString() {
	    return out.toString();
    }
    
    protected String ns(String s) {
    	return getNamingStyle().of(s);
    }
    
	private void wrapAndThrow(IOException e){
		throw new JsonException(e.getMessage(),e);
	}
}
