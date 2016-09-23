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

public class ResourceOperation {

    private String       name;
    private HttpMethod[] httpMethods;
    private PathPattern  pathPattern;
    private String       permissionTemplate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPermissionTemplate() {
        return permissionTemplate;
    }

    public void setPermissionTemplate(String permissionTemplate) {
        this.permissionTemplate = permissionTemplate;
    }

}
