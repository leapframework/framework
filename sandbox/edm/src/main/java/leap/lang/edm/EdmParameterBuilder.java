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
package leap.lang.edm;

import leap.lang.Buildable;

public class EdmParameterBuilder extends EdmNamedBuilder implements Buildable<EdmParameter> {
	
	protected EdmParameterMode mode;
	protected EdmType          type;
	protected Boolean		   nullable;
	protected String		   serializeType;
	protected String           serializeFormat;
	
	@Override
    public EdmParameterBuilder setName(String name) {
	    super.setName(name);
	    return this;
    }

	@Override
    public EdmParameterBuilder setTitle(String title) {
	    super.setTitle(title);
	    return this;
    }
	
	public EdmParameterMode getMode() {
		return mode;
	}

	public EdmParameterBuilder setMode(EdmParameterMode mode) {
		this.mode = mode;
		return this;
	}
	
	public EdmType getType() {
		return type;
	}

	public EdmParameterBuilder setType(EdmType type) {
		this.type = type;
		return this;
	}
	
	public Boolean getNullable() {
		return nullable;
	}

	public EdmParameterBuilder setNullable(Boolean nullable) {
		this.nullable = nullable;
		return this;
	}

	public String getSerializeType() {
		return serializeType;
	}

	public EdmParameterBuilder setSerializeType(String serializeType) {
		this.serializeType = serializeType;
		return this;
	}

	public String getSerializeFormat() {
		return serializeFormat;
	}

	public EdmParameterBuilder setSerializeFormat(String serializeFormat) {
		this.serializeFormat = serializeFormat;
		return this;
	}

	@Override
    public EdmParameterBuilder setDocumentation(EdmDocumentation documentation) {
	    super.setDocumentation(documentation);
	    return this;
    }

	@Override
    public EdmParameterBuilder setDocumentation(String summary, String longDescription) {
	    super.setDocumentation(summary, longDescription);
	    return this;
    }

	public EdmParameter build() {
	    return new EdmParameter(name,title, type, mode, nullable, serializeType, serializeFormat, documentation);
    }
}
