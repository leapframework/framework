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

package leap.lang.meta;

import leap.lang.accessor.AttributeGetter;
import leap.lang.accessor.AttributeSetter;

import java.util.HashMap;
import java.util.Map;

public interface MTypeContext extends AttributeGetter,AttributeSetter {

    MTypeContext DEFAULT = new MTypeContext() {
        public Object getAttribute(String name) {
            return null;
        }
        public void setAttribute(String name, Object value) {
        }
        public void onComplexTypeCreating(Class<?> type, String name) {
        }
        public void onComplexTypeCreated(Class<?> type, MComplexType ct) {
        }
        public String getCreatingComplexType(Class type) {
            return null;
        }
        public boolean isComplexTypeCreatingOrCreated(Class type) {
            return false;
        }
    };

    /**
     * Optional. Returns the root factory.
     */
    default MTypeFactory root() {
        return null;
    }

    /**
     * Required. Returns the {@link MTypeStrategy}.
     */
    default MTypeStrategy strategy() {
        return MTypeStrategy.DEFAULT;
    }

    /**
     * Notify a complex type is creating.
     */
    void onComplexTypeCreating(Class<?> type, String name);

    /**
     * Notify a complex type has been created.
     */
    void onComplexTypeCreated(Class<?> type, MComplexType ct);

    /**
     * Returns the name of complex type if creating.
     */
    String getCreatingComplexType(Class type);

    /**
     * Returns true if the type is creating or has been created.
     */
    boolean isComplexTypeCreatingOrCreated(Class type);
}