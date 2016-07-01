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

import leap.agent.InstrumentClass;

import java.util.Set;

public interface AppInstrumentClass extends InstrumentClass {

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
     * Returns true if the class is declared as a managed bean.
     */
    boolean isBeanDeclared();

    /**
     * Sets the class is delcared as a managed bean.
     */
    void setBeanDeclared(boolean b);

    /**
     * Returns a set contains all the classes which the class instrumented by..
     */
    Set<AppInstrumentProcessor> getAllInstrumentedBy();

    /**
     * Returns true if the class has been instrumented in method body only.
     */
    default boolean isInstrumentedMethodBodyOnly() {
        for(AppInstrumentProcessor p : getAllInstrumentedBy()) {
            if(!p.isMethodBodyOnly()) {
                return false;
            }
        }
        return true;
    }
}
