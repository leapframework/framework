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

import leap.lang.Strings;
import leap.lang.annotation.Internal;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;

import java.util.ArrayList;
import java.util.List;

@Internal
public class AppResources {

    private static ThreadLocal<ResourceSet> ctx;

    private static final String XML_EXT = ".xml";

    static final String CP_CORE_PREFIX      = "/META-INF/leap/core";
    static final String CP_FRAMEWORK_PREFIX = "/META-INF/leap/framework";
    static final String CP_MODULES_PREFIX   = "/META-INF/leap/modules/*";
    static final String CP_META_PREFIX      = "/META-INF/conf";
    static final String CP_APP_PREFIX       = "/conf";

    private static final String[] CP_FRAMEWORK_TEMPLATES = new String[]{
            CP_CORE_PREFIX + "/{0}{1}",
            CP_CORE_PREFIX + "/{0}/**/*{1}",
            CP_FRAMEWORK_PREFIX + "/{0}{1}",
            CP_FRAMEWORK_PREFIX + "/{0}/**/*{1}"
    };

    private static final String[] CP_MODULES_TEMPLATES = new String[]{
            CP_MODULES_PREFIX + "/{0}{1}",
            CP_MODULES_PREFIX + "/{0}/**/*{1}"
    };

    private static final String[] CP_META_TEMPLATES = new String[]{
            CP_META_PREFIX + "/{0}{1}",
            CP_META_PREFIX + "/{0}/**/*{1}"
    };

    private static final String[] CP_APP_TEMPLATES = new String[]{
            CP_APP_PREFIX + "/{0}{1}",
            CP_APP_PREFIX + "/{0}/**/*{1}"
    };

    private static final String[] CP_CORE_TEMPLATES_FOR_PATTERN = new String[]{
            CP_CORE_PREFIX + "/{0}{1}"
    };

    private static final String[] CP_FRAMEWORK_TEMPLATES_FOR_PATTERN = new String[]{
            CP_FRAMEWORK_PREFIX + "/{0}{1}"
    };

    private static final String[] CP_MODULES_TEMPLATES_FOR_PATTERN = new String[]{
            CP_MODULES_PREFIX + "/{0}{1}"
    };

    private static final String[] CP_META_TEMPLATES_FOR_PATTERN = new String[]{
            CP_META_PREFIX + "/{0}{1}"
    };

    private static final String[] CP_APP_TEMPLATES_FOR_PATTERN = new String[]{
            CP_APP_PREFIX + "/{0}{1}"
    };

    private static final String CP_CORE_LOCATION      = Strings.format("classpath*:{0}/**/*", CP_CORE_PREFIX);
    private static final String CP_FRAMEWORK_LOCATION = Strings.format("classpath*:{0}/**/*", CP_FRAMEWORK_PREFIX);
    private static final String CP_MODULES_LOCATION   = Strings.format("classpath*:{0}/**/*", CP_MODULES_PREFIX);
    private static final String CP_META_LOCATION      = Strings.format("classpath*:{0}/**/*", CP_META_PREFIX);
    private static final String CP_APP_LOCATION       = Strings.format("classpath*:{0}/**/*", CP_APP_PREFIX);

    public static boolean isFrameworkResource(String url) {
        return url.contains(CP_CORE_PREFIX) || url.contains(CP_FRAMEWORK_PREFIX);
    }

    /**
     * Returns all the resource(s).
     */
    public static ResourceSet get() {
        if(ctx != null){
            ResourceSet resources = ctx.get();

            if(null == resources){
                resources = Resources.scan(CP_CORE_LOCATION,
                                            CP_FRAMEWORK_LOCATION,
                                            CP_MODULES_LOCATION,
                                            CP_META_LOCATION,
                                            CP_APP_LOCATION);
                ctx.set(resources);
            }
            return resources;

        }else{
            return Resources.scan(CP_CORE_LOCATION,
                                    CP_FRAMEWORK_LOCATION,
                                    CP_MODULES_LOCATION,
                                    CP_META_LOCATION,
                                    CP_APP_LOCATION);
        }
    }

    public static Resource getAppClasspathDirectory(String name) {
        return Resources.getResource("classpath:" + CP_APP_PREFIX + "/" + name);
    }

    public static Resource[] getLocClasspathResources(String[] locations) {
        List<Resource> list = new ArrayList<>();

        ResourceSet rs = get();

        Resource[] resources = rs.searchClasspaths(locations);
        for(Resource resource : resources){
            list.add(resource);
        }

        return list.toArray(new Resource[list.size()]);
    }

    public static Resource[] getAllClasspathResources(String name, String ext){
        List<Resource> list = new ArrayList<>();

        ResourceSet rs = get();

        searchClasspathResources(list, CP_FRAMEWORK_TEMPLATES, rs, name, ext);
        searchClasspathResources(list, CP_MODULES_TEMPLATES,   rs, name, ext);
        searchClasspathResources(list, CP_META_TEMPLATES,      rs, name, ext);
        searchClasspathResources(list, CP_APP_TEMPLATES,       rs, name, ext);

        return list.toArray(new Resource[list.size()]);
    }

    public static Resource[] getAllClasspathResourcesForXml(String name){
        return getAllClasspathResources(name, XML_EXT);
    }

    public static Resource[] getAllClasspathResourcesWithPattern(String pattern, String ext){
        List<Resource> list = new ArrayList<>();

        ResourceSet rs = get();

        searchClasspathResources(list, CP_CORE_TEMPLATES_FOR_PATTERN,      rs, pattern, ext);
        searchClasspathResources(list, CP_FRAMEWORK_TEMPLATES_FOR_PATTERN, rs, pattern, ext);
        searchClasspathResources(list, CP_MODULES_TEMPLATES_FOR_PATTERN,   rs, pattern, ext);
        searchClasspathResources(list, CP_META_TEMPLATES_FOR_PATTERN,      rs, pattern, ext);
        searchClasspathResources(list, CP_APP_TEMPLATES_FOR_PATTERN,       rs, pattern, ext);

        return list.toArray(new Resource[list.size()]);
    }

    /**
     * FM -> Framework, Module.
     */
    public static Resource[] getFMClasspathResources(String name, String ext){
        List<Resource> list = new ArrayList<>();

        ResourceSet rs = get();

        searchClasspathResources(list, CP_FRAMEWORK_TEMPLATES, rs, name, ext);
        searchClasspathResources(list, CP_MODULES_TEMPLATES,   rs, name, ext);

        return list.toArray(new Resource[list.size()]);
    }

    /**
     * FM -> Framework and Module.
     */
    public static Resource[] getFMClasspathResourcesForXml(String name){
        return getFMClasspathResources(name, XML_EXT);
    }

    /**
     * FMM -> Framework, Module, Meta.
     */
    public static Resource[] getFMMClasspathResourcesForXml(String name){
        return getFMMClasspathResources(name, XML_EXT);
    }

    /**
     * FMM -> Framework, Module, Meta.
     */
    public static Resource[] getFMMClasspathResources(String name, String ext){
        List<Resource> list = new ArrayList<>();

        ResourceSet rs = get();

        searchClasspathResources(list, CP_FRAMEWORK_TEMPLATES, rs, name, ext);
        searchClasspathResources(list, CP_MODULES_TEMPLATES,   rs, name, ext);
        searchClasspathResources(list, CP_META_TEMPLATES,      rs, name, ext);

        return list.toArray(new Resource[list.size()]);
    }

    public static Resource[] getMetaClasspathResources(String name, String ext){
        List<Resource> list = new ArrayList<>();

        ResourceSet rs = get();

        searchClasspathResources(list, CP_META_TEMPLATES, rs, name, ext);

        return list.toArray(new Resource[list.size()]);
    }

    public static Resource[] getMetaClasspathResourcesForXml(String name){
        return getMetaClasspathResources(name, XML_EXT);
    }

    public static Resource[] getAppClasspathResources(String name, String ext){
        List<Resource> list = new ArrayList<>();

        ResourceSet rs = get();

        searchClasspathResources(list, CP_APP_TEMPLATES, rs, name, ext);

        return list.toArray(new Resource[list.size()]);
    }

    public static Resource[] getAppClasspathResourcesForXml(String xmlResourceName){
        return getAppClasspathResources(xmlResourceName, XML_EXT);
    }

    static void onContextInitializing() {
        ctx = new InheritableThreadLocal<>();
    }

    static void onContextInitialized() {
        ctx.remove();
        ctx = null;
    }

    private static void searchClasspathResources(List<Resource> list,
                                                 String[] templateLocations,
                                                 ResourceSet rs,
                                                 String resourceName,
                                                 String ext){

        String[] locations = new String[templateLocations.length];

        for(int i=0; i<locations.length; i++){
            locations[i] = Strings.format(templateLocations[i],resourceName,ext);
        }

        Resource[] resources = rs.searchClasspaths(locations);
        for(Resource resource : resources){
            list.add(resource);
        }
    }
}
