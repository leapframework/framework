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

import java.util.ArrayList;
import java.util.List;

import leap.lang.Assert;
import leap.lang.Buildable;

public class EdmEnumTypeBuilder extends EdmNamedBuilder implements Buildable<EdmEnumType>{

	protected boolean isFlags;
	protected EdmSimpleType underlyingType;
	protected List<EdmEnumMember> members = new ArrayList<EdmEnumMember>();
	
	public EdmEnumTypeBuilder(){

	}
	
	public EdmEnumTypeBuilder(String name){
		this.name = name;
	}
	
	@Override
    public EdmEnumTypeBuilder setName(String name) {
	    super.setName(name);
	    return this;
    }

	@Override
    public EdmEnumTypeBuilder setTitle(String title) {
	    super.setTitle(title);
	    return this;
    }

	public boolean isFlags() {
		return isFlags;
	}

	public EdmEnumTypeBuilder setFlags(boolean isFlags) {
		this.isFlags = isFlags;
		return this;
	}

	public EdmSimpleType getUnderlyingType() {
		return underlyingType;
	}

	public EdmEnumTypeBuilder setUnderlyingType(EdmSimpleType underlyingType) {
		this.underlyingType = underlyingType;
		return this;
	}

	public List<EdmEnumMember> getMembers() {
		return members;
	}

	public EdmEnumTypeBuilder addMember(EdmEnumMember member){
		Assert.notNull(member.getName(),"the 'name' of enum member must not be empty");
		members.add(member);
		return this;
	}
	
	public EdmEnumTypeBuilder addMember(String name){
		return addMember(new EdmEnumMember(name));
	}
	
	public EdmEnumTypeBuilder addMember(String name,Object value){
		return addMember(new EdmEnumMember(name, value));
	}
	
	public EdmEnumTypeBuilder addMembers(EdmEnumMember... members){
		for(EdmEnumMember member : members){
			addMember(member);
		}
		return this;
	}
	
	public EdmEnumTypeBuilder addMembers(Iterable<EdmEnumMember> members){
		for(EdmEnumMember member : members){
			addMember(member);
		}
		return this;
	}
	
	@Override
    public EdmEnumTypeBuilder setDocumentation(EdmDocumentation documentation) {
	    super.setDocumentation(documentation);
	    return this;
    }

	@Override
    public EdmEnumTypeBuilder setDocumentation(String summary, String longDescription) {
	    super.setDocumentation(summary, longDescription);
	    return this;
    }

	public EdmEnumType build() {
	    return new EdmEnumType(name,title,underlyingType, isFlags, members, documentation);
    }
}
