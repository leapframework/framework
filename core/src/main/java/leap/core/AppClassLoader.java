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

import leap.core.instrument.AppInstrumentClass;
import leap.core.instrument.AppInstrumentation;
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
import java.util.HashSet;
import java.util.Set;

@Internal
public class AppClassLoader extends ClassLoader {

    private static final Log log = LogFactory.get(AppClassLoader.class);

    private static final ThreadLocal<Set<String>> beanClassNamesLocal = new ThreadLocal<>();

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

    private final ClassLoader        parent;
    private final AppConfig          config;
    private final AppInstrumentation instrumentation = Factory.newInstance(AppInstrumentation.class);
    private final Set<String>        loaded          = new HashSet<>();
    private final String             basePackage;

    private boolean parentLoaderPriority  = true;
    private boolean alwaysUseParentLoader = true;
    private Method  parentLoaderDefineClass;

    public AppClassLoader(ClassLoader parent, AppConfig config) {
        this.parent      = parent;
        this.config      = config;
        this.basePackage = config.getBasePackage() + ".";

        try {
            parentLoaderDefineClass =
                    ClassLoader.class.getDeclaredMethod("defineClass",
                        new Class[] {String.class, byte[].class, int.class,int.class});

            parentLoaderDefineClass.setAccessible(true);
        } catch (Exception e) {
            throw Exceptions.uncheck(e);
        }

        instrumentation.init(config);

        loadAllClasses();
    }

    void done() {
        loaded.clear();
        beanClassNamesLocal.remove();
    }

    private void loadAllClasses() {
        config.getResources().forEach(resource -> {

            if(resource.exists()) {
                String filename = resource.getFilename();

                if(null != filename &&
                        filename.endsWith(Classes.CLASS_FILE_SUFFIX)) {

                    try {
                        findClass("", resource);
                    } catch (ClassNotFoundException e) {
                        throw new NestedClassNotFoundException(e);
                    }
                }
            }
        });
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);

        if (null == c) {
            c = this.findClass(name);
        }

        if (null == c) {
            c = parent.loadClass(name);
        }

        if (resolve) {
            resolveClass(c);
        }

        return c;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        if(isIgnore(name)) {
            return null;
        }

        Resource resource = Resources.getResource("classpath:" + name.replace('.', '/') + ".class");
        if(null == resource || !resource.exists()) {
            return null;
        }

        return findClass(name, resource);
    }

    private Class<?> findClass(String name, Resource resource) throws ClassNotFoundException {
        String url = resource.getURLString();
        if(loaded.contains(url)) {
            return null;
        }else{
            loaded.add(url);
        }

        InputStream is = null;
        try {
            is = resource.getInputStream();
            byte[] bytes = IO.readByteArray(is);

            //try instrument the class.
            AppInstrumentClass ic = instrumentation.tryInstrument(resource, bytes);

            if(null != ic) {
                name  = ic.getClassName();
                bytes = ic.getClassData();

                if(!alwaysUseParentLoader && (ic.isBeanDeclared() || isBeanClass(name))) {
                    log.trace("Defining instrumented bean class '{}' use app loader", name);
                    return defineClass(name, bytes, 0, bytes.length);
                }
            }

            if(!parentLoaderPriority) {
                log.trace("Defining instrumented class '{}' use app loader", name);
                return defineClass(name, bytes, 0, bytes.length);
            }else{
                if(null == ic) {
                    return null;
                }

                log.trace("Defining instrumented class '{}' use parent loader", name);
                Object[] args = new Object[]{name, bytes, 0, bytes.length};

                try {
                    return (Class<?>) parentLoaderDefineClass.invoke(parent, args);
                }catch(InvocationTargetException e) {
                    Throwable cause = e.getCause();

                    if(cause instanceof  LinkageError) {
                        if (ic.isEnsure()) {
                            throw new IllegalStateException("Class '" + name + "' already loaded by '" +
                                    parent.getClass().getName() + "', cannot instrument it!");
                        } else {
                            log.warn("Cannot define the instrumented class '{}', it was loaded by parent loader", name);
                            return null;
                        }
                    }else{
                        throw new ClassNotFoundException(name, cause);
                    }
                }catch(Exception e) {
                    throw new ClassNotFoundException(name, e);
                }
            }
        }catch(IOException e) {
            throw new ClassNotFoundException(name, e);
        }finally{
            IO.close(is);
        }
    }

    protected boolean isIgnore(String name) {
        if(name.startsWith("java")) {
            return true;
        }

        if(isBeanClass(name)) {
            return false;
        }

        if(name.startsWith(basePackage)) {
            return false;
        }

        return true;
    }
}