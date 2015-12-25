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
package leap.lang.edm;

import leap.lang.Enumerable;
import leap.lang.Enumerables;
import leap.lang.Iterables;
import leap.lang.Predicates;

public class EdmEntityType extends EdmNamedStructualType {
	
	private final String             fullQualifiedName;
	private final EdmEntityType   	 baseType;
	private final boolean 		     hasStream;
	private final boolean			 openType;
	private final Enumerable<String> keys;
	private final Enumerable<EdmNavigationProperty> navigationProperties;
	
	public EdmEntityType(String name,
						 String title,
						 String fullQualifiedName,
						 Iterable<EdmProperty> properties,
						 Iterable<EdmNavigationProperty> navigationProperties,
						 Iterable<String> keys, 
						 boolean isAbstract,
						 boolean hasStream,
						 EdmEntityType baseType){
		
		super(name,title,properties,isAbstract);
		
		this.fullQualifiedName = fullQualifiedName;
		this.keys       = Enumerables.of(keys);
		this.navigationProperties = Enumerables.of(navigationProperties);
		this.baseType   = baseType;
		this.hasStream  = hasStream;
		this.openType   = false;
		
		doCheckValidKeys();
	}
	
	public EdmEntityType(String name,
						 String title,
						 String fullQualifiedName,
						 Iterable<EdmProperty> properties,
						 Iterable<EdmNavigationProperty> navigationProperties,
						 Iterable<String> keys,
						 boolean isAbstract,
						 boolean hasStream,
						 boolean openType,
						 EdmEntityType baseType,
						 EdmDocumentation documentation){
		
		super(name,title,properties,isAbstract);
		
		this.fullQualifiedName = fullQualifiedName;
		this.keys       = Enumerables.of(keys);
		this.navigationProperties = Enumerables.of(navigationProperties);
		this.baseType   = baseType;
		this.hasStream  = hasStream;
		this.openType   = openType;
		this.documentation = documentation;
		
		doCheckValidKeys();
	}
	
	public String getFullQualifiedName() {
    	return fullQualifiedName;
    }

	public EdmEntityType getBaseType() {
    	return baseType;
    }
	
	public EdmProperty findProperty(String name){
		EdmProperty property = findDeclaredProperty(name);
		
		if(null == property && null != baseType){
			return baseType.findProperty(name);
		}
		
		return property;
	}
	
	public EdmProperty findDeclaredProperty(String name){
		return Iterables.firstOrNull(properties,Predicates.<EdmProperty>nameEqualsIgnoreCase(name));
	}

	public Enumerable<EdmNavigationProperty> getDeclaredNavigationProperties(){
		return navigationProperties;
	}
	
	public EdmNavigationProperty findDeclaredNavigationProperty(String name){
		return Iterables.firstOrNull(navigationProperties,Predicates.<EdmNavigationProperty>nameEqualsIgnoreCase(name));
	}
	
	public EdmNavigationProperty findNavigationProperty(String name){
		EdmNavigationProperty p = findDeclaredNavigationProperty(name);
		if(null == p && null != baseType){
			return baseType.findNavigationProperty(name);
		}
		return p;
	}
	
    public Enumerable<EdmProperty> getAllProperties(){
		if(null == baseType){
			return properties;
		}
		return baseType.getAllProperties().concat(properties);
	}
	
    public Enumerable<EdmNavigationProperty> getAllNavigationProperties(){
		if(null == baseType){
			return navigationProperties;
		}
		return baseType.getAllNavigationProperties().concat(navigationProperties);
	}
	
	public Enumerable<String> getKeys() {
    	return null == baseType ? keys : baseType.getKeys();
    }
	
	public boolean hasStream() {
    	return hasStream;
    }
	
	public boolean isOpenType() {
		return openType;
	}

	@Override
    public EdmTypeKind getTypeKind() {
	    return EdmTypeKind.Entity;
    }

	protected void doCheckValidKeys(){
		for(String key : getKeys()){
			if(null == findProperty(key)){
				throw new EdmException("Key property '" + key + "' not found");
			}
		}
	}
}