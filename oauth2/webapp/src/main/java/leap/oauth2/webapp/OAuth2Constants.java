/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package leap.oauth2.webapp;

public class OAuth2Constants {

	public static String BEARER = "Bearer";

    public static String JWT_TYPE = "jwt";

	/**
	 * The access token issued by the authorization server. This value is REQUIRED.
	 */
	public static String ACCESS_TOKEN = "access_token";

	protected OAuth2Constants() {
		
	}
}
