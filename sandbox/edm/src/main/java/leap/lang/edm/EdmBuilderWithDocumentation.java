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

import leap.lang.Strings;


public abstract class EdmBuilderWithDocumentation extends EdmBuilder {
	
	protected EdmDocumentation documentation;
	
	public EdmDocumentation getDocumentation() {
		return documentation;
	}
	
	public EdmBuilderWithDocumentation setDocumentation(EdmDocumentation documentation) {
    	this.documentation = documentation;
    	return this;
    }
	
	public EdmBuilderWithDocumentation setDocumentation(String summary,String longDescription) {
		if(Strings.isEmpty(summary) && Strings.isEmpty(longDescription)){
			this.documentation = null;
		}else{
			this.documentation = new EdmDocumentation(summary, longDescription);	
		}
    	return this;
    }
}