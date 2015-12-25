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
package leap.web.format;

import leap.core.AppException;

public class FormatNotAcceptableException extends AppException {

	private static final long serialVersionUID = -3981300203172288957L;

	public FormatNotAcceptableException() {
	}

	public FormatNotAcceptableException(String message) {
		super(message);
	}

	public FormatNotAcceptableException(Throwable cause) {
		super(cause);
	}

	public FormatNotAcceptableException(String message, Throwable cause) {
		super(message, cause);
	}

}
