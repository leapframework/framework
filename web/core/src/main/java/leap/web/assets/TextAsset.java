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

import leap.core.web.assets.AssetManager;
import leap.lang.Strings;

public class TextAsset extends AbstractAsset {

    public TextAsset(AssetManager manager, String path, String text, String contentType) {
        super(path);
        this.contentType = contentType;
        this.debugResource = new TextAssetResource(manager, this, Strings.getBytesUtf8(text));
        this.resource      = debugResource;
    }

    @Override
    public boolean isText() {
        return true;
    }

    @Override
    public boolean reloadable() {
        return false;
    }

    @Override
    public boolean reload() {
        return false;
    }
}