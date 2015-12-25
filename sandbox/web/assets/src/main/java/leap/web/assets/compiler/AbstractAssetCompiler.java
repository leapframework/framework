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
package leap.web.assets.compiler;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import leap.core.validation.annotations.NotEmpty;
import leap.core.validation.annotations.NotNull;
import leap.lang.Strings;
import leap.lang.io.IO;
import leap.web.assets.AssetCompiler;
import leap.web.assets.AssetManager;
import leap.web.assets.AssetType;

public abstract class AbstractAssetCompiler implements AssetCompiler {
	
	protected @NotNull  AssetType compiledAssetType;
	protected @NotEmpty String[]  fileExtensions;

	@Override
	public AssetType getCompiledAssetType() {
		return compiledAssetType;
	}

	public void setCompiledAssetType(AssetType targetAssetType) {
		this.compiledAssetType = targetAssetType;
	}

	@Override
    public String[] getFileExtensions() {
	    return fileExtensions;
    }
	
	public void setFileExtensions(String[] fileExtensions) {
		this.fileExtensions = fileExtensions;
	}
	
	@Override
    public boolean supports(String filename) {
		for(String ext : fileExtensions){
			if(Strings.endsWith(filename, ext, true)){
				return true;
			}
		}
		return false;
    }

	@Override
    public final String compile(AssetManager manager, File file) {
	    return doCompile(manager, IO.readString(file, manager.getConfig().getCharset()), file);
    }

	@Override
    public final String compile(AssetManager manager, String source) {
	    return doCompile(manager, source, null);
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
	
	protected abstract String doCompile(AssetManager manager, String source, File file);
}
