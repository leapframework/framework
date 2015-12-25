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

import java.io.File;



public interface AssetCompiler {
	
	/**
	 * Returns the compiled asset type.
	 */
	AssetType getCompiledAssetType();
	
	/**
	 * Returns the supported file extensions by this asset compiler.
	 */
	String[] getFileExtensions();	
	
	/**
	 * Returns <code>true</code> if this compiler supports the given filename.
	 */
	boolean supports(String filename);
	
	/**
	 * Compiles the given source file.
	 */
	String compile(AssetManager manager, File file) throws AssetCompileException;
	
	/**
	 * Compiles the given source string.
	 */
	String compile(AssetManager manager, String source) throws AssetCompileException;

}