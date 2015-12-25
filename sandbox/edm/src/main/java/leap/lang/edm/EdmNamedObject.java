/*
 * Copyright 2012 the original author or authors.
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

import leap.lang.Named;
import leap.lang.Strings;

public abstract class EdmNamedObject extends EdmObjectWithDocumentation implements Named {

	protected final String name;
	protected final String title;
	
	protected EdmNamedObject(String name)
	{
		this.name  = name;
		this.title = null;
	}
	
	protected EdmNamedObject(String name,String title)
	{
		this.name  = name;
		this.title = title;
	}
	
	protected EdmNamedObject(String name,EdmDocumentation documentation)
	{
		this.name          = name;
		this.title         = null;
		this.documentation = documentation;
	}
	
	protected EdmNamedObject(String name,String title,EdmDocumentation documentation)
	{
		this.name          = name;
		this.title         = title;
		this.documentation = documentation;
	}

	public String getName() {
		return name;
    }
	
	public String getTitle() {
		return title;
	}

	public String getTitleOrName(){
		return Strings.firstNotEmpty(getTitle(),getName());
	}
}