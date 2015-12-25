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

public class EdmAssociationSet extends EdmNamedObject {

	private final EdmAssociation association;
	
	private final EdmAssociationSetEnd end1;
	
	private final EdmAssociationSetEnd end2;
	
	public EdmAssociationSet(String name,String title,EdmAssociation association,EdmAssociationSetEnd end1,EdmAssociationSetEnd end2){
		super(name,title);
		this.association = association;
		this.end1        = end1;
		this.end2        = end2;
	}
	
	public EdmAssociationSet(String name,String title,EdmAssociation association,EdmAssociationSetEnd end1,EdmAssociationSetEnd end2,EdmDocumentation documentation){
		this(name,title,association,end1,end2);
		
		this.documentation = documentation;
	}

	public EdmAssociation getAssociation() {
    	return association;
    }

	public EdmAssociationSetEnd getEnd1() {
    	return end1;
    }

	public EdmAssociationSetEnd getEnd2() {
    	return end2;
    }
}