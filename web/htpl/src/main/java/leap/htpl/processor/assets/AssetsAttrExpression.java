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

import leap.web.assets.Asset;
import leap.web.assets.AssetSource;
import leap.htpl.HtplContext;
import leap.htpl.ast.Attr;
import leap.htpl.expression.AttrExpression;
import leap.lang.expression.ExpressionException;

public class AssetsAttrExpression extends AttrExpression {
	
	protected final String assetPath;
	
	public AssetsAttrExpression(Attr attr) {
        this(attr, attr.getString());
    }

	public AssetsAttrExpression(Attr attr,String assetPath) {
	    super(attr);
	    this.assetPath = assetPath;
	}
	
	@Override
    protected Object doEval(HtplContext context, Map<String, Object> vars) {
		try {
	        Asset asset = context.getAssetSource().getAsset(assetPath, context.getLocale());
	        if(null == asset){
	        	return assetPath;
	        }
	        return context.isDebug() ? asset.getDebugResource().getClientUrl() : asset.getResource().getClientUrl();
        } catch (Throwable e) {
        	throw new ExpressionException("Error resolving asset '" + assetPath + "', " + e.getMessage(), e);
        }
    }

	@Override
    public String toString() {
		return "asset:" + this.assetPath;
	}
}