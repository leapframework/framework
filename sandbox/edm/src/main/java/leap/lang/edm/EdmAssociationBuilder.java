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

public class EdmAssociationBuilder extends EdmNamedBuilder implements Buildable<EdmAssociation> {
	
	protected EdmAssociationEnd end1;
	
	protected EdmAssociationEnd end2;
	
	public EdmAssociationBuilder(){
		
	}
	
	public EdmAssociationBuilder(String name){
		this.name = name;
	}
	
	@Override
    public EdmAssociationBuilder setName(String name) {
	    super.setName(name);
	    return this;
    }

	@Override
    public EdmAssociationBuilder setTitle(String title) {
	    super.setTitle(title);
	    return this;
    }

	public EdmAssociationEnd getEnd1() {
    	return end1;
    }

	public EdmAssociationBuilder setEnd1(EdmAssociationEnd end1) {
    	this.end1 = end1;
    	return this;
    }
	
	public EdmAssociationBuilder setEnd1(String role,EdmEntityTypeRef type,EdmMultiplicity multiplicity) {
		return setEnd1(new EdmAssociationEnd(role, type, multiplicity));
	}

	public EdmAssociationEnd getEnd2() {
    	return end2;
    }

	public EdmAssociationBuilder setEnd2(EdmAssociationEnd end2) {
    	this.end2 = end2;
    	return this;
    }
	
	public EdmAssociationBuilder setEnd2(String role,EdmEntityTypeRef type,EdmMultiplicity multiplicity) {
		return setEnd2(new EdmAssociationEnd(role, type, multiplicity));
	}	
	
	@Override
    public EdmAssociationBuilder setDocumentation(EdmDocumentation documentation) {
	    super.setDocumentation(documentation);
	    return this;
    }

	@Override
    public EdmAssociationBuilder setDocumentation(String summary, String longDescription) {
	    super.setDocumentation(summary, longDescription);
	    return this;
    }
	
	public EdmAssociation build() {
	    return new EdmAssociation(name,title,end1,end2);
    }
}