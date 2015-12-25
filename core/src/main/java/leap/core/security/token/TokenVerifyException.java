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

import leap.core.security.SecurityException;

public class TokenVerifyException extends SecurityException {
	
	public static enum ErrorCode {
		INVALID_TOKEN,
		INVALID_SIGNATURE,
		INVALID_PAYLOAD,
		TOKEN_EXPIRED,
		VERIFY_FAILED;
	}

	private static final long serialVersionUID = 5944490378073866038L;
	
	private final ErrorCode errorCode;

	public TokenVerifyException(ErrorCode type) {
		this.errorCode = type;
	}

	public TokenVerifyException(ErrorCode type, String message) {
		super(message);
		this.errorCode = type;
	}

	public TokenVerifyException(ErrorCode type, Throwable cause) {
		super(cause);
		this.errorCode = type;
	}

	public TokenVerifyException(ErrorCode type, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = type;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public boolean isExpired() {
		return errorCode == ErrorCode.TOKEN_EXPIRED;
	}
}
