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

import leap.lang.Args;
import leap.lang.Buildable;
import leap.lang.edm.EdmFeedCustomization.SyndicationItemProperty;
import leap.lang.edm.EdmFeedCustomization.SyndicationTextContentKind;

public class EdmPropertyBuilder extends EdmNamedBuilder implements Buildable<EdmProperty> {
	
	protected EdmType type;
	
	protected Boolean nullable;
	
	protected String defaultValue;
	
	protected Boolean fixedLength;
	
	protected Integer maxLength;
	
	protected Integer precision;
	
	protected Integer scale;
	
	protected String fcTargetPath;	
	
	protected String fcContentKind;  
	
	protected Boolean fcKeepInContent;	
	
	protected String serializeType;
	
	protected String serializeFormat;
	
	public EdmPropertyBuilder(){
		
	}
	
	public EdmPropertyBuilder(String name){
		this.name = name;
	}
	
	public EdmPropertyBuilder(String name,EdmType type,boolean nullable){
		this.name     = name;
		this.type     = type;
		this.nullable = nullable;
	}
	
	@Override
    public EdmPropertyBuilder setName(String name) {
	    super.setName(name);
	    return this;
    }
	
	@Override
    public EdmPropertyBuilder setTitle(String title) {
	    super.setTitle(title);
	    return this;
    }

	public EdmType getType() {
    	return type;
    }

	public EdmPropertyBuilder setType(EdmType type) {
    	this.type = type;
    	return this;
    }

	public boolean isNullable() {
    	return null != nullable &&  nullable;
    }
	
	public Boolean getNullable(){
		return nullable;
	}

	public EdmPropertyBuilder setNullable(Boolean nullable) {
    	this.nullable = nullable;
    	return this;
    }

	public String getDefaultValue() {
    	return defaultValue;
    }

	public EdmPropertyBuilder setDefaultValue(String defaultValue) {
    	this.defaultValue = defaultValue;
    	return this;
    }

	public Boolean isFixedLength() {
    	return null != fixedLength && fixedLength;
    }
	
	public Boolean getFixedLength(){
		return fixedLength;
	}

	public EdmPropertyBuilder setFixedLength(Boolean fixedLength) {
    	this.fixedLength = fixedLength;
    	return this;
    }

	public Integer getMaxLength() {
    	return maxLength;
    }

	public EdmPropertyBuilder setMaxLength(Integer maxLength) {
    	this.maxLength = maxLength;
    	return this;
    }

	public Integer getPrecision() {
    	return precision;
    }

	public EdmPropertyBuilder setPrecision(Integer precision) {
    	this.precision = precision;
    	return this;
    }

	public Integer getScale() {
    	return scale;
    }

	public EdmPropertyBuilder setScale(Integer scale) {
    	this.scale = scale;
    	return this;
    }
	
	public String getFcTargetPath() {
    	return fcTargetPath;
    }

	public EdmPropertyBuilder setFcTargetPath(String fcTargetPath) {
    	this.fcTargetPath = fcTargetPath;
    	return this;
    }
	
	public EdmPropertyBuilder setFcTargetPath(SyndicationItemProperty fcTargetPath) {
		Args.assertFalse(SyndicationItemProperty.CustomProperty.equals(fcTargetPath),"fcTargetPath cannot be the 'SyndicationItemProperty.CustomProperty'");
		
    	this.fcTargetPath = fcTargetPath.getValue();
    	return this;
    }

	public String getFcContentKind() {
    	return fcContentKind;
    }

	public EdmPropertyBuilder setFcContentKind(String fcContentKind) {
    	this.fcContentKind = fcContentKind;
    	return this;
    }
	
	public EdmPropertyBuilder setFcContentKind(SyndicationTextContentKind fcContentKind) {
    	this.fcContentKind = fcContentKind.getValue();
    	return this;
    }

	public boolean isFcKeepInContent() {
    	return null != fcKeepInContent && fcKeepInContent;
    }
	
	public Boolean getFcKeepInContent(){
		return fcKeepInContent;
	}

	public EdmPropertyBuilder setFcKeepInContent(Boolean fcKeepInContent) {
    	this.fcKeepInContent = fcKeepInContent;
    	return this;
    }
	
	public String getSerializeType() {
		return serializeType;
	}

	public EdmPropertyBuilder setSerializeType(String serializeType) {
		this.serializeType = serializeType;
		return this;
	}

	public String getSerializeFormat() {
		return serializeFormat;
	}

	public EdmPropertyBuilder setSerializeFormat(String serializeFormat) {
		this.serializeFormat = serializeFormat;
		return this;
	}

	@Override
	public EdmPropertyBuilder setDocumentation(EdmDocumentation documentation) {
		super.setDocumentation(documentation);
		return this;		
	}

	@Override
	public EdmPropertyBuilder setDocumentation(String summary, String longDescription) {
		super.setDocumentation(summary, longDescription);
		return this;
	}
	
	public EdmProperty build() {
		if(null == nullable){
			nullable = true;
		}
		
		if(null == maxLength){
			maxLength = 0;
		}
		
		if(null == fixedLength){
			fixedLength = false;
		}
		
		if(null == precision){
			precision = 0;
		}
		
		if(null == scale){
			scale = 0;
		}
		
		if(null == fcKeepInContent){
			fcKeepInContent = false;
		}
		
		return new EdmProperty(name,title, type , nullable, defaultValue, 
							   fixedLength, maxLength, precision, scale,
							   fcTargetPath,fcContentKind,fcKeepInContent,
							   serializeType,serializeFormat,documentation);
	}
}
