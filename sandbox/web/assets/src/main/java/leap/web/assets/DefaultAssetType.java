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
package leap.web.assets;

import java.util.LinkedHashSet;
import java.util.Set;

import leap.core.validation.annotations.NotEmpty;
import leap.lang.Strings;

public class DefaultAssetType implements AssetType {

	protected @NotEmpty String   name;
	protected @NotEmpty String[] fileExtensions;

	@Override
    public String getName() {
	    return name;
    }
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
    public String[] getFileExtensions() {
	    return fileExtensions;
    }
	
	public void setFileExtensions(String[] fileExtensions) {
		this.fileExtensions = fileExtensions;
	}

	@Override
    public boolean matches(String filename) {
		for(String ext : fileExtensions){
			if(Strings.endsWith(filename, ext, true)){
				return true;
			}
		}
		return false;
    }

	public void setFileExtensions(String extensions){
		if(!Strings.isEmpty(extensions)){
			Set<String> set = new LinkedHashSet<String>();
			
			String[] lines = Strings.splitMultiLines(extensions);
			
			for(String line : lines){
				String[] parts = Strings.split(line,'|');
				
				for(String part : parts){
					if(!Strings.isEmpty((part = part.trim()))){
						set.add(part);
					}
				}
			}
			
			if(!set.isEmpty()){
				this.fileExtensions = set.toArray(new String[set.size()]);
			}
		}
	}
}
