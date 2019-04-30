/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.core;

import leap.lang.path.Paths;
import leap.lang.resource.Resource;

public final class SimpleAppResource implements AppResource {

    private final Resource resource;
    private final boolean  defaultOverride;
    private final float    order;
    private final String   path;

    public SimpleAppResource(Resource resource) {
        this(resource, false);
    }

    public SimpleAppResource(Resource resource, boolean defaultOverride) {
        this(resource, defaultOverride, Integer.MAX_VALUE, resource.getClasspath());
    }

    public SimpleAppResource(Resource resource, boolean defaultOverride, float order, String path) {
        this.resource = resource;
        this.defaultOverride = defaultOverride;
        this.order = order;
        this.path = Paths.prefixWithoutSlash(path);
    }

    @Override
    public float getSortOrder() {
        return order;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public boolean isDefaultOverride() {
        return defaultOverride;
    }

    @Override
    public String toString() {
        return resource.toString();
    }
}
