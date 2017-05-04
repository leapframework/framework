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

import leap.lang.Strings;

public abstract class MNamedWithDescBuilder<T extends MObject> extends MNamedBuilder<T> {

	protected String summary;
	protected String description;

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

    public void trySetSummary(String summary) {
        if(Strings.isEmpty(this.summary)) {
            this.summary = summary;
        }
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public void trySetDescription(String description) {
        if(Strings.isEmpty(this.description)) {
            this.description = description;
        }
    }

}
