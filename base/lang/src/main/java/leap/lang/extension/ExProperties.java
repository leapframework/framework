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
package leap.lang.extension;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import leap.lang.Props;
import leap.lang.Strings;
import leap.lang.accessor.PropertyGetter;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.convert.Converts;
import leap.lang.exception.NestedIOException;
import leap.lang.text.PlaceholderResolver;

public class ExProperties extends Properties implements PropertyGetter {

	private static final long serialVersionUID = -7225067603324341854L;
	
	/**
	 * Load a new {@link ExProperties} object from the given file.
	 */
	public static ExProperties load(File file) {
		return Props.load(file);
	}
	
	/**
	 * Load a new {@link ExProperties} object from the given file.
	 */
	public static ExProperties load(File file,Properties defaults) {
		return Props.load(file);
	}
	
	/**
	 * Creates a new {@link ExProperties} and put all the given {@link Map} properties as defaults.
	 */
	public static ExProperties create(Map<String, String> defaults){
		ExProperties p = new ExProperties();
		p.putAll(defaults);
		return p;
	}
	
	/**
	 * Creates a new {@link ExProperties} that wrapped a {@link Properties} as default values.
	 */
	public static ExProperties create(Properties properties){
		return new ExProperties(properties);
	}
	
	private File file;

	public ExProperties() {
	    super();
    }

	public ExProperties(Properties defaults) {
	    super(defaults);
    }
	
	public ExProperties(File file){
		this.file = file;
	}

	public ExProperties(File file,Properties defaults){
		super(defaults);
		this.file = file;
	}
	
	/**
	 * Returns the {@link File} stores this properties or <code>null</code> if not load from file.
	 */
	public File getFile() {
		return file;
	}
	
    public String removeProperty(String name) {
	    return (String)this.remove(name);
    }

    public boolean hasProperty(String name) {
	    return this.containsKey(name);
    }

	/**
	 * Converts this properties object to a {@link Map}.
	 */
	public Map<String, String> toMap() {
		Map<String, String> map = new LinkedHashMap<>();
		
		for(Map.Entry<Object, Object> entry : this.entrySet()){
			map.put((String)entry.getKey(),(String)entry.getValue());
		}
		
		if(null != this.defaults){
			for(Map.Entry<Object, Object> entry : this.defaults.entrySet())	{
				if(!map.containsKey(entry.getKey())){
					map.put((String)entry.getKey(),(String)entry.getValue());
				}
			}
		}
		
		return map;
	}
	
	public Map<String, String> toMap(PlaceholderResolver pr) {
		if(null == pr) {
			return toMap();
		}
		
		Map<String, String> map = new LinkedHashMap<>();
		
		for(Map.Entry<Object, Object> entry : this.entrySet()){
			map.put((String)entry.getKey(),pr.resolveString((String)entry.getValue()));
		}
		
		if(null != this.defaults){
			for(Map.Entry<Object, Object> entry : this.defaults.entrySet())	{
				if(!map.containsKey(entry.getKey())){
					map.put((String)entry.getKey(),pr.resolveString((String)entry.getValue()));
				}
			}
		}
		
		return map;
	}
	
	@Override
    public synchronized Object put(Object key, Object value) {
		if(null == value){
			value = Strings.EMPTY;
		}
	    return super.put(key, value);
    }

	@Override
    public synchronized String get(Object key) {
		return getProperty(key.toString());
	}
	
	public <T> T get(String key, Class<T> type) {
		return Props.get((PropertyGetter)this, key, type);
	}
	
	public Boolean getBoolean(String key) {
		return Props.getBoolean(this, key);
	}
	
	public boolean getBoolean(String key,boolean defaultValue) {
		return Props.getBoolean(this, key,defaultValue);
	}
	
	public Integer getInteger(String key) {
		return Props.getInteger(this, key);
	}
	
	public int getInteger(String key,int defaultValue) {
		return Props.getInteger(this, key,defaultValue);
	}

	/**
	 * Converts this properties object to a bean instance of the given class.
	 */
	public <T> T toBean(Class<T> cls) {
		return Converts.toBean(toMap(), cls);
	}
	
	/**
	 * Injects property values to the given bean as bean property.
	 */
	public void inject(Object bean){
		if(null != bean){
			BeanType bt = BeanType.of(bean.getClass());
			for(Map.Entry<Object, Object> entry : this.entrySet()){
				String name = (String)entry.getKey();
				BeanProperty bp = bt.getProperty(name);
				if(null != bp){
					bp.setValue(bean, entry.getValue());
				}
			}
		}
	}
	
	/**
	 * Saves this properties to file.
	 * 
	 * @throws IllegalStateException if this properties not load from file.
	 */
	public void save() throws IllegalStateException,NestedIOException {
		if(null == file){
			throw new IllegalStateException("Cannot save, this properties not load from file");
		}
		Props.save(this, file);
	}
	
	/**
	 * Saves this properties to the given file.
	 */
	public void save(File file) throws NestedIOException {
		Props.save(this, file);
	}
}
