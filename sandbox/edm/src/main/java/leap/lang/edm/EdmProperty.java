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

import leap.lang.Assert;
import leap.lang.Strings;

public class EdmProperty extends EdmNamedObject {

	private final EdmType type;
	
	private final boolean nullable;
	
	private final String defaultValue;
	
	private final boolean fixedLength;
	
	private final int maxLength;
	
	private final int precision;
	
	private final int scale;

	//---------------------------------------------------------------------------------------------------------------------------------------------
	//Feed Customization Attributes,see <a href="http://msdn.microsoft.com/en-us/library/ee373839.aspx">Feed Customization (WCF Data Services)</a>.
	//---------------------------------------------------------------------------------------------------------------------------------------------
	private final String  fcTargetPath;	  //See EdmFeedCustomization.SyndicationItemProperty
	private final String  fcContentKind;  //See EdmFeedCustomization.SyndicationTextContentKind
	private final boolean fcKeepInContent;
	
	//-------------------------------------------------------------------
	//Extended properties
	//-------------------------------------------------------------------
	private final String serializeType;
	private final String serializeFormat;
	
	public EdmProperty(String name,String title,EdmType type,
					   boolean nullable,String defaultValue,
					   boolean fixedLength,
					   int     maxLength,
					   int     precision,
					   int     scale,
					   String  fcTargetPath,
					   String  fcContentKind,
					   boolean fcKeepInContent,
					   String  serializeType,
					   String  serializeFormat) {

		super(name,title);
		
		this.type = type;
		
		this.nullable     = nullable;
		this.defaultValue = defaultValue;
		this.fixedLength  = fixedLength;
		this.maxLength    = maxLength;
		this.precision    = precision;
		this.scale        = scale;
		
		this.fcTargetPath    = fcTargetPath;
		this.fcContentKind   = fcContentKind;
		this.fcKeepInContent = fcKeepInContent;	
		this.serializeType   = serializeType;
		this.serializeFormat = serializeFormat;
		
		Assert.notNull(name,"name is required in EdmProperty");
		Assert.notNull(type,"type is required in EdmProperty");
	}
	
	public EdmProperty(String name,String title,EdmType type,
					   boolean nullable,String defaultValue,
					   boolean fixedLength,int maxLength,int precision,int scale,
					   String fcTargetPath,String fcContentKind,boolean fcKeepInContent,
					   String serializeType,String serialzeFormat,
					   EdmDocumentation documentation) {
		
		this(name,title,type,nullable,defaultValue,fixedLength,maxLength,precision,scale,fcTargetPath,fcContentKind,fcKeepInContent,serializeType,serialzeFormat);

		this.documentation   = documentation;
	}
	
	public EdmType getType() {
    	return type;
    }

	public boolean isNullable() {
    	return nullable;
    }

	public String getDefaultValue() {
    	return defaultValue;
    }

	public boolean isFixedLength() {
    	return fixedLength;
    }

	public int getMaxLength() {
    	return maxLength;
    }

	public int getPrecision() {
    	return precision;
    }

	public int getScale() {
    	return scale;
    }
	
	public String getFcTargetPath() {
    	return fcTargetPath;
    }

	public String getFcContentKind() {
    	return fcContentKind;
    }

	public boolean isFcKeepInContent() {
    	return fcKeepInContent;
    }
	
	public String getSerializeType() {
		return serializeType;
	}

	public String getSerializeFormat() {
		return serializeFormat;
	}

	@Override
    public String toString() {
		return Strings.format("EdmProperty[name={0},type={1}]", name,type);
    }
}