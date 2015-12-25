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
package leap.web.captcha;

import leap.core.security.token.TokenExpiredException;
import leap.core.security.token.TokenVerifyException;
import leap.web.Request;
import leap.web.Response;

public interface CaptchaStore {
	
	/**
	 * Saves the captcha token so it can be resolved later from request.
	 */
	void saveCaptchaToken(Request request, Response response, String token);
	
	/**
	 * Verify the user's input token is valid.
	 * 
	 * <p>
	 * Returns <code>true</code> if the user's input token is valid.
	 * 
	 * <p>
	 * Returns <code>false</code> fi the user's input token is invalid.
	 * 
	 * @throw {@link TokenVerifyException} if the generated token aleady expired.
	 */
	boolean verifyCaptchaToken(Request request, Response response, String input) throws TokenExpiredException;

}