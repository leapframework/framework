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


public class MComplexTypeRef extends MTypeRef {

    private final Boolean partial;
	
	public MComplexTypeRef(String refTypeName) {
	    this(refTypeName, (Boolean)null);
    }

    public MComplexTypeRef(String refTypeName, Boolean partial) {
        super(refTypeName);
        this.partial = partial;
    }

	public MComplexTypeRef(String refTypeName, String refTypeQName) {
        this(refTypeName, refTypeQName, null);
    }

    public MComplexTypeRef(String refTypeName, String refTypeQName, Boolean partial) {
        super(refTypeName, refTypeQName);
        this.partial = partial;
    }

	public MComplexTypeRef(String summary, String description, String refTypeName, String refTypeQName) {
		this(summary, description, refTypeName, refTypeQName, null);
	}

    public MComplexTypeRef(String summary, String description, String refTypeName, String refTypeQName, Boolean partial) {
        super(summary, description, refTypeName, refTypeQName);
        this.partial = partial;
    }

    @Override
    public String getName() {
        return refTypeName;
    }

    @Override
	public MTypeKind getRefTypeKind() {
		return MTypeKind.COMPLEX;
	}

    public Boolean getPartial() {
        return partial;
    }
}