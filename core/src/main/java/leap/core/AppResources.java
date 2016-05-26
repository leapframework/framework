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
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.PathMatcher;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;

import java.util.*;

@Internal
public class AppResources {

    private static final Log log = LogFactory.get(AppResources.class);

    private static final String PROFILE_SEPARATOR = "_";

    static final String CP_CORE_PREFIX      = "/META-INF/leap/core";
    static final String CP_FRAMEWORK_PREFIX = "/META-INF/leap/framework";
    static final String CP_MODULES_PREFIX   = "/META-INF/leap/modules/*";
    static final String CP_META_PREFIX      = "/META-INF/conf";
    static final String CP_APP_PREFIX       = "/conf";

    private static final String CP_CORE_LOCATION      = Strings.format("classpath*:{0}/**/*", CP_CORE_PREFIX);
    private static final String CP_FRAMEWORK_LOCATION = Strings.format("classpath*:{0}/**/*", CP_FRAMEWORK_PREFIX);
    private static final String CP_MODULES_LOCATION   = Strings.format("classpath*:{0}/**/*", CP_MODULES_PREFIX);
    private static final String CP_META_LOCATION      = Strings.format("classpath*:{0}/**/*", CP_META_PREFIX);
    private static final String CP_APP_LOCATION       = Strings.format("classpath*:{0}/**/*", CP_APP_PREFIX);

    private static final String CP_PROFILE_LOCATION   = Strings.format("classpath*:{0}" + PROFILE_SEPARATOR + "{profile}/**/*", CP_APP_PREFIX);
    private static final String CP_LOCAL_LOCATION     = Strings.format("classpath*:{0}" + PROFILE_SEPARATOR + "local/**/*",     CP_APP_PREFIX);

    private static final Map<AppConfig, AppResources> instances = new IdentityHashMap<>();

    public static AppResources get(AppConfig config) {
        AppResources inst = instances.get(config);
        if(null == inst) {
            throw new IllegalStateException("No resources for app config '" + config + "'");
        }
        return inst;
    }

    public static Resource[] convertTo(AppResource[] resources) {
        Resource[] rs = new Resource[resources.length];
        for(int i=0;i<rs.length;i++) {
            rs[i] = resources[i].getResource();
        }
        return rs;
    }

    public static AppResource[] convertFrom(ResourceSet resources) {
        return convertFrom(resources.toResourceArray(), false);
    }

    public static AppResource[] convertFrom(ResourceSet resources, boolean defaultOverride) {
        return convertFrom(resources.toResourceArray(), defaultOverride);
    }

    public static AppResource[] convertFrom(Resource[] resources) {
        return convertFrom(resources, false);
    }

    public static AppResource[] convertFrom(Resource[] resources, boolean defaultOverride) {
        AppResource[] ars = new AppResource[resources.length];
        for(int i=0;i<ars.length;i++) {
            ars[i] = new SimpleAppResource(resources[i], defaultOverride);
        }
        return ars;
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
    private final Map<String, Resource>    confClasspathResources = new HashMap<>();
    private final Map<String, AppResource> allResources           = new LinkedHashMap<>();
    private final boolean dev;

    private String[] defaultSearchPatterns;

    private AppResources(DefaultAppConfig config) {
        this.config = config;
        this.dev = AppConfig.PROFILE_DEVELOPMENT.equals(config.getProfile());

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
        if(dev) {
            Resources.scan(CP_LOCAL_LOCATION).forEach((r) -> add(r, true));
        }

        List<String> patterns = new ArrayList<>();

        //{0}{1} -> {name}{suffix}

        final String filePattern = "/{0}{1}";
        final String dirPattern  = "/{0}/**/*{1}";

        //add fixed search patterns.
        patterns.add(CP_CORE_PREFIX      + filePattern);
        patterns.add(CP_CORE_PREFIX      + dirPattern);
        patterns.add(CP_FRAMEWORK_PREFIX + filePattern);
        patterns.add(CP_FRAMEWORK_PREFIX + dirPattern);
        patterns.add(CP_MODULES_PREFIX   + filePattern);
        patterns.add(CP_MODULES_PREFIX   + dirPattern);
        patterns.add(CP_META_PREFIX      + filePattern);
        patterns.add(CP_META_PREFIX      + dirPattern);
        patterns.add(CP_APP_PREFIX       + filePattern);
        patterns.add(CP_APP_PREFIX       + dirPattern);

        //add profile search patterns.
        patterns.add(CP_APP_PREFIX + PROFILE_SEPARATOR + config.getProfile() + filePattern);
        patterns.add(CP_APP_PREFIX + PROFILE_SEPARATOR + config.getProfile() + dirPattern);

        //add local search patterns. (only for development profile).
        if(dev) {
            patterns.add(CP_APP_PREFIX + PROFILE_SEPARATOR + "local"  + filePattern);
            patterns.add(CP_APP_PREFIX + PROFILE_SEPARATOR + "local"  + dirPattern);
        }

        this.defaultSearchPatterns = patterns.toArray(new String[0]);
    }

    protected void add(Resource resource, boolean defaultOverride) {
        if(!resource.isReadable()) {
            return;
        }

        String url = resource.getURLString();
        if(allResources.containsKey(url)) {
            return;
        }

        //config files not in classpath or exists in jar allows multi.
        if(!resource.hasClasspath() || !resource.isFile()) {
            doAdd(resource, defaultOverride);
            return;
        }

        String classpath = resource.getClasspath();
        if(!isAppResource(classpath)) {
            doAdd(resource, defaultOverride);
            return;
        }

        ////conf/* does not allows multi resources with the same classpath.
        Resource old = confClasspathResources.get(classpath);
        if(null == old) {
            doAdd(resource, defaultOverride);
            confClasspathResources.put(classpath, resource);
            return;
        }

        //check should ignore or override the old one.
        if(dev) {

            //in dev environment, the test resource should override the main resource.
            if(isTestResource(resource)) {
                removeAndAdd(old, resource, defaultOverride);

                confClasspathResources.put(classpath, resource);
                return;
            }else{
                //ignore
            }

        }else{

            //in non-dev environment, if old one is test resource, it will be removed and use new one.
            if(isTestResource(old)) {
                removeAndAdd(old, resource, defaultOverride);
                confClasspathResources.put(classpath, resource);
                return;
            }

            //in non-dev environment, if old one not a test resource, use the old resource, just do nothing.
            if(log.isInfoEnabled()) {
                log.info("Ignore resource [{}], another one already loaded : {}",
                        resource.getURLString(),
                        old.getURLString());
            }else{
                System.out.println("Ignore resource [" + resource.getURLString() +
                                    "], another one already loaded : " + old.getURLString());
            }
        }

    }

    private void doAdd(Resource resource, boolean defaultOverride) {
        allResources.put(resource.getURLString(), new SimpleAppResource(resource, defaultOverride));
    }

    private void removeAndAdd(Resource oldOne, Resource newOne, boolean defaultOverride) {
        allResources.remove(oldOne.getURLString());
        doAdd(newOne,defaultOverride);
    }

    private boolean isAppResource(String cp) {
        return (Strings.startsWith(cp, "conf/") || Strings.startsWith(cp, "conf" + PROFILE_SEPARATOR));
    }

    private boolean isTestResource(Resource resource) {
        return resource.getURLString().indexOf("/test-classes/") > 0;
    }

    protected void clear() {
        confClasspathResources.clear();
        allResources.clear();
    }

    private static final String extractProfile(String url) {
        int index0 = url.lastIndexOf("/conf" + PROFILE_SEPARATOR);
        if(index0 > 0) {
            int index1 = url.indexOf('/', index0+1);
            if(index1 > index0) {
                return url.substring(index0,index1);
            }
        }
        return null;
    }

    public AppResource[] search(String name) {
        return searchClasspathResources(defaultSearchPatterns, name, ".*");
    }

    public AppResource[] search(String name, String suffix) {
        return searchClasspathResources(defaultSearchPatterns, name, suffix);
    }

    public AppResource[] searchAppFiles(String[] filenamePatterns) {
        List<String> patterns = new ArrayList<>();

        addAppFiles(patterns, filenamePatterns);

        return searchClasspathResources(patterns.toArray(new String[0]));
    }

    public AppResource[] searchAllFiles(String[] filenamePatterns) {
        List<String> patterns = new ArrayList<>();

        addCFMMFiles(patterns, filenamePatterns);
        addAppFiles(patterns,  filenamePatterns);

        return searchClasspathResources(patterns.toArray(new String[0]));
    }

    //CFMM -> core, framework, module, meta
    private void addCFMMFiles(List<String> patterns, String[] filenames) {
        // core
        for(String filename : filenames) {
            patterns.add(CP_CORE_PREFIX + "/" + filename);
        }

        // framework
        for(String filename : filenames) {
            patterns.add(CP_FRAMEWORK_PREFIX + "/" + filename);
        }

        // modules
        for(String filename : filenames) {
            patterns.add(CP_MODULES_PREFIX + "/" + filename);
        }

        // meta
        for(String filename : filenames) {
            patterns.add(CP_META_PREFIX + "/" + filename);
        }
    }

    private void addAppFiles(List<String> patterns, String[] filenames) {
        // conf/{filename}
        for(String filename : filenames) {
            patterns.add(CP_APP_PREFIX + "/" + filename);
        }

        // conf-{profile}/{filename}
        for(String filename : filenames) {
            patterns.add(CP_APP_PREFIX + PROFILE_SEPARATOR + config.getProfile() + "/" + filename);
        }

        // conf-local/{filename}
        if(dev) {
            for(String filename : filenames) {
                patterns.add(CP_APP_PREFIX + PROFILE_SEPARATOR + "local/" + filename);
            }
        }
    }

    private AppResource[] searchClasspathResources(String[] patterns, String name, String suffix){

        String[] namedPatterns = new String[patterns.length];

        for(int i = 0; i< namedPatterns.length; i++){
            namedPatterns[i] = Strings.format(patterns[i],name, suffix);
        }

        return searchClasspathResources(namedPatterns);
    }

    public AppResource[] searchClasspathResources(String[] patterns){

        List<AppResource> list = new ArrayList<>();

        final PathMatcher matcher = Resources.getPathMatcher();

        for(String namedPattern : patterns){
            if(namedPattern.startsWith("/")){
                namedPattern = namedPattern.substring(1);
            }

            for(AppResource r  : allResources.values()) {
                if(null != r.getResource().getClasspath()) {
                    String cp = r.getResource().getClasspath();

                    if(matcher.match(namedPattern, cp)) {
                        list.add(r);
                    }
                }
            }

        }

        return list.toArray(new AppResource[0]);
    }

    public static Resource getAppClasspathDirectory(String name) {
        return Resources.getResource("classpath:" + CP_APP_PREFIX + "/" + name);
    }

}
