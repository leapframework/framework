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

import leap.core.AppConfig;
import leap.lang.resource.Resource;

public interface AppInstrumentProcessor {

    default void init(AppConfig config) {

    }

    /**
     * Returns true if the processor changes the method body only.
     */
    default boolean isMethodBodyOnly() {
        return false;
    }

    /**
     * Returns true if the processor supports change the method body only.
     */
    default boolean supportsMethodBodyOnly() {
        return true;
    }

    /**
     * Returns true if the instrumented class should be redefined if define by class loader failed.
     */
    default boolean shouldRedefine() {
        return true;
    }

    /**
     * Instrument the class.
     *
     * @param context the {@link AppInstrumentContext}.
     * @param resource the resource of the instrument class.
     * @param bytes the byte codes of the instrument class.
     * @param methodBodyOnly if true, the processor must use a mechanism which changes the method body only.
     */
    void instrument(AppInstrumentContext context, Resource resource, byte[] bytes, boolean methodBodyOnly);

}