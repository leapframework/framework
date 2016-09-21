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
package leap.lang.xml;

import javax.xml.namespace.QName;

import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.text.PlaceholderResolver;

import java.util.Iterator;
import java.util.function.BiConsumer;

public abstract class XmlReaderBase implements XmlReader {
	
	protected Object              source              = "unknow";
	protected PlaceholderResolver placeholderResolver = null;
	protected boolean			  trimAll			  = true;
	
	@Override
    public Object getSource() {
	    return source;
    }

	@Override
    public String getCurrentLocation() {
	    return "line " + getLineNumber() + ", element '" + getElementLocalName() + "' in " + (null == source ? "[unknow source]" : source ) ;
    }
	
	@Override
    public void setTrimAll(boolean trimAll) {
		this.trimAll = trimAll;
    }

	@Override
    public boolean isTrimAll() {
	    return trimAll;
    }

	@Override
    public void setPlaceholderResolver(PlaceholderResolver placeholderResolver) {
		this.placeholderResolver = placeholderResolver;
    }

	@Override
    public PlaceholderResolver getPlaceholderResolver() {
	    return placeholderResolver;
    }

	@Override
    public boolean nextWhileNotEnd(QName elementName) {
		if(isEndElement(elementName)){
			return false;
		}
	    return next();
    }

	@Override
    public boolean nextWhileNotEnd(String elementLocalName) {
		if(isEndElement(elementLocalName)){
			return false;
		}
	    return next();
    }
	
	@Override
    public boolean nextToStartElement() {
		while(next()){
			if(isStartElement()){
				return true;
			}
		}
	    return false;
    }

	@Override
    public boolean nextToStartElement(String localName) {
		while(next()){
			if(isStartElement(localName)){
				return true;
			}
		}
	    return false;
    }

	@Override
    public boolean nextToStartElement(QName name) {
		while(next()){
			if(isStartElement(name)){
				return true;
			}
		}
	    return false;
    }
	
	@Override
    public boolean nextToEndElement() {
		if(!isStartElement()) {
			throw new IllegalStateException("Must be start element");
		}
		
		int counter = 1;
		
		while(next()) {
			
			if(isStartElement()) {
				counter++;
				continue;
			}
			
			if(isEndElement()) {
				counter--;
				continue;
			}
			
			if(counter == 0) {
				return true;
			}
		}
		
		return false;
    }
	
	@Override
    public boolean nextToEndElement(String localName) {
		while(next()){
			if(isEndElement(localName)){
				return true;
			}
		}
	    return false;
    }

	@Override
    public boolean nextToEndElement(QName name) {
		while(next()){
			if(isEndElement(name)){
				return true;
			}
		}
	    return false;
    }
	
	@Override
    public final String getElementTextAndEnd() {
		String text = doGetElementTextAndEnd();
	    return trimAll ? Strings.trim(text) : text;
    }
	
	@Override
    public String getRequiredElementTextAndEnd() {
		String value = getElementTextAndEnd();
		if(Strings.isEmpty(value)){
			throw new IllegalStateException("Element text must not be empty, location: " + getCurrentLocation());
		}
		return value;
    }
	

	@Override
    public final String getAttribute(QName name) {
		String value = doGetAttribute(name);
	    return trimAll ? Strings.trim(value) : value;
    }

	@Override
    public final String getAttribute(String localName) {
		String value = doGetAttribute(localName);
	    return trimAll ? Strings.trim(value) : value;
    }
	
    public String getAttributeOrNull(QName name) {
		String value = doGetAttribute(name);
	    return null == value ? null : (trimAll ? Strings.trim(value) : value);
    }

	@Override
    public String getAttributeOrNull(String localName) {
		String value = doGetAttribute(localName);
	    return null == value ? null : (trimAll ? Strings.trim(value) : value);
    }

	@Override
    public String getAttribute(QName name, String defaultValue) {
		String value = getAttribute(name);
	    return Strings.isEmpty(value) ? defaultValue : value;
    }

	@Override
    public String getAttribute(String localName, String defaultValue) {
		String value = getAttribute(localName);
	    return Strings.isEmpty(value) ? defaultValue : value;
    }
	
	@Override
    public String getRequiredAttribute(QName name) {
		String value = getAttribute(name);
		if(Strings.isEmpty(value)){
			throw new IllegalStateException("Attribute '" + name + "' must not be empty, location : " + getCurrentLocation());
		}
	    return value;
    }
	
	public String getRequiredAttribute(String localName) {
		String value = getAttribute(localName);
		if(Strings.isEmpty(value)){
			throw new IllegalStateException("Attribute '" + localName + "' must not be empty, location : " + getCurrentLocation());
		}		
	    return value;
    }
	
	@Override
    public <T> T getAttribute(QName name, Class<T> targetType) {
		String value = getAttribute(name);
	    return Strings.isEmpty(value) ? null : Converts.convert(value, targetType);
    }

	@Override
    public <T> T getAttribute(String localName, Class<T> targetType) {
		String value = getAttribute(localName);
	    return Strings.isEmpty(value) ? null : Converts.convert(value, targetType);
    }

	@Override
    public <T> T getAttribute(QName name, Class<T> targetType, T defaultValue) {
		String value = getAttribute(name);
	    return Strings.isEmpty(value) ? defaultValue : Converts.convert(value, targetType);
    }

	@Override
    public <T> T getAttribute(String localName, Class<T> targetType, T defaultValue) {
		String value = getAttribute(localName);
	    return Strings.isEmpty(value) ? defaultValue : Converts.convert(value, targetType);
    }

	@Override
    public <T> T getRequiredAttribute(QName name, Class<T> targetType) {
		return Converts.convert(getRequiredAttribute(name),targetType);
    }

	@Override
    public <T> T getRequiredAttribute(String localName, Class<T> targetType) {
		return Converts.convert(getRequiredAttribute(localName),targetType);
    }

	public Boolean getBooleanAttribute(QName name) {
		String value = getAttribute(name);
		return Strings.isEmpty(value) ? null : Converts.toBoolean(value);
    }

	public Boolean getBooleanAttribute(String name) {
		String value = getAttribute(name);
		return Strings.isEmpty(value) ? null : Converts.toBoolean(value);
    }

	public boolean getBooleanAttribute(QName name, boolean defaultValue) {
		String value = getAttribute(name);
		return Strings.isEmpty(value) ? defaultValue : Converts.toBoolean(value);
    }

	public boolean getBooleanAttribute(String name, boolean defaultValue) {
		String value = getAttribute(name);
		return Strings.isEmpty(value) ? defaultValue : Converts.toBoolean(value);
    }
	
	@Override
    public boolean getRequiredBooleanAttribute(QName name) {
		return Converts.toBoolean(getRequiredAttribute(name));
    }

	@Override
    public boolean getRequiredBooleanAttribute(String localName) {
	    return Converts.toBoolean(getRequiredAttribute(localName));
    }	
	
	public Integer getIntegerAttribute(QName name) {
		String value = getAttribute(name);
		return Strings.isEmpty(value) ? null : Converts.toInt(value);
    }
	
	public Integer getIntegerAttribute(String name) {
		String value = getAttribute(name);
		return Strings.isEmpty(value) ? null : Converts.toInt(value);
    }
	
	public int getIntAttribute(QName name, int defaultValue) {
		String value = getAttribute(name);
		return Strings.isEmpty(value) ? defaultValue : Converts.toInt(value);
    }
	
	public int getIntAttribute(String name, int defaultValue) {
		String value = getAttribute(name);
		return Strings.isEmpty(value) ? defaultValue : Converts.toInt(value);
    }

    @Override
    public Float getFloatAttribute(QName name) {
        String value = getAttribute(name);
        return Strings.isEmpty(value) ? null : Converts.convert(value, Float.class);
    }

    @Override
    public Float getFloatAttribute(String localName) {
        String value = getAttribute(localName);
        return Strings.isEmpty(value) ? null : Converts.convert(value, Float.class);
    }

    @Override
    public float getFloatAttribute(QName name, float defaultValue) {
        String value = getAttribute(name);
        return Strings.isEmpty(value) ? defaultValue : Converts.convert(value, Float.class);
    }

    @Override
    public float getFloatAttribute(String localName, float defaultValue) {
        String value = getAttribute(localName);
        return Strings.isEmpty(value) ? defaultValue : Converts.convert(value, Float.class);
    }

    @Override
    public int getRequiredIntAttribute(QName name) {
	    return Converts.toInt(getRequiredAttribute(name));
    }

	@Override
    public int getRequiredIntAttribute(String localName) {
	    return Converts.toInt(getRequiredAttribute(localName));
    }
	
	private final String resolve(String value){
		return null == placeholderResolver ? value : placeholderResolver.resolveString(value);
	}
	
	@Override
    public String resolveElementTextAndEnd() {
	    return resolve(getElementTextAndEnd());
    }

	@Override
    public String resolveRequiredElementTextAndEnd() {
	    return resolve(getRequiredElementTextAndEnd());
    }

	@Override
    public String resolveAttribute(QName name) {
	    return resolve(getAttribute(name));
    }

	@Override
    public String resolveAttribute(String localName) {
	    return resolve(getAttribute(localName));
    }

	@Override
    public String resolveAttribute(QName name, String defaultValue) {
	    return resolve(getAttribute(name, defaultValue));
    }

	@Override
    public String resolveAttribute(String localName, String defaultValue) {
	    return resolve(getAttribute(localName, defaultValue));
    }

	@Override
    public String resolveRequiredAttribute(QName name) {
	    return resolve(getRequiredAttribute(name));
    }

	@Override
    public String resolveRequiredAttribute(String localName) {
	    return resolve(getRequiredAttribute(localName));
    }
	
	@Override
    public <T> T resolveAttribute(QName name, Class<T> targetType) {
		String value = resolveAttribute(name);
	    return Strings.isEmpty(value) ? null : Converts.convert(value, targetType);
    }

	@Override
    public <T> T resolveAttribute(String localName, Class<T> targetType) {
		String value = resolveAttribute(localName);
	    return Strings.isEmpty(value) ? null : Converts.convert(value, targetType);
    }

	@Override
    public <T> T resolveAttribute(QName name, Class<T> targetType, T defaultValue) {
		String value = resolveAttribute(name);
	    return Strings.isEmpty(value) ? defaultValue : Converts.convert(value, targetType);
    }

	@Override
    public <T> T resolveAttribute(String localName, Class<T> targetType, T defaultValue) {
		String value = resolveAttribute(localName);
	    return Strings.isEmpty(value) ? defaultValue : Converts.convert(value, targetType);
    }

	@Override
    public <T> T resolveRequiredAttribute(QName name, Class<T> targetType) {
		return Converts.convert(resolveRequiredAttribute(name), targetType);
	}

	@Override
    public <T> T resolveRequiredAttribute(String localName, Class<T> targetType) {
		return Converts.convert(resolveRequiredAttribute(localName), targetType);
    }

	@Override
    public Boolean resolveBooleanAttribute(QName name) {
	    String value = resolveAttribute(name);
	    return Strings.isEmpty(value) ? null : Converts.toBoolean(value);
    }

	@Override
    public Boolean resolveBooleanAttribute(String localName) {
	    String value = resolveAttribute(localName);
	    return Strings.isEmpty(value) ? null : Converts.toBoolean(value);
    }

	@Override
    public boolean resolveBooleanAttribute(QName name, boolean defaultValue) {
	    String value = resolveAttribute(name);
	    return Strings.isEmpty(value) ? defaultValue : Converts.toBoolean(value);
    }

	@Override
    public boolean resolveBooleanAttribute(String localName, boolean defaultValue) {
	    String value = resolveAttribute(localName);
	    return Strings.isEmpty(value) ? defaultValue : Converts.toBoolean(value);
    }

	@Override
    public boolean resolveRequiredBooleanAttribute(QName name) {
	    return Converts.toBoolean(resolveRequiredAttribute(name));
    }

	@Override
    public boolean resolveRequiredBooleanAttribute(String localName) {
		return Converts.toBoolean(resolveRequiredAttribute(localName));
    }

	@Override
    public Integer resolveIntegerAttribute(QName name) {
		String value = resolveAttribute(name);
		return Strings.isEmpty(value) ? null : Converts.toInt(value);
    }

	@Override
    public Integer resolveIntegerAttribute(String localName) {
		String value = resolveAttribute(localName);
		return Strings.isEmpty(value) ? null : Converts.toInt(value);
    }

	@Override
    public int resolveIntAttribute(QName name, int defaultValue) {
		String value = resolveAttribute(name);
		return Strings.isEmpty(value) ? defaultValue : Converts.toInt(value);
    }

	@Override
    public int resolveIntAttribute(String localName, int defaultValue) {
		String value = resolveAttribute(localName);
		return Strings.isEmpty(value) ? defaultValue : Converts.toInt(value);
    }

    @Override
    public float resolveFloatAttribute(QName name, int defaultValue) {
        String value = resolveAttribute(name);
        return Strings.isEmpty(value) ? defaultValue : Converts.convert(value, Float.class);
    }

    @Override
    public float resolveFloatAttribute(String localName, int defaultValue) {
        String value = resolveAttribute(localName);
        return Strings.isEmpty(value) ? defaultValue : Converts.convert(value, Float.class);
    }

    @Override
    public int resolveRequiredIntAttribute(QName name) {
		return Converts.toInt(resolveRequiredAttribute(name));
    }

	@Override
    public int resolveRequiredIntAttribute(String localName) {
		return Converts.toInt(resolveRequiredAttribute(localName));
    }

    @Override
    public void forEachResolvedAttributes(BiConsumer<QName, String> func) {
        Iterator<QName> names = getAttributeNames();
        while(names.hasNext()) {
            QName  name  = names.next();
            String value = resolveAttribute(name);
            func.accept(name, value);
        }
    }

    @Override
    public void forEachAttributes(BiConsumer<QName, String> func) {
        Iterator<QName> names = getAttributeNames();
        while(names.hasNext()) {
            QName  name  = names.next();
            String value = getAttribute(name);
            func.accept(name, value);
        }
    }

    protected abstract String doGetElementTextAndEnd();
	
	protected abstract String doGetAttribute(QName name);
	
	protected abstract String doGetAttribute(String localName);	
}
