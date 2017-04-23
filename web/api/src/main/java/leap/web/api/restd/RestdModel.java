/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.restd;

import leap.lang.Buildable;
import leap.lang.path.Paths;
import leap.orm.mapping.EntityMapping;

public class RestdModel {

    protected final EntityMapping entityMapping;
    protected final String        path;

    public RestdModel(EntityMapping entityMapping, String path) {
        this.entityMapping = entityMapping;
        this.path          = Paths.prefixWithAndSuffixWithoutSlash(path);
    }

    public String getName() {
        return entityMapping.getEntityName();
    }

    /**
     * Prefix with and suffix without '/'
     */
    public String getPath() {
        return path;
    }

    public EntityMapping getEntityMapping() {
        return entityMapping;
    }

    public static final class Builder implements Buildable<RestdModel>{

        protected EntityMapping entityMapping;
        protected String        path;

        public Builder(EntityMapping entityMapping) {
            this.entityMapping = entityMapping;
        }

        public EntityMapping getEntityMapping() {
            return entityMapping;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public RestdModel build() {
            return new RestdModel(entityMapping, path);
        }
    }
}