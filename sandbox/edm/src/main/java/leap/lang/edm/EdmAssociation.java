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

public class EdmAssociation extends EdmNamedObject {

	private final EdmAssociationEnd end1;
	
	private final EdmAssociationEnd end2;
	
	public EdmAssociation(String name,String title,EdmAssociationEnd end1,EdmAssociationEnd end2) {
		super(name,title);
		this.end1 = end1;
		this.end2 = end2;
	}

	public EdmAssociation(String name,String title,EdmAssociationEnd end1,EdmAssociationEnd end2,EdmDocumentation documentation) {
		super(name,title,documentation);
		this.end1 = end1;
		this.end2 = end2;
	}
	
	public EdmAssociationEnd getEnd1() {
    	return end1;
    }

	public EdmAssociationEnd getEnd2() {
    	return end2;
    }
	
    @Override
    public String toString() {
        StringBuilder rt = new StringBuilder();
        rt.append("EdmAssociation[");
        rt.append(name);
        rt.append(",end1=").append(end1);
        rt.append(",end2=").append(end2);
        rt.append(']');
        return rt.toString();
    }
}