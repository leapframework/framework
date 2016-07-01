/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.core.instrument;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultAppInstrumentContext implements AppInstrumentContext {

    private final ClassLoader                     classLoader;
    private final Map<String, AppInstrumentClass> instrumentedMap = new LinkedHashMap<>();

    public DefaultAppInstrumentContext(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Collection<AppInstrumentClass> getAllInstrumentedClasses() {
        return instrumentedMap.values();
    }

    @Override
    public AppInstrumentClass getInstrumentedClass(String className) {
        return instrumentedMap.get(className);
    }

    @Override
    public AppInstrumentClass newInstrumentedClass(String internalClassName) {
        return new SimpleAppInstrumentClass(internalClassName);
    }

    @Override
    public void updateInstrumented(AppInstrumentClass ic, AppInstrumentProcessor instrumentedBy, byte[] classData, boolean ensure) {
        SimpleAppInstrumentClass sic = (SimpleAppInstrumentClass)ic;

        if(!instrumentedMap.containsValue(ic)) {
            instrumentedMap.put(ic.getInternalClassName(), ic);
        }

        sic.updateClassData(classData);
        sic.addInstrumentedBy(instrumentedBy);

        if(ensure) {
            ic.makeEnsure();
        }
    }

    public void clear() {
        instrumentedMap.clear();
    }
}
