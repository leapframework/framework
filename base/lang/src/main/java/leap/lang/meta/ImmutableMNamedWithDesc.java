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
package leap.lang.meta;

import leap.lang.Named;
import leap.lang.Titled;
import leap.lang.annotation.Localizable;

public class ImmutableMNamedWithDesc extends ImmutableMNamed implements MNamedWithDesc {
	
	protected final String summary;
	protected final String description;

	public ImmutableMNamedWithDesc(String name, String title, String summary,String description) {
		super(name,title);
		this.summary = summary;
		this.description = description;
	}

	@Override
	@Localizable
	public String getSummary() {
		return summary;
	}

	@Override
	@Localizable
	public String getDescription() {
		return description;
	}

	public abstract static class Builder implements Named,Titled{

		protected String name;
		protected String title;
		protected String summary;
		protected String description;
		
		public Builder() {
	        super();
        }
		
		public Builder(ImmutableMNamedWithDesc m) {
			this.name = m.name;
			this.title = m.title;
			this.summary = m.summary;
			this.description = m.description;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Localizable
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		@Localizable
		public String getSummary() {
			return summary;
		}

		public void setSummary(String summary) {
			this.summary = summary;
		}

		@Localizable
		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}
}