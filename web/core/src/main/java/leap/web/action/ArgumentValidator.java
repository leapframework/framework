/*
 * Copyright 2016 the original author or authors.
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
package leap.web.action;

import leap.core.validation.Validation;
import leap.lang.Out;

public interface ArgumentValidator {

    /**
     * @deprecated  use {@link #validate(Validation, Argument, Object, Out)} instead.
     */
    @Deprecated
    default boolean validate(Validation validation, Argument arg, Object value) {
        return true;
    }

    /**
     * Returns <code>true</code> if validate success!
     */
    default boolean validate(Validation validation, Argument arg, Object value, Out<Object> out) {
        return validate(validation, arg, value);
    }

}