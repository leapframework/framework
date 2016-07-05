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

import leap.lang.Described;
import leap.lang.annotation.Localizable;

public class ImmutableMDescribed implements Described,MObject {
	
	protected final String summary;
	protected final String description;

	public ImmutableMDescribed(String summary,String description) {
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

}