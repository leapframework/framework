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

import java.util.Locale;

import leap.lang.NamedError;

public class NestedSimpleErrors extends SimpleErrors {
	
	protected final String parentObjectName;

	public NestedSimpleErrors(String parentObjectName) {
		this.parentObjectName = parentObjectName;
	}

	public NestedSimpleErrors(String parentObjectName, Locale locale) {
		super(locale);
		this.parentObjectName = parentObjectName;
	}

	@Override
    protected NamedError createError(String name, String code, String message) {
	    return super.createError(parentObjectName + "." + name, code, message);
    }

	@Override
    protected NamedError createError(String name, String message) {
	    return super.createError(parentObjectName + "." +name, message);
    }

}