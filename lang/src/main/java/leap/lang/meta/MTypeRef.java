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

import leap.lang.Args;
import leap.lang.Strings;

public abstract class MTypeRef extends MType {
	
	protected final String refTypeName;
	protected final String refTypeQName;
	
	public MTypeRef(String refTypeName) {
		this(null, null, refTypeName, refTypeName);
	}
	
	public MTypeRef(String refTypeName, String refTypeQName) {
		this(null, null, refTypeName, refTypeQName);
	}

	public MTypeRef(String summary, String description, String refTypeName, String refTypeQName) {
		super(summary, description);
		
		Args.notEmpty(refTypeName, "refTypeName");
		
		this.refTypeName  = refTypeName;
		this.refTypeQName = Strings.isEmpty(refTypeQName) ? refTypeName : refTypeQName;
	}

	@Override
	public MTypeKind getTypeKind() {
		return MTypeKind.REFERENCE;
	}
	
	/**
	 * The short name of the referenced type.
	 */
	public String getRefTypeName() {
		return refTypeName;
	}

	/**
	 * The full qualified name of the referenced type.
	 */
	public String getRefTypeQName() {
		return refTypeQName;
	}

	public abstract MTypeKind getRefTypeKind();

}