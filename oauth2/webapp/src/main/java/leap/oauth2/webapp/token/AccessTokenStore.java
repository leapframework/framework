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
package leap.oauth2.webapp.token;

import leap.lang.Result;

/**
 * OAuth2.0 Access Token Store in Resource Server.
 */
public interface AccessTokenStore {

	/**
	 * Returns the result of loading {@link AccessTokenDetails}.
	 */
	Result<AccessTokenDetails> loadAccessTokenDetails(AccessToken token);

	/**
	 * Removes the {@link AccessToken} when access token expired.
	 */
	void removeAccessToken(AccessToken token);

}