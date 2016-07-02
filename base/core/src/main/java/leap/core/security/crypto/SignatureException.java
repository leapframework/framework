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
package leap.core.security.crypto;

import leap.core.security.SecurityException;

public class SignatureException extends SecurityException {

	private static final long serialVersionUID = -7935570927087163687L;

	public SignatureException() {
	    super();
    }
	
	public SignatureException(String message) {
	    super(message);
    }

	public SignatureException(Throwable cause) {
	    super(cause);
    }

	public SignatureException(String message, Throwable cause) {
	    super(message, cause);
    }

	public SignatureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
	    super(message, cause, enableSuppression, writableStackTrace);
    }

}
