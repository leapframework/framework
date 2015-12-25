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
package leap.core.ioc;



/**
 * <pre>
 * value type :
 * 
 * 1. {@link BeanReference}
 * 2. resolved java object (String,Integer,etc)
 * 3. list,set,array,map
 * </pre>
 */
class ValueDefinition {

	private boolean  merge;
	private Object   definedValue;
	private Class<?> definedType;
	private Class<?> definedKeyType;
	private Class<?> definedElementType;
	private Object   resolvedValue;
	private boolean  resolved;
	private boolean  hasBeanReferences;
	
	public ValueDefinition(Object value){
		this(value,false);
	}
	
	public ValueDefinition(Object value,boolean merge){
		this(value,merge,null,null,null);
	}
	
	public ValueDefinition(Object value,boolean merge,Class<?> type){
		this(value,merge,type,null,null);
	}
	
	public ValueDefinition(Object value,boolean merge,Class<?> type,Class<?> keyType,Class<?> elementType){
		this.definedValue       = value;
		this.merge              = merge;
		this.definedType        = type;
		this.definedKeyType     = keyType;
		this.definedElementType = elementType;
	}
	
	public boolean isMerge() {
	    return merge;
    }
	
	public Object getDefinedValue() {
		return definedValue;
	}
    
	public Class<?> getDefinedType() {
		return definedType;
	}

	public Class<?> getDefinedKeyType() {
		return definedKeyType;
	}

	public Class<?> getDefinedElementType() {
		return definedElementType;
	}
	
	public boolean isResolved() {
		return resolved;
	}
	
	public Object getResolvedValue() {
		return resolvedValue;
	}

	public boolean hasBeanReferences(){
		return hasBeanReferences;
	}

	protected void resolved(Object value){
		this.resolved = true;
		this.resolvedValue    = value;
	}
	
	protected void setMerge(boolean merge) {
		this.merge = merge;
	}

	protected void setDefinedValue(Object definedValue) {
		this.definedValue = definedValue;
	}

	protected void setDefinedType(Class<?> definedType) {
		this.definedType = definedType;
	}

	protected void setDefinedKeyType(Class<?> definedKeyType) {
		this.definedKeyType = definedKeyType;
	}

	protected void setDefinedElementType(Class<?> definedElementType) {
		this.definedElementType = definedElementType;
	}
}
