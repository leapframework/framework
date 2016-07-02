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

import leap.agent.Agent;
import leap.core.instrument.AppInstrumentClass;
import leap.core.instrument.AppInstrumentation;
import leap.core.instrument.ClassDependency;
import leap.core.instrument.ClassDependencyResolver;
import leap.lang.Classes;
import leap.lang.Exceptions;
import leap.lang.Factory;
import leap.lang.annotation.Internal;
import leap.lang.exception.NestedClassNotFoundException;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

@Internal
public class AppClassLoader extends ClassLoader {

    private static final Log log = LogFactory.get(AppClassLoader.class);

    private static final ThreadLocal<Set<String>> beanClassNamesLocal = new ThreadLocal<>();

    private static ThreadLocal<AppClassLoader>     instanceLocal;
    private static Map<ClassLoader,AppClassLoader> instances = new IdentityHashMap<>();

    @Internal
    public static AppClassLoader get() {
        return instanceLocal == null ? null : instanceLocal.get();
    }

    static AppClassLoader init(ClassLoader parent)  {
        if(null == instanceLocal) {
            instanceLocal = new ThreadLocal<>();
        }
        AppClassLoader inst = instances.get(parent);
        if(null == inst) {
            inst = new AppClassLoader(parent);
            instances.put(parent, inst);
        }
        instanceLocal.set(inst);
        return inst;
    }

    public static void addBeanClass(String name) {
        Set<String> names = beanClassNamesLocal.get();
        if(null == names) {
            names = new HashSet<>();
            beanClassNamesLocal.set(names);
        }
        names.add(name);
    }

    public static boolean isBeanClass(String name) {
        Set<String> names = beanClassNamesLocal.get();
        if(null == names) {
            return false;
        }

        return names.contains(name);
    }

    private final ClassLoader parent;

    private final Set<String>                       loadedUrls         = new HashSet<>();
    private final Set<String>                       loadedNames        = new HashSet<>();
    private final AppInstrumentation                instrumentation    = Factory.newInstance(AppInstrumentation.class);
    private final ClassDependencyResolver           dependencyResolver = Factory.newInstance(ClassDependencyResolver.class);
    private final Set<String>                       instrumenting      = new HashSet<>();
    private final Map<Class<?>, AppInstrumentClass> redefineClasses    = new LinkedHashMap<>();
    private final Map<String, AppInstrumentClass>   failedClasses      = new HashMap<>();

    private Method  parentLoaderDefineClass;
    private Method  parentFindLoadedClass;

    private AppConfig config;
    private String    basePackage;
    private boolean   testing;

    private RedefineClassLoader redefineClassLoader;

    private AppClassLoader(ClassLoader parent) {
        this.parent  = parent;
        this.testing = AppContextInitializer.isTesting();

        try {
            parentLoaderDefineClass =
                    ClassLoader.class.getDeclaredMethod("defineClass",
                        new Class[] {String.class, byte[].class, int.class,int.class});
            parentLoaderDefineClass.setAccessible(true);

            parentFindLoadedClass =
                    ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            parentFindLoadedClass.setAccessible(true);
        } catch (Exception e) {
            throw Exceptions.uncheck(e);
        }
    }

    void load(AppConfig config) {
        this.config      = config;
        this.basePackage = config.getBasePackage() + ".";
        instrumentation.init(config);
        loadAllClasses();
    }

    private void loadAllClasses() {
        log.debug("Try instrument all classes in app configured resources.");
        config.getResources().forEach(resource -> {

            if(resource.exists()) {
                String filename = resource.getFilename();

                if(null != filename &&
                        filename.endsWith(Classes.CLASS_FILE_SUFFIX)) {

                    try {
                        instrumentClass(null, resource, true);
                    } catch (ClassNotFoundException e) {
                        throw new NestedClassNotFoundException(e);
                    }
                }
            }
        });
    }

    void done() {
        //todo : clear loaded classes info.
        redefine();
        //loadedUrls.clear();
        //loadedNames.clear();
        instrumenting.clear();
        beanClassNamesLocal.remove();
    }

    @Internal
    public boolean hasFailedClasses() {
        return !failedClasses.isEmpty();
    }

    @Internal
    public Class<?> redefineTestClass(Class<?> c) {
        if (null == redefineClassLoader) {
            redefineClassLoader = new RedefineClassLoader();
        }
        return redefineClassLoader.redefineClass(c);
    }

    private void redefine() {
        if(!redefineClasses.isEmpty()) {
            log.warn("Redefining {} classes by agent...", redefineClasses.size());
            //redefine by agent.
            if(!Agent.redefine(redefineClasses)){
                if(!testing) {
                    log.warn("Agent redefine failed!");
                    for (AppInstrumentClass ic : redefineClasses.values()) {
                        if (ic.isEnsure()) {
                            throw new IllegalStateException("Class '" + ic.getClassName() + "' already loaded by '" +
                                    parent.getClass().getName() + "', cannot instrument it!");
                        } else {
                            log.warn("Cannot define the instrumented class '{}', it was loaded by parent loader", ic.getClassName());
                        }
                    }
                }else{
                    log.warn("Redefine by agent failed, redefine by class loader.");
                    redefineClasses.forEach((c, ic) -> {
                        redefineFailedClass(ic);
                    });
                }
            }
            redefineClasses.clear();
        }
    }

    private void onInstrumentFailed(Resource resource, byte[] rawBytes, AppInstrumentClass ic) {
        if(!testing) {
            throw new IllegalStateException("Cannot instrument class '" + ic.getClassName() + "', check the class loading!");
        }
        redefineFailedClass(ic);
        failedClasses.put(ic.getClassName(), ic);
    }

    private void redefineFailedClass(AppInstrumentClass ic) {
        if (null == redefineClassLoader) {
            redefineClassLoader = new RedefineClassLoader();
        }
        log.warn("Redefine failed class '{}' by class loader!", ic.getClassName());
        redefineClassLoader.defineClass(ic.getClassName(), ic.getClassData());
    }

    @Override
    public URL getResource(String name) {
        return parent.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return parent.getResources(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return parent.getResourceAsStream(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        log.trace("Loading class '{}'...", name);

        Class<?> c = findLoadedClass(name);

        if (null == c) {
            c = this.findClass(name);
        }

        if (null == c) {
            log.trace("Load class '{}' by parent loader", name);
            c = parent.loadClass(name);
        }

        if (resolve) {
            resolveClass(c);
        }

        return c;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        if(loadedNames.contains(name)) {
            return null;
        }else{
            loadedNames.add(name);
        }

        if(isParentLoaded(name)) {
            log.trace("Class '{}' already loaded by parent", name);
            return null;
        }

        return instrumentClass(name);
    }

    private Class<?> instrumentClass(String name) throws ClassNotFoundException {
        if(isIgnore(name)) {
            return null;
        }

        Resource resource = Resources.getResource("classpath:" + name.replace('.', '/') + ".class");
        if(null == resource || !resource.exists()) {
            return null;
        }

        if(instrumenting.contains(name)) {
            log.debug("Found cyclic instrumenting class '{}'", name);
            return instrumentClass(name, resource, false);
        }

        instrumenting.add(name);

        Class<?> c = instrumentClass(name, resource, true);

        instrumenting.remove(name);

        return c;
    }

    private Class<?> instrumentClass(String name, Resource resource, boolean depFirst) throws ClassNotFoundException {
        String url = resource.getURLString();
        if(loadedUrls.contains(url)) {
            return null;
        }

        try {
            byte[] rawBytes = IO.readByteArrayAndClose(resource.getInputStream());

            if(depFirst) {
                ClassDependency dep = dependencyResolver.resolveDependentClassNames(resource, rawBytes);

                if(null != dep.getSuperClassName() && !"java.lang.Object".equals(dep.getSuperClassName())) {
                    log.trace("Loading super class '{}' of '{}'", dep.getSuperClassName(), dep.getClassName());
                    instrumentClass(dep.getSuperClassName());
                }

                if(!dep.getDependentClassNames().isEmpty()) {

                    log.trace("Loading {} dependent classes of '{}'...",
                            dep.getDependentClassNames().size(),
                            dep.getClassName());

                    for(String depClassName : dep.getDependentClassNames()) {
                        log.trace("Loading dependent class '{}'", depClassName);
                        instrumentClass(depClassName);
                    }

                }

                if(loadedUrls.contains(url)){
                    return null;
                }
            }

            //try instrument the class.
            AppInstrumentClass ic = instrumentation.tryInstrument(this, resource, rawBytes, false);
            if(null == ic && null == name) {
                return null;
            }

            if(null == ic && isParentLoaded(name)) {
                log.trace("Class '{}' already loaded, do nothing", name);
                return null;
            }

            loadedUrls.add(url);

            byte[] bytes;
            if(null == ic) {
                bytes = rawBytes;
            }else{
                name  = ic.getClassName();
                bytes = ic.getClassData();
            }

            log.trace("Defining instrumented class '{}' use parent loader", name);
            Object[] args = new Object[]{name, bytes, 0, bytes.length};

            try {
                return (Class<?>) parentLoaderDefineClass.invoke(parent, args);
            }catch(InvocationTargetException e) {
                Throwable cause = e.getCause();

                if(cause instanceof ClassFormatError) {
                    throw new IllegalStateException("Instrument error of '" + name + "'", cause);
                }

                if(cause instanceof  LinkageError) {
                    if(null != ic && ic.shouldRedefine()) {
                        if(ic.supportsInstrumentMethodBodyOnly()) {
                            log.warn("Cannot define the instrumented class '{}', it was loaded by parent loader", name);
                            if(!ic.isInstrumentedMethodBodyOnly()) {
                                ic = instrumentation.tryInstrument(this, resource, rawBytes, true);
                            }
                            redefineClasses.put(parent.loadClass(name), ic);
                        }else{
                            log.warn("Instrument failed, class '{}'", name);
                            onInstrumentFailed(resource, rawBytes, ic);
                        }
                    }else{
                        if(null != ic && ic.isEnsure()) {
                            throw new IllegalStateException("Class '" + ic.getClassName() + "' already loaded by '" +
                                    parent.getClass().getName() + "', cannot instrument it!");
                        }else{
                            log.warn("Cannot define the instrumented class '{}', it was loaded by parent loader", name);
                        }
                    }
                    return null;
                }else{
                    throw new ClassNotFoundException(name, cause);
                }
            }catch(Exception e) {
                throw new ClassNotFoundException(name, e);
            }
        }catch(IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private boolean isParentLoaded(String className) {
        try {
            return null != parentFindLoadedClass.invoke(parent, className);
        } catch (Exception e) {
            throw Exceptions.uncheck(e);
        }
    }

    private static final Set<String>  SYSTEM_PACKAGES    = new HashSet<>();
    private static final Set<String>  FRAMEWORK_PACKAGES = new HashSet<>();
    private static final Set<String>  FRAMEWORK_CLASSES  = new HashSet<>();
    static {
        SYSTEM_PACKAGES.add("java");
        SYSTEM_PACKAGES.add("sun");
        SYSTEM_PACKAGES.add("org.junit.");

        FRAMEWORK_PACKAGES.add("leap.junit.");
        FRAMEWORK_PACKAGES.add("leap.lang.");
        FRAMEWORK_PACKAGES.add("leap.core.");
    }

    protected boolean isIgnore(String name) {
        for(String p : SYSTEM_PACKAGES) {
            if(name.startsWith(p)) {
                return true;
            }
        }

        for(String p : FRAMEWORK_PACKAGES) {
            if(name.startsWith(p)) {
                return true;
            }
        }

        if(FRAMEWORK_CLASSES.contains(name)) {
            return true;
        }

        if(isBeanClass(name)) {
            return false;
        }

        if(null != basePackage && name.startsWith(basePackage)) {
            return false;
        }

        return true;
    }

    private final class RedefineClassLoader extends ClassLoader {

        public void defineClass(String name, byte[] bytes) {
            defineClass(name, bytes, 0 ,bytes.length);
        }

        public Class<?> redefineClass(Class<?> c) {
            Class<?> redefined = redefineClass(c.getName());
            return null == redefined ? c : redefined;
        }

        private boolean isTestResource(Resource resource) {
            return resource.getURLString().indexOf("/test-classes/") > 0;
        }

        private Class<?> redefineClass(String name) {
            Class<?> c = findLoadedClass(name);
            if(null != c) {
                return null;
            }

            Resource resource = Resources.getResource("classpath:" + name.replace('.', '/') + ".class");
            if(null == resource || !resource.exists()) {
                return null;
            }

            if(!isTestResource(resource)) {
                return null;
            }

            try{
                byte[] bytes = IO.readByteArrayAndClose(resource.getInputStream());

                ClassDependency dep = dependencyResolver.resolveDependentClassNames(resource, bytes);

                boolean shouldRedefine = false;
                for(String depClassName : dep.getDependentClassNames()) {
                    if(failedClasses.containsKey(depClassName)) {
                        shouldRedefine = true;
                        break;
                    }
                }

                if(!shouldRedefine) {
                    return null;
                }

                log.warn("Redefining test class '{}'...",name);

                if(null != dep.getSuperClassName()) {
                    log.warn("Redefine super class '{}'", dep.getSuperClassName());
                    redefineClass(dep.getSuperClassName());
                }

                for(String innerClassName : dep.getInnerClassNames()) {
                    log.warn("Redefine inner class '{}'", innerClassName);
                    redefineClass(innerClassName);
                }

                //            String packageName = Classes.getPackageName(name);
                //            for(String depClassName : dep.getDependentClassNames()) {
                //                String depPackageName = Classes.getPackageName(depClassName);
                //                if(Strings.equals(packageName, depPackageName)) {
                //
                //                    if(null != redefineClass(depClassName)) {
                //
                //                    }
                //                }
                //            }

                return defineClass(name, bytes, 0, bytes.length);
            }catch(IOException e) {
                throw new IllegalStateException("Error redefine class '" + name + "'", e);
            }
        }

    }
}