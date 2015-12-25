/*
 * Copyright 2015 the original author or authors.
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

import java.io.IOException;
import java.io.Reader;

import leap.core.annotation.Inject;
import leap.core.web.assets.Asset;
import leap.core.web.assets.AssetBundle;
import leap.core.web.assets.AssetManager;
import leap.core.web.assets.AssetResource;
import leap.lang.io.IO;

public abstract class AbstractAssetBundler implements AssetBundler {
    
    protected static final class BundleContext {
        boolean deubg;
    }
    
    protected @Inject AssetManager manager;
    
    @Override
    public Asset bundle(AssetBundle bundle) throws IOException {
        if(!supports(bundle)) {
            return null;
        }
        
        BundleContext context = new BundleContext();
        
        String content = bundleContent(bundle, context);
        String debugContent = context.deubg ? bundleDebugContent(bundle, context) : content;

        return new BundledAsset(manager, this, bundle, content, debugContent);
    }
    
    protected abstract boolean supports(AssetBundle bundle);
    
    protected String bundleContent(AssetBundle bundle, BundleContext context) throws IOException {
        StringBuilder out = new StringBuilder();
        
        for(Asset asset : bundle.getAssets()) {
            
            if(!asset.getDebugResource().getFingerprint().equals(asset.getResource().getFingerprint())) {
                context.deubg = true;
            }
            
            try(Reader r = asset.getResource().getReader()) {
                appendLineIfNecessary(out);
                processContent(bundle, context, asset.getDebugResource(), IO.readString(r), out);
            }
            
        }
        
        return out.toString();
    }
    
    protected String bundleDebugContent(AssetBundle bundle, BundleContext context) throws IOException {
        StringBuilder out = new StringBuilder();
        
        for(Asset asset : bundle.getAssets()) {
            try(Reader r = asset.getDebugResource().getReader()) {
                appendLineIfNecessary(out);
                processContent(bundle, context, asset.getDebugResource(), IO.readString(r), out);
            }
        }
        
        return out.toString();
    }
    
    protected void processContent(AssetBundle bundler, BundleContext context, AssetResource resource, String content, StringBuilder out) {
        out.append(content);
    }
    
    protected void appendLineIfNecessary(StringBuilder out) {
        if(out.length() > 0) {
            char c = out.charAt(out.length() - 1);
            if(c == '\r' || c == '\n') {
                return;
            }else{
                out.append('\n');
            }
        }
    }
}
