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

import leap.web.api.meta.model.MPermission;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class ResourcePermissions {

    private final Set<Class<?>>          resourceClasses  = new HashSet<>();
    private final Set<String>            resourcePackages = new HashSet<>();
    private final Set<ResourceOperation> operations       = new LinkedHashSet<>();
    private final Set<MPermission>       permissions      = new LinkedHashSet<>();

    public Set<Class<?>> getResourceClasses() {
        return resourceClasses;
    }

    public Set<String> getResourcePackages() {
        return resourcePackages;
    }

    public Set<ResourceOperation> getOperations() {
        return operations;
    }

    public Set<MPermission> getPermissions() {
        return permissions;
    }

}