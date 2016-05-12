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

package leap.core.instrument;

import leap.core.AppConfig;
import leap.core.AppInitException;
import leap.lang.Classes;
import leap.lang.Factory;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import leap.lang.resource.SimpleResourceSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultAppInstrumentation implements AppInstrumentation {

    private static final Log log = LogFactory.get(DefaultAppInstrumentation.class);

    private final List<AppInstrumentProcessor> processors = Factory.newInstances(AppInstrumentProcessor.class);

    private Set<String> instrumented = new HashSet<>();

    @Override
    public void init(AppConfig config) {
        for(AppInstrumentProcessor p : processors) {
            p.init(config);
        }
    }

    @Override
    public void complete() {
        instrumented.clear();
    }

    @Override
    public void instrument(ResourceSet rs) {
        DefaultAppInstrumentContext context = new DefaultAppInstrumentContext();

        for(AppInstrumentProcessor p : processors){
            try {
                p.instrument(context, rs);
            } catch (Throwable e) {
                throw new AppInitException("Error calling instrument processor '" + p + "', " + e.getMessage(), e);
            }
        }

        postInstrumented(context.getAllInstrumentedClasses());
    }

    @Override
    public void instrument(String className) {
        if(!tryInstrument(className)) {
            throw new ObjectNotFoundException("The resource of class name '" + className + "' not found");
        }
    }

    @Override
    public boolean tryInstrument(String className) {
        Resource resource = Resources.getResource("classpath:" + className.replace('.', '/') + ".class");
        if(null == resource || !resource.exists()) {
            return false;
        }

        instrument(new SimpleResourceSet(new Resource[]{resource}));
        return true;
    }

    public void postInstrumented(Collection<AppInstrumentClass> instrumentClasses) {
        AppInstrumentClassLoader classLoader = new AppInstrumentClassLoader(Classes.getClassLoader());

        for(AppInstrumentClass ic : instrumentClasses) {
            log.trace("Define the instrumented class '{}'", ic.getClassName());

            String className = ic.getClassName().replace('/','.');

            if(instrumented.contains(className)) {
                log.info("Class '{}' already instrumented", className);
                continue;
            }

            instrumented.add(className);
            try {
                classLoader.defineClass(className, ic.getClassData());
            } catch (RuntimeException e) {
                Throwable cause = e.getCause();

                if(cause instanceof ClassFormatError) {
                    throw e;
                }

                if(cause instanceof LinkageError) {
                    log.warn("Class '{}' already loaded or instrumented by another class loader", ic.getClassName());
                    continue;
                }

                throw e;
            }
        }
    }
}
