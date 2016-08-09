/*
 * Copyright 2013 the original author or authors.
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
package leap.orm.validation;

import leap.core.validation.Validation;
import leap.orm.value.EntityWrapper;

public interface EntityValidator {

	boolean validate(EntityWrapper entity,Validation validation,int maxErrors);

    boolean validate(EntityWrapper entity,Validation validation,int maxErrors, Iterable<String> fields);
	
}