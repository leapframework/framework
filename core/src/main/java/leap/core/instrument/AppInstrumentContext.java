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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public interface AppInstrumentContext {

    /**
     * Returns the input stream of instrumented class or just returns the given if the class was not instrumented.
     */
    default InputStream getInstrcumentedOrTheGiven(String className, InputStream is) {
        AppInstrumentClass ic = getInstrumentedClass(className);
        if(null == ic) {
            return is;
        }else{
            return new ByteArrayInputStream(ic.getClassData());
        }
    }

    /**
     * Returns true if the given class name was instrumented by the class.
     */
    default boolean isInstrumentedBy(String className, Class<?> instrumentedBy) {
        AppInstrumentClass ic = getInstrumentedClass(className);
        return null == ic ? false : ic.getAllInstrumentedBy().contains(instrumentedBy);
    }

    /**
     * Returns the instrumented class or null.
     */
    AppInstrumentClass getInstrumentedClass(String className);

    /**
     * Puts the instrumented class to context.
     */
    void addInstrumentedClass(Class<?> instrumentBy, String className, byte[] classData);

}