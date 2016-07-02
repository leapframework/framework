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

import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.annotation.Localizable;

public class ImmutableMNamed implements MNamed {

	protected final String name;
	protected final String title;

	public ImmutableMNamed(String name) {
		this(name,name);
	}
	
	public ImmutableMNamed(String name,String title) {
		Args.notNull(name,"name");
		this.name  = name;
		this.title = Strings.isEmpty(title) ? name : title;
	}

	/**
	 * Returns the code name of this object.
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	@Localizable
    public String getTitle() {
	    return title;
    }
}