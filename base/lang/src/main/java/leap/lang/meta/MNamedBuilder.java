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

import leap.lang.Buildable;
import leap.lang.Named;
import leap.lang.Strings;

public abstract class MNamedBuilder<T extends MObject> implements Buildable<T>, Named {

	protected String name;
	protected String title;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}	

    public void trySetTitle(String title) {
        if(Strings.isEmpty(title)) {
            return;
        }

        if(Strings.isEmpty(this.title) || this.title.equals(name)) {
            this.title = title;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + getName() + ")";
    }
}
