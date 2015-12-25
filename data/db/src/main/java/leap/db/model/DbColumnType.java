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
package leap.db.model;

public class DbColumnType {

    private final int     typeCode;
    private final String  typeDef;
    private final int     nativeTypeCode;
    private final Integer minLength;
    private final Integer maxLength;
    
    
    public DbColumnType(int typeCode,String typeDef){
    	this(typeCode,typeDef, null , null,typeCode);
    }
    
    public DbColumnType(int typeCode,String typeDef,int nativeTypeCode){
    	this(typeCode,typeDef,null,null,typeCode);
    }
    
    public DbColumnType(int typeCode,String typeDef,Integer minLength,Integer maxLength){
    	this(typeCode,typeDef,minLength,maxLength,typeCode);
    }    
    
	public DbColumnType(int typeCode, String typeDef, Integer minLength, Integer maxLength, int nativeTypeCode) {
	    this.typeCode       = typeCode;
	    this.typeDef        = typeDef;
	    this.minLength      = minLength;
	    this.maxLength      = maxLength;
	    this.nativeTypeCode = nativeTypeCode;
    }

	public int getTypeCode() {
		return typeCode;
	}

	public String getTypeDef() {
		return typeDef;
	}
	
	public boolean hasMinLength() {
	    return null != minLength;
	}
	
	public boolean hasMaxLength() {
	    return null != maxLength;
	}

	public Integer getMinLength() {
		return minLength;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public int getNativeTypeCode() {
		return nativeTypeCode;
	}
	
	public boolean matchesLength(int len) {
	    if(null != minLength && len < minLength) {
	        return false;
	    }
	    if(null != maxLength && len > maxLength) {
	        return false;
	    }
	    return true;
	}

	public String getRangeString() {
	    return "[min:" + (null == minLength ? "unlimited" : minLength) + 
	           ",max:" + (null == maxLength ? "unlimited" : maxLength) + 
	           "]";
	}
}
