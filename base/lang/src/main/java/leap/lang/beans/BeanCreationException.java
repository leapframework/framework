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
package leap.lang.beans;



public class BeanCreationException extends BeanException {

	private static final long serialVersionUID = -7111373109559488768L;

	public BeanCreationException() {
		
	}

	public BeanCreationException(String message) {
		super(message);
	}

	public BeanCreationException(Throwable cause) {
		super(cause);
	}

	public BeanCreationException(String message, Throwable cause) {
		super(message, cause);
	}

}
