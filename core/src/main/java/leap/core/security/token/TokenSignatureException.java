/*
 * Copyright 2015 the original author or authors.
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
package leap.core.security.token;

import leap.core.security.crypto.SignatureException;

public class TokenSignatureException extends SignatureException {

	private static final long serialVersionUID = -6160673782572414863L;

	public TokenSignatureException() {
		super();
	}

	public TokenSignatureException(String message) {
		super(message);
	}

	public TokenSignatureException(Throwable cause) {
		super(cause);
	}

	public TokenSignatureException(String message, Throwable cause) {
		super(message, cause);
	}

	public TokenSignatureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
