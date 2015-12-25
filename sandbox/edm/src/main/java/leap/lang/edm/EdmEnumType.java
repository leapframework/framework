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

import leap.lang.Enumerable;
import leap.lang.Enumerables;
import leap.lang.Named;
import leap.lang.Strings;

public class EdmEnumType extends EdmType implements Named {
	
	protected final String  name;
	protected final String  title;
	protected final EdmSimpleType underlyingType;
	protected final boolean isFlags;
	protected final Enumerable<EdmEnumMember> members;
	
	public EdmEnumType(String name,String title, EdmSimpleType underlyingType, boolean isFlags, Iterable<EdmEnumMember> members) {
	    this.name = name;
	    this.title = title;
	    this.underlyingType = underlyingType;
	    this.isFlags = isFlags;
	    this.members = Enumerables.of(members);
    }
	
	public EdmEnumType(String name,String title, EdmSimpleType underlyingType, boolean isFlags, Iterable<EdmEnumMember> members,EdmDocumentation documentation) {
	    this.name = name;
	    this.title = title;
	    this.underlyingType = underlyingType;
	    this.isFlags = isFlags;
	    this.members = Enumerables.of(members);
	    this.documentation = documentation;
    }

	public String getName() {
	    return name;
    }
	
	public String getTitle() {
		return title;
	}
	
	public String getTitleOrName(){
		return Strings.firstNotEmpty(title,name);
	}

	public EdmSimpleType getUnderlyingType() {
		return underlyingType;
	}

	public boolean isFlags() {
		return isFlags;
	}

	public Enumerable<EdmEnumMember> getMembers() {
		return members;
	}

	@Override
    public EdmTypeKind getTypeKind() {
	    return EdmTypeKind.Enum;
    }
}
