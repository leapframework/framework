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

import leap.lang.Strings;


public class DefaultAssetNamingStrategy implements AssetNamingStrategy {
	
	@Override
    public String getCompiledFilename(AssetCompiler compiler,String sourceFilename) {
		String filenameWithoutExt = null;
		for(String ext : compiler.getFileExtensions()){
			if(Strings.endsWith(sourceFilename, ext,true)){
				filenameWithoutExt = sourceFilename.substring(0,sourceFilename.length() - ext.length());
				break;
			}
		}
		
		AssetType compiledAssetType = compiler.getCompiledAssetType();
		
		if(null == filenameWithoutExt){
			return sourceFilename + compiledAssetType.getFileExtensions()[0];
		}else if(compiledAssetType.matches(filenameWithoutExt)){
			return filenameWithoutExt;
		}else{
			return filenameWithoutExt + compiledAssetType.getFileExtensions()[0];
		}
    }
	
	@Override
	public String getMinifiedFilename(AssetType assetType, String sourceFilename) {
		int lastDotIndex = sourceFilename.lastIndexOf('.');
		
		if(lastDotIndex > 0){
			return sourceFilename.substring(0,lastDotIndex) + ".min" + sourceFilename.substring(lastDotIndex);
		}else{
			return sourceFilename + ".min";
		}
	}

	@Override
    public String getGzippedFilename(AssetType assetType, String sourceFilename) {
	    return sourceFilename + ".gz";
    }
}