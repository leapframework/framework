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

public class EdmEntitySetBuilder extends EdmNamedBuilder implements Buildable<EdmEntitySet> {
	
	protected EdmEntityTypeRef entityType;

	public EdmEntitySetBuilder() {
	    super();
    }

	public EdmEntitySetBuilder(String name) {
	    super(name);
    }
	
	public EdmEntitySetBuilder(String name,EdmEntityTypeRef entityType) {
	    super(name);
	    this.entityType = entityType;
    }
	
	@Override
    public EdmEntitySetBuilder setName(String name) {
	    super.setName(name);
	    return this;
    }

	@Override
    public EdmEntitySetBuilder setTitle(String title) {
	    super.setTitle(title);
	    return this;
    }

	public EdmEntityTypeRef getEntityType() {
		return entityType;
	}

	public EdmEntitySetBuilder setEntityType(EdmEntityTypeRef entityType) {
		this.entityType = entityType;
		return this;
	}
	
	@Override
    public EdmEntitySetBuilder setDocumentation(EdmDocumentation documentation) {
	    super.setDocumentation(documentation);
	    return this;
    }

	@Override
    public EdmEntitySetBuilder setDocumentation(String summary, String longDescription) {
	    super.setDocumentation(summary, longDescription);
	    return this;
    }

	public EdmEntitySet build() {
	    return new EdmEntitySet(name,title,entityType, documentation);
    }
}
