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

public interface ClassDependency {

    /**
     * Returns the class name.
     */
    String getClassName();

    /**
     * Returns the super class name or returns null if the super class is {@link Object}.
     */
    String getSuperClassName();

    /**
     * Returns the inner class names.
     */
    Set<String> getInnerClassNames();

    /**
     * Returns all the dependent class names (includes the inner class names).
     */
    Set<String> getDependentClassNames();
}
