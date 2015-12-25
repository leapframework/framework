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
package leap.htpl.exception;

import leap.htpl.HtplException;

public class HtplDefinitionException extends HtplException {

	private static final long serialVersionUID = -8457171059000352716L;

	public HtplDefinitionException() {
	}

	public HtplDefinitionException(String message) {
		super(message);
	}

	public HtplDefinitionException(Throwable cause) {
		super(cause);
	}

	public HtplDefinitionException(String message, Throwable cause) {
		super(message, cause);
	}

}