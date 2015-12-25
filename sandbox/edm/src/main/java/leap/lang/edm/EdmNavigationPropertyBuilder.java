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

public class EdmNavigationPropertyBuilder extends EdmNamedBuilder implements Buildable<EdmNavigationProperty>{

	private EdmAssociation 	  relationship;
	private EdmAssociationEnd fromRole;
	private EdmAssociationEnd toRole;
	private String			  relationshipFullQualifiedName;
	private String            fromRoleName;
	private String			  toRoleName;
	
	public EdmNavigationPropertyBuilder() {
	    super();
    }

	public EdmNavigationPropertyBuilder(String name) {
	    super(name);
    }
	
	@Override
    public EdmNavigationPropertyBuilder setName(String name) {
	    super.setName(name);
	    return this;
    }

	@Override
    public EdmNavigationPropertyBuilder setTitle(String title) {
	    super.setTitle(title);
	    return this;
    }

	public EdmAssociation getRelationship() {
		return relationship;
	}
	
	public EdmNavigationPropertyBuilder setRelationship(EdmAssociation relationship) {
		this.relationship = relationship;
		return this;
	}
	
	public String getRelationshipFullQualifiedName() {
		return relationshipFullQualifiedName;
	}

	public EdmNavigationPropertyBuilder setRelationshipFullQualifiedName(String relationshipFullQualifiedName) {
		this.relationshipFullQualifiedName = relationshipFullQualifiedName;
		return this;
	}
	
	public EdmAssociationEnd getFromRole() {
		return fromRole;
	}
	
	public String getFromRoleName() {
		return fromRoleName;
	}

	public EdmNavigationPropertyBuilder setFromRoleName(String fromRoleName) {
		this.fromRoleName = fromRoleName;
		return this;
	}

	public EdmNavigationPropertyBuilder setFromRole(EdmAssociationEnd fromRole) {
		this.fromRole = fromRole;
		return this;
	}
	
	public EdmAssociationEnd getToRole() {
		return toRole;
	}

	public EdmNavigationPropertyBuilder setToRole(EdmAssociationEnd toRole) {
		this.toRole = toRole;
		return this;
	}
	
	public String getToRoleName() {
		return toRoleName;
	}

	public EdmNavigationPropertyBuilder setToRoleName(String toRoleName) {
		this.toRoleName = toRoleName;
		return this;
	}

	@Override
    public EdmNavigationPropertyBuilder setDocumentation(EdmDocumentation documentation) {
	    super.setDocumentation(documentation);
	    return this;
    }

	@Override
    public EdmNavigationPropertyBuilder setDocumentation(String summary, String longDescription) {
	    super.setDocumentation(summary, longDescription);
	    return this;
    }

	public EdmNavigationProperty build() {
	    return new EdmNavigationProperty(name,title,relationship, fromRole, toRole,documentation);
    }
}
