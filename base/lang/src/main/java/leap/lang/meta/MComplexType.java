/*
 * Copyright 2015 the original author or authors.
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
package leap.lang.meta;

import java.util.Collection;

import leap.lang.Args;
import leap.lang.Strings;


public class MComplexType extends MStructuralType implements MNamed {
	
	protected final String		 name;
	protected final String		 title;
	protected final MComplexType baseType;
	protected final boolean		 _abstract;

	public MComplexType(String name, String title, String summary, String description, 
						MComplexType baseType, Collection<MProperty> properties,
					    boolean isAbstract) {
		super(summary, description, properties);
		
		Args.notEmpty(name,  "name");

		this.name      = name;
		this.title     = Strings.isEmpty(title) ? name : title;
		this.baseType  = baseType;
		this._abstract = isAbstract;
	}
	
	@Override
    public String getName() {
	    return name;
    }
	
	@Override
    public String getTitle() {
	    return title;
    }

	@Override
    public MTypeKind getTypeKind() {
	    return MTypeKind.COMPLEX;
    }

	/**
	 * The super type of this type, may be <code>null</code>
	 */
	public MComplexType getBaseType() {
		return baseType;
	}

	/**
	 * Returns <code>true</code> if this type is an abstract type.
	 */
	public boolean isAbstract() {
		return _abstract;
	}

	public MComplexTypeRef createTypeRef() {
		return new MComplexTypeRef(name);
	}
}