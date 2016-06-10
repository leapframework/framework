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

import java.util.Set;

public interface AppInstrumentClass {

    /**
     * Returns the class name.
     */
    String getClassName();

    /**
     * Returns the instrumented class name as internal class format.
     */
    String getInternalClassName();

    /**
     * Returns the bytes of class.
     */
    byte[] getClassData();

    /**
     * Returns true if the class must be instrumented.
     */
    boolean isEnsure();

    /**
     * Make the class ensure be instrumented.
     */
    void makeEnsure();

    /**
     * Updates the class data to the newly.
     */
    void updateClassData(byte[] data);

    /**
     * Returns a set contains all the classes which the class instrumented by..
     */
    Set<Class<?>> getAllInstrumentedBy();

    /**
     * Adds an instrumented by.
     */
    void addInstrumentedBy(Class<?> cls);
}
