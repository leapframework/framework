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


public interface Asset {
	
	String getPath();

	String getDebugPath();

	String getContentType();
	
	AssetResource getResource();
	
	AssetResource getDebugResource();
	
	long getLoadedAt();
	
	boolean isText();

	boolean reloadable();
	
	boolean reload();

	/**
	 * Returns <code>true</code> if the asset is a bundle asset.
     */
	default boolean isBundle() {
		return false;
	}

	default String getClientUrl(boolean debug) {
		return debug ? getDebugResource().getClientUrl() : getResource().getClientUrl();
	}

}