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

import leap.web.config.WebConfigurator;

public interface CaptchaConfigurator {
	
	String CONFIG_PREFIX = WebConfigurator.CONFIG_PREFIX + "captcha.";
	
	String CONFIG_PROPERTY_DEFAULT_COOKIE_NAME   = CONFIG_PREFIX + "default-cookie-name";
	String CONFIG_PROPERTY_DEFAULT_TOKEN_EXPIRES = CONFIG_PREFIX + "default-token-expires";
	String CONFIG_PROPERTY_TOKEN_IGNROECASE      = CONFIG_PREFIX + "token-ignorecase";
	String CONFIG_PROPERTY_TOKEN_MIN_LENGTH      = CONFIG_PREFIX + "token-min-length";
	String CONFIG_PROPERTY_TOKEN_MAX_LENGTH      = CONFIG_PREFIX + "token-max-length";
	
	String DEFAULT_DEFAULT_COOKIE_NAME   = "captcha";
	int    DEFAULT_DEFAULT_TOKEN_EXPIRES = 60;//seconds
	int    DEFAULT_TOKEN_MIN_LENGTH      = 4;
	int    DEFAULT_TOKEN_MAX_LENGTH      = 6;
	
	CaptchaConfigurator setDefaultCookieName(String name);
	
	CaptchaConfigurator setDefaultTokenExpires(int expiresInSeconds);
	
	CaptchaConfigurator setTokenIgnoreCase(boolean ignorecase);
	
	CaptchaConfigurator setTokenMinLength(int len);
	
	CaptchaConfigurator setTokenMaxLength(int len);

}