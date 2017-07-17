/*
 * Copyright 2014 the original author or authors.
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
package leap.core.validation;

public interface BeanValidator {

    /**
     * Validates the bean nested.
     *
     * @throws ValidationException if validate failed.
     */
    void validate(Object bean) throws ValidationException;

    /**
     * Validates the bean nested. Returns <code>false</code> if failed.
     */
	boolean validate(Object bean, Validation validation);

    /**
     * Validates the bean nested. Returns <code>false</code> if failed.
     */
	boolean validate(Object bean, Validation validation, int maxErrors);
	
}