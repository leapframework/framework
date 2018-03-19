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

import leap.lang.*;
import leap.lang.annotation.Internal;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.logging.LogUtils;
import leap.lang.net.Urls;
import leap.lang.path.PathMatcher;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import leap.lang.servlet.ServletResource;
import leap.lang.servlet.Servlets;
import leap.lang.servlet.SimpleServletResource;

import javax.servlet.ServletContext;
import java.util.*;

@Internal
public class AppResources {

    private static final Log log = LogFactory.get(AppResources.class);

    private static final String PROFILE_SEPARATOR = "_";

    static final String CP_CORE_PREFIX      = "META-INF/leap/core";
    static final String CP_FRAMEWORK_PREFIX = "META-INF/leap/framework";
    static final String CP_MODULES_PREFIX   = "META-INF/leap/modules/*";
    static final String CP_META_PREFIX      = "META-INF/conf";
    static final String CP_APP_PREFIX       = "conf";

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

    public static AppResources tryGet(AppConfig config) {
        return instances.get(config);
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

    static AppResources create(DefaultAppConfig config, Object externalContext) {
        AppResources inst = new AppResources(config, externalContext);

        instances.put(config, inst);

        return inst;
    }

    public static boolean isFrameworkAndCoreResource(String url) {
        return url.contains(CP_CORE_PREFIX) || url.contains(CP_FRAMEWORK_PREFIX);
    }

    protected static boolean isCoreResource(String cp) {
        return null != cp && cp.startsWith(CP_CORE_PREFIX);
    }

    protected static boolean isFrameworkResource(String cp) {
        return null != cp && cp.startsWith(CP_FRAMEWORK_PREFIX);
    }

    protected static boolean isModuleResource(String cp) {
        return null != cp && cp.startsWith(CP_MODULES_PREFIX);
    }

    protected static boolean isMetaResource(String cp) {
        return null != cp && cp.startsWith(CP_META_PREFIX);
    }

    protected static boolean isAppResource(String cp) {
        return null != cp &&
                (cp.startsWith(CP_APP_PREFIX) || cp.startsWith(CP_APP_PREFIX + PROFILE_SEPARATOR));
    }

    protected static boolean isAppProfiledResource(String cp) {
        return null != cp && cp.startsWith(CP_APP_PREFIX + PROFILE_SEPARATOR);
    }

    protected static boolean isJarResource(Resource resource) {
        return Try.throwUncheckedWithResult(() -> Urls.isJarUrl(resource.getURL()));
    }

    protected static boolean isTestResource(Resource resource) {
        return resource.getURLString().indexOf("/test-classes/") > 0;
    }

    private final DefaultAppConfig config;
    private final Set<String>      resourceUrls    = new HashSet<>();
    private final Set<AppResource> sortedResources = new TreeSet<>(new ResourceComparator());
    private final boolean dev;

    private String[] defaultSearchPatterns;

    private AppResources(DefaultAppConfig config, Object externalContext) {
        this.config = config;
        this.dev    = AppConfig.PROFILE_DEVELOPMENT.equals(config.getProfile());
        init(externalContext);
    }

    protected void init(Object externalContext) {
        //load fixed resources.
        Resources.scan(CP_CORE_LOCATION).forEach(this::add);
        Resources.scan(CP_FRAMEWORK_LOCATION).forEach(this::add);
        Resources.scan(CP_MODULES_LOCATION).forEach(this::add);
        Resources.scan(CP_META_LOCATION).forEach(this::add);

        loadServletContextResources(externalContext);

        Resources.scan(CP_APP_LOCATION).forEach(this::add);

        //load profile resources.
        Resources.scan(config.getProfiled(new String[]{CP_PROFILE_LOCATION})).forEach(this::add);

        //load local resources(only for development profile).
        /*
        if(dev) {
            Resources.scan(CP_LOCAL_LOCATION).forEach(this::add);
        }
        */
        if(log.isDebugEnabled()) {
            for(AppResource ar : sortedResources) {

                Resource r = ar.getResource();
                if(isMetaResource(r.getClasspath()) || isAppResource(r.getClasspath())) {
                    log.debug("Found conf file : {}", LogUtils.getUrl(r));
                }else{
                    log.trace("Found conf file : {}", LogUtils.getUrl(r));
                }
            }
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

    protected int resolveSortOrder(Resource resource) {
        /* sort orders :
                core         : 0
                framework    : 1
                modules      : 2
                meta         : 3

                jar:conf     : 4
                jar:conf_*   : 5

                war:conf     : 6
                war:conf_*   : 7

                main:conf    : 8
                main:conf_*  : 9

                test:conf    : 10
                test:conf_*  : 11
         */

        int order = Integer.MAX_VALUE;

        String cp = resource.getClasspath();
        if(null != cp) {
            if(isCoreResource(cp)) {
                order = 0;
            }else if(isFrameworkResource(cp)) {
                order = 1;
            }else if(isModuleResource(cp)) {
                order = 2;
            }else if(isMetaResource(cp)) {
                order = 3;
            }else if(isAppResource(cp)) {

                if(isJarResource(resource)) {
                    order = 4;
                }else if(isTestResource(resource)) {
                    order = 10;
                }else {
                    order = 8;
                }

                if(isAppProfiledResource(cp)) {
                    order += 1;
                }
            }
        }else {
            if(resource instanceof ServletResource) {
                order = 6;
            }
        }

        return order;
    }

    protected boolean resolveDefaultOverride(Resource resource) {
        boolean defaultOverride = false;

        String cp = resource.getClasspath();
        if (null != cp) {
            if (isAppResource(cp) && isTestResource(resource)) {
                defaultOverride = true;
            }
        }

        return defaultOverride;
    }

    protected void add(Resource resource) {
        add(resource, resource.getClasspath());
    }

    protected void add(Resource resource, String path) {
        if(!resource.isReadable()) {
            return;
        }

        if(resource.isFile() && resource.getFile().isDirectory()) {
            return;
        }

        String url = resource.getURLString();
        if(resourceUrls.contains(url)) {
            return;
        }

        //in non-dev environment, the test resources should not be loaded.
        /*
        if(!dev && isTestResource(resource)) {
            return;
        }
        */

        int order = resolveSortOrder(resource);
        boolean defaultOverride = resolveDefaultOverride(resource);

        doAdd(resource, defaultOverride, order, path);
    }

    private void doAdd(Resource resource, boolean defaultOverride, int order, String path) {
        resourceUrls.add(resource.getURLString());
        sortedResources.add(new SimpleAppResource(resource, defaultOverride, order, path));
    }

    protected void clear() {
        resourceUrls.clear();
        sortedResources.clear();
    }

    public AppResource[] search(String name) {
        return searchResources(defaultSearchPatterns, name, ".*");
    }

    public AppResource[] search(String name, String suffix) {
        return searchResources(defaultSearchPatterns, name, suffix);
    }

    public AppResource[] searchAppFiles(String[] filenamePatterns) {
        List<String> patterns = new ArrayList<>();

        addAppFiles(patterns, filenamePatterns);

        return searchResources(patterns.toArray(new String[0]));
    }

    public AppResource[] searchAllFiles(String[] filenamePatterns) {
        List<String> patterns = new ArrayList<>();

        addCFMMFiles(patterns, filenamePatterns);
        addAppFiles(patterns,  filenamePatterns);

        return searchResources(patterns.toArray(new String[0]));
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
        /*
        if(dev) {
            for(String filename : filenames) {
                patterns.add(CP_APP_PREFIX + PROFILE_SEPARATOR + "local/" + filename);
            }
        }
        */
    }

    private AppResource[] searchResources(String[] patterns, String name, String suffix){

        String[] namedPatterns = new String[patterns.length];

        for(int i = 0; i< namedPatterns.length; i++){
            namedPatterns[i] = Strings.format(patterns[i],name, suffix);
        }

        return searchResources(namedPatterns);
    }

    private void loadServletContextResources(Object context) {
        if(!Classes.isPresent("javax.servlet.ServletContext")) {
            return;
        }

        final String searchPath = "/WEB-INF/conf/";
        final String pathPrefix = "/WEB-INF/";

        if(context instanceof ServletContext) {
            loadServletContextResources((ServletContext)context, searchPath, pathPrefix);
        }
    }

    private void loadServletContextResources(ServletContext sc, String searchPath, String pathPrefix) {
        Set<String> paths = sc.getResourcePaths(searchPath);
        if(paths != null){
            for(String path : paths) {
                loadServletContextResource(sc, path, pathPrefix);
            }
        }
    }

    private void loadServletContextResource(ServletContext sc, String path, String pathPrefix) {
        ServletResource resource = new SimpleServletResource(sc, path);
        if(!resource.isDirectory()) {
            add(resource, Strings.removeStart(path, pathPrefix));
        }else{
            loadServletContextResources(sc, path, pathPrefix);
        }
    }

    public AppResource[] searchResources(String[] patterns){
        Set<AppResource> set = new TreeSet<>(new ResourceComparator());

        final PathMatcher matcher = Resources.getPathMatcher();

        for(String namedPattern : patterns){
            if(namedPattern.startsWith("/")){
                namedPattern = namedPattern.substring(1);
            }

            for(AppResource r  : sortedResources) {
                if(null != r.getPath()) {
                    String path = r.getPath();

                    if(matcher.match(namedPattern, path)) {
                        set.add(r);
                    }
                }
            }

        }

        return set.toArray(new AppResource[0]);
    }

    public static Resource getAppClasspathDirectory(String name) {
        return Resources.getResource("classpath:" + CP_APP_PREFIX + "/" + name);
    }

    protected static final class ResourceComparator implements Comparator<AppResource> {

        @Override
        public int compare(AppResource o1, AppResource o2) {
            if(o1 == o2) {
                return 1;
            }

            if(o1.getSortOrder() > o2.getSortOrder()) {
                return 1;
            }

            if(o1.getSortOrder() < o2.getSortOrder()) {
                return -1;
            }

            Resource r1 = o1.getResource();
            Resource r2 = o2.getResource();

            if(r1.hasClasspath() && r2.hasClasspath()) {
                return comparePaths(r1.getClasspath(), r2.getClasspath());
            }else{
                return r1.getURLString().length() > r2.getURLString().length() ? -1 : 1;
            }
        }

        private int comparePaths(String p1, String p2) {
            String dir1 = Paths.getDirPath(p1);
            String dir2 = Paths.getDirPath(p2);

            String file1 = Paths.getFileName(p1);
            String file2 = Paths.getFileName(p2);

            if(dir1.equals(dir2)) {
                return result(file1.compareTo(file2));
            }else if(dir1.startsWith(dir2)) {
                return -1;
            }else if(dir2.startsWith(dir1)) {
                return 1;
            }else {
                return result(dir1.compareTo(dir2));
            }
        }

        private int result(int r) {
            return r == 0 ? -1 : r;
        }
    }

}
