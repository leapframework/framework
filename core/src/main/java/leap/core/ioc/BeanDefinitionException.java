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
package leap.core.ioc;

import leap.lang.beans.BeanException;


public class BeanDefinitionException extends BeanException {

	private static final long serialVersionUID = -1296169202632712225L;

	public BeanDefinitionException() {
	
	}

	public BeanDefinitionException(String message) {
		super(message);
	}

	public BeanDefinitionException(Throwable cause) {
		super(cause);
	}

	public BeanDefinitionException(String message, Throwable cause) {
		super(message, cause);
	}

}