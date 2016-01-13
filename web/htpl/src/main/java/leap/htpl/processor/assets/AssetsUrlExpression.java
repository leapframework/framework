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
package leap.htpl.processor.assets;

import java.util.Map;
import java.util.function.BiFunction;

import leap.web.assets.Asset;
import leap.web.assets.AssetConfig;
import leap.htpl.HtplContext;
import leap.htpl.HtplEngine;
import leap.htpl.expression.UrlExpression;
import leap.lang.Strings;
import leap.lang.expression.ExpressionException;

public class AssetsUrlExpression extends UrlExpression {

	private AssetConfig assetConfig;
	
	public AssetsUrlExpression(AssetConfig assetConfig, HtplEngine engine, String url) {
		this.url         = url;
		this.assetConfig = assetConfig;
		this.func        = createFunc(engine, url);
	}

	@Override
    protected BiFunction<HtplContext,Map<String,Object>,String> createFunc(HtplEngine engine, String url) {
		if(Strings.startsWith(url, assetConfig.getPathPrefix())){
			final String assetPath = url.substring(assetConfig.getPathPrefix().length());
			
			return (c,vars) -> {
				try {
			        Asset asset = c.getAssetSource().getAsset(assetPath, c.getLocale());
			        if(null == asset){
			        	return assetPath;
			        }
			        return c.isDebug() ? asset.getDebugResource().getClientUrl() : asset.getResource().getClientUrl();
		        } catch (Throwable e) {
		        	throw new ExpressionException("Error resolving asset '" + assetPath + "', " + e.getMessage(), e);
		        }
			};
		}
		
		return super.createFunc(engine, url);
    }

}