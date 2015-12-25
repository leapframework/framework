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
import java.util.List;

import javax.servlet.ServletContext;

import leap.web.assets.AssetConfig;

public interface AssetManager {
	
	/**
	 * Returns the configuration.
	 */
	AssetConfig getConfig();
	
	/**
	 * Returns the naming strategy.
	 */
	AssetNamingStrategy getNamingStrategy();
	
	/**
	 * Returns the {@link AssetTask} for the given webapp context.
	 */
	AssetTask getTask(ServletContext sc);
	
	/**
	 * Creates a new {@link AssetTask} for processing assets in the given webapp directory.
	 */
	AssetTask createTask(File webappDirectory);
	
	/**
	 * Returns the resolved {@link AssetType} or <code>null</code> by the given filename.
	 */
	AssetType resolveAssetTypeByFilename(String filename);
	
	/**
	 * Returns the resolved {@link AssetCompiler} or <code>null</code> by the given filename.
	 */
	AssetCompiler resolveAssetCompilerByFilename(String filename);
	
	/**
	 * Returns the {@link AssetMinifier} or <code>null</code> for the given asset type.
	 */
	AssetMinifier resolveAssetMinifier(AssetType type);
	
	/**
	 * Returns a {@link List} contains all the {@link AssetProcessor} for the given asset type.
	 */
	List<AssetProcessor> resolveResourceProcessors(AssetType type);
}