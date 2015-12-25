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

public class EdmParameter extends EdmNamedObject {

	private final EdmType type;
	
	private final EdmParameterMode mode;
	
	private final Boolean nullable;
	
	//extended property
	private final String  serializeType;
	private final String  serializeFormat;
	
	public EdmParameter(String name,String title,EdmType type,EdmParameterMode mode,Boolean nullable,String serializeType,String serializeFormat) {
		super(name,title);
		this.type = type;
		this.mode = mode;
		this.nullable		 = nullable;
		this.serializeType   = serializeType;
		this.serializeFormat = serializeFormat;
	}
	
	public EdmParameter(String name,String title,EdmType type,EdmParameterMode mode,Boolean nullable,String serializeType,String serializeFormat,EdmDocumentation documentation) {
		this(name,title,type,mode,nullable,serializeType,serializeFormat);
		this.documentation = documentation;
	}

	public EdmParameterMode getMode() {
    	return mode;
    }

	public EdmType getType() {
    	return type;
    }
	
	public Boolean getNullable() {
		return nullable;
	}

	public String getSerializeType() {
		return serializeType;
	}

	public String getSerializeFormat() {
		return serializeFormat;
	}
}
