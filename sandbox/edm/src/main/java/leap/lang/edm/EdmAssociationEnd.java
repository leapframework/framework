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

public class EdmAssociationEnd extends EdmObjectWithDocumentation {
	
	private final String role;
	
	private final EdmEntityTypeRef type;
	
	private final EdmMultiplicity multiplicity;
	
	public EdmAssociationEnd(String role,EdmEntityTypeRef type,EdmMultiplicity multiplicity) {
	    this.role = role;
	    this.type = type;
	    this.multiplicity = multiplicity;
    }
	
	public EdmAssociationEnd(String role,EdmEntityTypeRef type,EdmMultiplicity multiplicity,EdmDocumentation documentation) {
		this(role,type,multiplicity);
		this.documentation = documentation;
	}

	public String getRole() {
    	return role;
    }

	public EdmEntityTypeRef getType() {
    	return type;
    }

	public EdmMultiplicity getMultiplicity() {
    	return multiplicity;
    }
	
    @Override
    public String toString() {
        return String.format("EdmAssociationEnd[%s,%s,%s]", role, type, multiplicity);
    }
}