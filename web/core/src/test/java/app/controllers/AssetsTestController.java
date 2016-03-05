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
package app.controllers;

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.web.assets.Asset;
import leap.web.assets.AssetSource;
import leap.lang.Assert;
import leap.lang.io.IO;
import leap.web.Content;
import leap.web.Contents;
import leap.web.Request;
import leap.web.action.ControllerBase;

public class AssetsTestController extends ControllerBase {
	
	protected @Inject @M AssetSource assetSource;

	public Content hello(Request request) throws Throwable {
		Asset asset = request.getAssetSource().getAsset("js/hello.js", request.getLocale());
		Assert.notNull(asset);
		
		String content = IO.readString(asset.getResource().getReader());
		
		return Contents.json(content);
	}
	
	public String getAssetUrl(String path,Boolean debug) throws Throwable {
		Asset asset = assetSource.getAsset(path,Request.current().getLocale());
        if(null == asset) {
            return "";
        }
		return debug != null && debug ? asset.getDebugResource().getClientUrl() : asset.getResource().getClientUrl();
	}
	
}