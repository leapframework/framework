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
package leap.web.assets.css;

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.web.assets.AssetBundle;
import leap.core.web.assets.AssetResource;
import leap.core.web.assets.AssetSource;
import leap.lang.path.Paths;
import leap.web.assets.AbstractAssetBundler;
import leap.web.assets.AssetBundler;

public class CssAssetBundler extends AbstractAssetBundler implements AssetBundler {
    
    protected @Inject @M AssetSource source;

    @Override
    protected boolean supports(AssetBundle bundle) {
        return bundle.getType().isCSS();
    }

    @Override
    protected void processContent(AssetBundle bundler, BundleContext context, AssetResource resource, String content, StringBuilder out) {
        String path = Paths.getDirPath(resource.getPath());
        out.append(new AssetCssUrlRewriter(manager, path, content, resource.isDebug()).rewrite());
    }
}