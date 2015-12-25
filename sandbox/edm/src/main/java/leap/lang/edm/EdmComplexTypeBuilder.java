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

import leap.lang.Buildable;

public class EdmComplexTypeBuilder extends EdmNamedStructualTypeBuilder implements Buildable<EdmComplexType> {
	
	protected EdmComplexType baseType;
	
	public EdmComplexTypeBuilder() {
	    super();
    }

	public EdmComplexTypeBuilder(String name) {
	    super(name);
    }

	@Override
    public EdmComplexTypeBuilder setAbstract(boolean isAbstract) {
	    super.setAbstract(isAbstract);
	    return this;
    }

	@Override
    public EdmComplexTypeBuilder setName(String name) {
	    super.setName(name);
	    return this;
    }
	
	@Override
    public EdmComplexTypeBuilder setTitle(String title) {
	    super.setTitle(title);
	    return this;
    }

	@Override
    public EdmComplexTypeBuilder addProperty(EdmProperty property) {
	    super.addProperty(property);
	    return this;
    }

	@Override
    public EdmComplexTypeBuilder addProperty(String name, EdmType type, boolean nullable) {
	    super.addProperty(name, type, nullable);
	    return this;
    }
	
	public EdmComplexType getBaseType() {
		return baseType;
	}

	public EdmComplexTypeBuilder setBaseType(EdmComplexType baseType) {
		this.baseType = baseType;
		return this;
	}

	@Override
    public EdmComplexTypeBuilder setDocumentation(EdmDocumentation documentation) {
	    super.setDocumentation(documentation);
	    return this;
    }

	@Override
    public EdmComplexTypeBuilder setDocumentation(String summary, String longDescription) {
	    super.setDocumentation(summary, longDescription);
	    return this;
    }

	public EdmComplexType build() {
	    return new EdmComplexType(name,title,properties, isAbstract, baseType);
    }
}