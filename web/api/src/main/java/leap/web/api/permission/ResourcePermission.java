/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.web.api.permission;

import leap.lang.path.PathPattern;
import leap.web.annotation.http.HttpMethod;
import leap.web.route.Route;

import java.util.Comparator;

public class ResourcePermission {

    static Comparator<ResourcePermission> COMPARATOR = (r1, r2) -> {
        return 1;
    };

    private String       value;
    private String       description;
    private HttpMethod[] httpMethods;
    private PathPattern  pathPattern;
    private boolean      _default;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HttpMethod[] getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(HttpMethod[] httpMethods) {
        this.httpMethods = httpMethods;
    }

    public PathPattern getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(PathPattern pathPattern) {
        this.pathPattern = pathPattern;
    }

    public boolean isDefault() {
        return _default;
    }

    public void setDefault(boolean _default) {
        this._default = _default;
    }

    public boolean matches(Route route) {
        return false;
    }
}
