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

import leap.lang.Args;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ResourcePermissionsSet {

    private final Set<ResourcePermissions>           allResourcePermissions     = new LinkedHashSet<>();
    private final Map<String, ResourcePermissions>   resourceClassPermissions   = new HashMap<>();
    private final Map<String, ResourcePermissions>   resourcePackagePermissions = new HashMap<>();

    public Set<ResourcePermissions> getAllResourcePermissions() {
        return allResourcePermissions;
    }

    public ResourcePermissions tryGetResourcePermissions(Class<?> resourceClass) {

        ResourcePermissions rps = resourceClassPermissions.get(resourceClass);
        if(null != rps) {
            return rps;
        }

        for(Map.Entry<String, ResourcePermissions> entry : resourcePackagePermissions.entrySet()) {

            if(resourceClass.getName().startsWith(entry.getKey())) {
                return entry.getValue();
            }

        }

        return null;
    }

    public void addResourcePermissions(ResourcePermissions rps) {
        Args.notNull(rps);

        //check duplicated
        rps.getResourceClasses().forEach(c -> {

            if(resourceClassPermissions.containsKey(c)) {
                throw new IllegalStateException("Duplicate permissions definition of resource class '" + c + "'");
            }

            resourceClassPermissions.put(c, rps);
        });

        rps.getResourcePackages().forEach(p -> {

            if(!p.endsWith(".")) {
                p = p + ".";
            }

            if(resourcePackagePermissions.containsKey(p)) {
                throw new IllegalStateException("Duplicate permissions definition of resource package '" + p + "'");
            }

            resourcePackagePermissions.put(p, rps);
        });

        allResourcePermissions.add(rps);
    }

}