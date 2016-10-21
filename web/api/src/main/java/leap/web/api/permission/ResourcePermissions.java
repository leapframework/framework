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

import leap.lang.New;
import leap.lang.el.spel.SPEL;
import leap.web.api.meta.model.MPermission;
import leap.web.route.Route;

import java.util.*;

public class ResourcePermissions {

    private final Set<Class<?>>           resourceClasses  = new HashSet<>();
    private final Set<String>             resourcePackages = new HashSet<>();
    private final Set<ResourcePermission> permissions      = new TreeSet<>(ResourcePermission.COMPARATOR);

    private ResourcePermission defaultPermission;

    public Set<Class<?>> getResourceClasses() {
        return resourceClasses;
    }

    public Set<String> getResourcePackages() {
        return resourcePackages;
    }

    public Set<ResourcePermission> getPermissions() {
        return permissions;
    }

    public ResourcePermission getDefaultPermission() {
        return defaultPermission;
    }

    public void setDefaultPermission(ResourcePermission defaultPermission) {
        this.defaultPermission = defaultPermission;
    }

    public void addResourceClass(Class<?> c) {
        resourceClasses.add(c);
    }

    public void addResourcePackage(String p) {
        resourcePackages.add(p);
    }

    public void addPermission(ResourcePermission p) {
        if(p.isDefault()) {
            if(null != defaultPermission) {
                throw new IllegalStateException("Duplicated default permission '" + p.getValue() + "'");
            }
            this.defaultPermission = p;
        }

        permissions.add(p);
    }

    public MPermission[] resolvePermissions(Route route, Class<?> resourceType) {
        List<MPermission> list = New.arrayList();

        String resourceName = resourceType.getSimpleName();
        Map<String,Object> vars = New.hashMap("resource", resourceName);

        permissions.forEach(p -> {

            if(p.matches(route)) {
                String value = eval(p.getValue(), vars);
                String desc  = eval(p.getDescription(), vars);

                list.add(new MPermission(value, desc));
            }

        });

        if(list.isEmpty() && null != defaultPermission) {
            list.add(new MPermission(defaultPermission.getValue(), defaultPermission.getDescription()));
        }

        return list.toArray(new MPermission[0]);
    }

    protected String eval(String value, Map<String, Object> vars) {
        return (String)SPEL.createCompositeExpression(value).getValue(vars);
    }

}