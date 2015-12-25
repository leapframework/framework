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

import leap.core.web.assets.Asset;
import leap.core.web.assets.AssetResource;


public abstract class AbstractAsset implements Asset {
	
	protected String 		path;
	protected String		contentType;
	protected long          loadedAt = System.currentTimeMillis();
	protected AssetResource resource;
	protected AssetResource debugResource;
	
	public AbstractAsset() {
	    super();
    }

	public AbstractAsset(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
	
	@Override
    public String getContentType() {
	    return contentType;
    }
	
	@Override
    public long getLoadedAt() {
        return loadedAt;
    }

    public AssetResource getResource() {
		return resource;
	}

	public AssetResource getDebugResource() {
		return debugResource;
	}

	@Override
    public String toString() {
		return "Asset[" + path + "]";
	}
}
