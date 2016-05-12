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

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultAppInstrumentContext implements AppInstrumentContext {

    private static final Log log = LogFactory.get(DefaultAppInstrumentContext.class);

    private final Map<String, AppInstrumentClass> instrumentedMap = new LinkedHashMap<>();

    @Override
    public Collection<AppInstrumentClass> getAllInstrumentedClasses() {
        return instrumentedMap.values();
    }

    @Override
    public AppInstrumentClass getInstrumentedClass(String className) {
        return instrumentedMap.get(className);
    }

    @Override
    public void addInstrumentedClass(Class<?> instrumentBy, String className, byte[] classData) {
        AppInstrumentClass ic = getInstrumentedClass(className);
        if(null == ic) {
            ic = new SimpleAppInstrumentClass(className, classData);
            ic.addInstrumentedBy(instrumentBy);
            instrumentedMap.put(className, ic);
            return;
        }

        ic.updateClassData(classData);
        ic.addInstrumentedBy(instrumentBy);
    }

    public void clear() {
        instrumentedMap.clear();
    }
}
