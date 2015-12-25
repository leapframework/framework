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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import leap.lang.accessor.PropertyGetter;
import leap.lang.convert.Converts;
import leap.lang.exception.NestedIOException;
import leap.lang.extension.ExProperties;
import leap.lang.io.IO;
import leap.lang.resource.Resource;


/**
 * Property utils.
 */
public class Props {
	
	public static ExProperties load(Resource r) throws NestedIOException {
		try {
	        if(r.isFile()){
	        	return load(r.getFile());
	        }else{
	        	ExProperties p = new ExProperties();
	        	if(Strings.endsWithIgnoreCase(r.getFilename(), ".xml")){
	        		try(InputStream in = r.getInputStream()){
	        			p.loadFromXML(in);
	        		}
	        	}else{
	        		try(Reader reader = r.getInputStreamReader()){
	        			p.load(reader);
	        		}
	        	}
	        	return p;
	        }
        } catch (IOException e) {
        	throw new NestedIOException("Error loading properties from resource '" + r.getDescription() + "', " + e.getMessage(), e);
        }
	}	
	
	/**
	 * Reads the given properties file.
	 * 
	 * @throws NestedIOException if an I/O error occurs.
	 */
	public static ExProperties load(File file) throws NestedIOException {
		return load(file,null);
	}
	
	/**
	 * Reads the given properties file.
	 * 
	 * @throws NestedIOException if an I/O error occurs.
	 */
	public static ExProperties load(File file,Properties defaults) throws NestedIOException {
		Args.notNull(file,"file");
		try {
			ExProperties props = null == defaults ? new ExProperties(file) : new ExProperties(file,defaults);
        	if(Strings.endsWithIgnoreCase(file.getName(), ".xml")){
        		try(InputStream in = new FileInputStream(file)){
        			props.loadFromXML(in);	
        		}
        	}else{
    	        try(Reader reader = IO.createReader(file,Charset.defaultCharset())){
    	        	props.load(reader);
    	        }
        	}
        	return props;
        } catch (IOException e) {
        	throw new NestedIOException("Error loading properties file '" + file.getAbsolutePath() + "', " + e.getMessage(), e);
        }
	}
	
	/**
	 * Saving the given {@link Properties} to the given file use default charset {@link Charsets#defaultCharset()}.
	 * 
	 * @throws NestedIOException if an I/O error occurs.
	 */
	public static void save(Properties props, File file) throws NestedIOException {
		Args.notNull(props,"properties");
		Args.notNull(file,"file");

		try{
			if(!file.exists()){
				file.createNewFile();
			}
			
			if(Strings.endsWithIgnoreCase(file.getName(), ".xml")){
				try(OutputStream out = new FileOutputStream(file)){
					props.storeToXML(out, "");	
				}
			}else{
				try(Writer writer = IO.createWriter(file)){
					props.store(writer, "");
				}
			}
		}catch(IOException e){
			throw new NestedIOException("Error saving properties file '" + file.getAbsolutePath() + "', " + e.getMessage(), e);
		}
	}
	
	/**
	 * Returns the value as {@link Integer} of the given key.
	 * 
	 * <p>
	 * Returns <code>null</null> if the key not exists of the value is null or empty.
	 */
	public static Integer getInteger(Properties properties,String key){
		return get(properties,key,Integer.class);
	}	

	/**
	 * Returns the value as {@link Integer} of the given key.
	 * 
	 * <p>
	 * Returns the given default value if the key not exists of the value is null or empty.
	 */
	public static int getInteger(Properties properties,String key,int defaultValue){
		return get(properties,key,Integer.class,defaultValue);
	}
	
	/**
	 * Returns the value as {@link Boolean} of the given key.
	 * 
	 * <p>
	 * Returns <code>null</code> if the key not exists of the value is null or empty.
	 */
	public static boolean getBoolean(Properties properties,String key){
		return get(properties,key,Boolean.class);
	}	
	
	/**
	 * Returns the value as {@link Boolean} of the given key.
	 * 
	 * <p>
	 * Returns the given default value if the key not exists of the value is null or empty.
	 */
	public static boolean getBoolean(Properties properties,String key,boolean defaultValue){
		return get(properties,key,Boolean.class,defaultValue);
	}
	
	public static <T> T get(PropertyGetter properties,String key,Class<T> targetType) {
		if(null == properties || null == key){
			return null;
		}
		
		String value = properties.getProperty(key.toString());

		if(null == value || Strings.isEmpty((value = value.trim()))){
			return null;
		}
		
		return Converts.convert(value, targetType);
	}
	
	public static <T> T get(PropertyGetter properties,String key,Class<T> targetType, T defaultValue) {
		if(null == properties || null == key){
			return defaultValue;
		}
		
		String value = properties.getProperty(key.toString());

		if(null == value || Strings.isEmpty((value = value.trim()))){
			return defaultValue;
		}
		
		return Converts.convert(value, targetType);
	}
	
	public static <T> T get(Properties properties,String key,Class<T> targetType) {
		if(null == properties || null == key){
			return null;
		}
		
		String value = properties.getProperty(key);

		if(null == value || Strings.isEmpty((value = value.trim()))){
			return null;
		}
		
		return Converts.convert(value, targetType);
	}
	
	public static <T> T get(Properties properties,String key,Class<T> targetType, T defaultValue) {
		if(null == properties || null == key){
			return defaultValue;
		}
		
		String value = properties.getProperty(key);

		if(null == value || Strings.isEmpty((value = Strings.trim(value)))){
			return defaultValue;
		}
		
		return Converts.convert(value, targetType);
	}
	
	public static <T> T remove(Properties properties,String key,Class<T> targetType) {
		if(null == properties || null == key){
			return null;
		}
		
		Object value = properties.remove(key);

		if(null == value || (value instanceof String && Strings.isEmpty((String)(value = ((String)value).trim())))){
			return null;
		}
		
		return Converts.convert(value, targetType);
	}
	
	public static <T> T remove(Properties properties,String key,Class<T> targetType, T defaultValue) {
		if(null == properties || null == key){
			return defaultValue;
		}
		
		Object value = properties.remove(key);

		if(null == value || (value instanceof String && Strings.isEmpty((String)(value = ((String)value).trim())))){
			return defaultValue;
		}
		
		return Converts.convert(value, targetType);
	}	
	
	/**
	 * Merge the given Properties instance into the given Map,
	 * copying all properties (key-value pairs) over.
	 * <p>Uses {@code Properties.propertyNames()} to even catch
	 * default properties linked into the original Properties instance.
	 * @param props the Properties instance to merge (may be {@code null})
	 * @param map the target Map to merge the properties into
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	//from spring framework 3.2.6 under Apache License 2.0.
	public static void mergePropertiesIntoMap(Properties props, Map map) {
		if (map == null) {
			throw new IllegalArgumentException("Map must not be null");
		}
		if (props != null) {
			for (Enumeration en = props.propertyNames(); en.hasMoreElements();) {
				String key = (String) en.nextElement();
				Object value = props.getProperty(key);
				if (value == null) {
					// Potentially a non-String value...
					value = props.get(key);
				}
				map.put(key, value);
			}
		}
	}
	
	/**
	 * Converts the {@link Properties} to a {@link Map} object.
	 */
	public static Map<String, String> toMap(Properties props) {
		Map<String, String> map = new LinkedHashMap<>();
		
		if(null != props) {
			for(Map.Entry<Object, Object> entry : props.entrySet()){
				map.put((String)entry.getKey(),(String)entry.getValue());
			}
		}
		
		return map;
	}
	
	protected Props(){
		
	}
}
