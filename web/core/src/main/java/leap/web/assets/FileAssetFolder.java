/*
 * Copyright 2016 the original author or authors.
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

import leap.lang.resource.FileResource;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;

import java.io.File;

public class FileAssetFolder implements AssetFolder {

    private final File         dir;
    private final String       pathPrefix;
    private final FileResource dirResource;

    public FileAssetFolder(File dir, String pathPrefix) {
        this.dir = dir;
        this.pathPrefix = pathPrefix;
        this.dirResource = Resources.createFileResource(dir);
    }

    @Override
    public boolean exists() {
        return dir.exists();
    }

    @Override
    public String getPathPrefix() {
        return pathPrefix;
    }

    @Override
    public Resource getRelativeResource(String path) {
        return dirResource.createRelative(path);
    }
}