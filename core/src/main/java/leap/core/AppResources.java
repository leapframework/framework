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
import leap.lang.path.PathMatcher;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;

import java.util.*;

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

    private static final String CP_PROFILE_LOCATION   = Strings.format("classpath*:{0}-{profile}/**/*", CP_APP_PREFIX);
    private static final String CP_LOCAL_LOCATION     = Strings.format("classpath*:{0}-local/**/*",     CP_APP_PREFIX);

    private static final Map<AppConfig, AppResources> instances = new IdentityHashMap<>();

    public static AppResources get(AppConfig config) {
        AppResources inst = instances.get(config);
        if(null == inst) {
            throw new IllegalStateException("No resources for app config '" + config + "'");
        }
        return inst;
    }

    static void destroy(AppConfig config) {
        AppResources inst = instances.get(config);
        if(null != inst) {
            inst.clear();
            instances.remove(config);
        }
    }

    static AppResources create(DefaultAppConfig config) {
        AppResources inst = new AppResources(config);

        instances.put(config, inst);

        return inst;
    }

    public static boolean isFrameworkResource(String url) {
        return url.contains(CP_CORE_PREFIX) || url.contains(CP_FRAMEWORK_PREFIX);
    }

    private final DefaultAppConfig         config;
    private final Map<String, AppResource> resources = new LinkedHashMap<>();
    private final boolean                  devProfile;

    private String[] defaultSearchPatterns;

    private AppResources(DefaultAppConfig config) {
        this.config = config;
        this.devProfile = AppConfig.PROFILE_DEVELOPMENT.equals(config.getProfile());

        init();
    }

    protected void init() {

        //load fixed resources.
        Resources.scan(CP_CORE_LOCATION).forEach((r) -> add(r, false));
        Resources.scan(CP_FRAMEWORK_LOCATION).forEach((r) -> add(r, false));
        Resources.scan(CP_MODULES_LOCATION).forEach((r) -> add(r, false));
        Resources.scan(CP_META_LOCATION).forEach((r) -> add(r, false));
        Resources.scan(CP_APP_LOCATION).forEach((r) -> add(r, false));

        //load profile resources.
        Resources.scan(config.getProfiled(new String[]{CP_PROFILE_LOCATION})).forEach((r) -> add(r, true));

        //load local resources(only for development profile).
        if(devProfile) {
            Resources.scan(CP_LOCAL_LOCATION).forEach((r) -> add(r, true));
        }

        List<String> patterns = new ArrayList<>();

        //add fixed search patterns.
        patterns.add(CP_CORE_PREFIX      + "/{0}.*");
        patterns.add(CP_CORE_PREFIX      + "/{0}/**/*");
        patterns.add(CP_FRAMEWORK_PREFIX + "/{0}.*");
        patterns.add(CP_FRAMEWORK_PREFIX + "/{0}/**/*");
        patterns.add(CP_MODULES_PREFIX   + "/{0}.*");
        patterns.add(CP_MODULES_PREFIX   + "/{0}/**/*");
        patterns.add(CP_META_PREFIX      + "/{0}.*");
        patterns.add(CP_META_PREFIX      + "/{0}/**/*");
        patterns.add(CP_APP_PREFIX       + "/{0}.*");
        patterns.add(CP_APP_PREFIX       + "/{0}/**/*");

        //add profile search patterns.
        patterns.add(CP_APP_PREFIX + "-" + config.getProfile() + "/{0}.*");
        patterns.add(CP_APP_PREFIX + "-" + config.getProfile() + "/{0}/**/*");

        //add local search patterns. (only for development profile).
        if(devProfile) {
            patterns.add(CP_APP_PREFIX + "-local/{0}.*");
            patterns.add(CP_APP_PREFIX + "-local/{0}/**/*");
        }

        this.defaultSearchPatterns = patterns.toArray(new String[0]);
    }

    protected void add(Resource resource, boolean defaultOverride) {
        String url = resource.getURLString();

        AppResource old = resources.get(url);
        if(null != old) {

            String oldClassPath = old.getResource().getClasspath();
            String newClassPath = resource.getClasspath();

            boolean override = false;
            if(Strings.startsWith(oldClassPath, "/conf/") && oldClassPath.equals(newClassPath)) {
                override = true;
            }

            if(!override) {
                return;
            }
        }

        resources.put(url, new SimpleAppResource(resource, defaultOverride));
    }

    protected void clear() {
        resources.clear();
    }

    private static final String extractProfile(String url) {
        int index0 = url.lastIndexOf("/conf-");
        if(index0 > 0) {
            int index1 = url.indexOf('/', index0+1);
            if(index1 > index0) {
                return url.substring(index0,index1);
            }
        }
        return null;
    }

    public AppResource[] search(String name) {
        return searchClasspathResources(defaultSearchPatterns, name);
    }

    public AppResource[] searchConfFiles(String[] filenames) {
        List<String> patterns = new ArrayList<>();

        // conf/{filename}
        for(String filename : filenames) {
            patterns.add(CP_APP_PREFIX + "/" + filename);
        }

        // conf-{profile}/{filename}
        for(String filename : filenames) {
            patterns.add(CP_APP_PREFIX +  "-" + config.getProfile() + "/" + filename);
        }

        // conf-local/{filename}
        if(devProfile) {
            for(String filename : filenames) {
                patterns.add(CP_APP_PREFIX +  "-local/" + filename);
            }
        }

        return searchClasspathResources(patterns.toArray(new String[0]));
    }

    private AppResource[] searchClasspathResources(String[] patterns, String name){

        String[] namedPatterns = new String[patterns.length];

        for(int i = 0; i< namedPatterns.length; i++){
            namedPatterns[i] = Strings.format(patterns[i],name);
        }

        return searchClasspathResources(namedPatterns);
    }

    public AppResource[] searchClasspathResources(String[] patterns){

        List<AppResource> list = new ArrayList<>();

        final PathMatcher matcher = Resources.getPathMatcher();

        for(AppResource r : resources.values()){

            if(null != r.getResource().getClasspath()) {

                for(String namedPattern : patterns){
                    if(namedPattern.startsWith("/")){
                        namedPattern = namedPattern.substring(1);
                    }

                    if(matcher.match(namedPattern, r.getResource().getClasspath())) {
                        list.add(r);
                        break;
                    }
                }
            }
        }

        return list.toArray(new AppResource[0]);
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
