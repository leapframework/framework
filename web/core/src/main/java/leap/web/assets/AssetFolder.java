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

import leap.lang.resource.Resource;

public interface AssetFolder {

    /**
     * Returns <code>true</code> if the folder exists.
     */
    boolean exists();

    /**
     * Optional. Returns the path prefix of the folder, the path must be both starts and ends with '/'.
     *
     * <p/>
     * The path prefix will append to the path prefix of assets and prepend before the path of asset file.
     *
     * <p/>
     * Example :
     * <pre>
     *     if path-prefix is '/upload/', and the file '1.img' in folder will be accessed by path '/assets/upload/1.img'.
     * </pre>
     */
    String getPathPrefix();

    /**
     * Returns the relative resource in folder.
     */
    Resource getRelativeResource(String path);
}
