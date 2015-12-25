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

public class EdmNavigationProperty extends EdmNamedObject {
	
	private final EdmAssociation relationship;

	private final EdmAssociationEnd fromRole;
	
	private final EdmAssociationEnd toRole;
	
	public EdmNavigationProperty(String name,String title,EdmAssociation relationship,EdmAssociationEnd fromRole,EdmAssociationEnd toRole){
		super(name,title);
		this.relationship = relationship;
		this.fromRole = fromRole;
		this.toRole = toRole;
	}
	
	public EdmNavigationProperty(String name,String title,EdmAssociation relationship,EdmAssociationEnd fromRole,EdmAssociationEnd toRole,EdmDocumentation documentation){
		this(name,title,relationship,fromRole,toRole);
	
		this.documentation = documentation;
	}

	public EdmAssociation getRelationship() {
    	return relationship;
    }

	public EdmAssociationEnd getFromRole() {
    	return fromRole;
    }

	public EdmAssociationEnd getToRole() {
    	return toRole;
    }
}
