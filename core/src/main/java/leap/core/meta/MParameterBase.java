/*
 * Copyright 2014 the original author or authors.
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
package leap.core.meta;

import leap.lang.Args;
import leap.lang.meta.ImmutableMNamedWithDesc;
import leap.lang.meta.MType;

public abstract class MParameterBase extends ImmutableMNamedWithDesc {
	
	protected final MType	type;
	protected final Boolean required;

	public MParameterBase(String name, String title, String summary, String description, MType type, Boolean required) {
		super(name, title, summary, description);
		
		Args.notNull(type,"type");
		
		this.required = required;
		this.type = type;
	}
	
	public MType getType() {
		return type;
	}

	public Boolean getRequired() {
		return required;
	}
	
	@Override
    public String toString() {
		return "Paramster[name=" + name + ", type=" + type + "]"; 
	}

	public static class Builder extends ImmutableMNamedWithDesc.Builder {
		
		protected MType   type;
		protected Boolean required;
		
		public Builder() {
	        super();
        }
		
		public Builder(MParameterBase p) {
	        super(p);
	        this.type = p.type;
	        this.required = p.required;
        }

		public Boolean getRequired() {
			return required;
		}

		public void setRequired(Boolean required) {
			this.required = required;
		}
		
	}
}