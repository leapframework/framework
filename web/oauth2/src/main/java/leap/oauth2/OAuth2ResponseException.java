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
package leap.oauth2;


public class OAuth2ResponseException extends OAuth2Exception {

	private static final long serialVersionUID = -2496110058626352411L;
	
	protected final int    status;
	protected final String error;

	public OAuth2ResponseException(int status, String error, String message) {
		super(message);
		this.status = status;
		this.error  = error;
	}

	/**
	 * The http status.
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * The error code.
	 */
	public String getError() {
		return error;
	}

}