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

import leap.core.BeanFactory;
import leap.core.annotation.Configurable;
import leap.core.ioc.PostCreateBean;
import leap.lang.Assert;
import leap.web.config.WebConfigurator;

@Configurable(prefix=WebConfigurator.CONFIG_PREFIX + "captcha")
public class DefaultCaptchaConfig implements CaptchaConfig, CaptchaConfigurator, PostCreateBean {
	
	protected String  defaultCookieName   = DEFAULT_DEFAULT_COOKIE_NAME;
	protected int     defaultTokenExpires = DEFAULT_DEFAULT_TOKEN_EXPIRES;
	protected boolean tokenIgnoreCase     = true;
	protected int	  tokenMinLength      = DEFAULT_TOKEN_MIN_LENGTH;
	protected int     tokenMaxLength      = DEFAULT_TOKEN_MAX_LENGTH;
	
	@Configurable.Property
	public CaptchaConfigurator setDefaultCookieName(String name) {
		this.defaultCookieName = name;
		return this;
	}

	@Override
	public String getDefaultCookieName() {
		return defaultCookieName;
	}

	@Override
    public int getDefaultTokenExpires() {
	    return defaultTokenExpires;
    }

	@Override
    public CaptchaConfigurator setDefaultTokenExpires(int expiresInSeconds) {
		this.defaultTokenExpires = expiresInSeconds;
		return this;
	}

	public boolean isTokenIgnoreCase() {
		return tokenIgnoreCase;
	}

	public CaptchaConfigurator setTokenIgnoreCase(boolean tokenIgnoreCase) {
		this.tokenIgnoreCase = tokenIgnoreCase;
		return this;
	}

	public int getTokenMinLength() {
		return tokenMinLength;
	}

	public CaptchaConfigurator setTokenMinLength(int tokenMinLength) {
		this.tokenMinLength = tokenMinLength;
		return this;
	}

	public int getTokenMaxLength() {
		return tokenMaxLength;
	}

	public CaptchaConfigurator setTokenMaxLength(int tokenMaxLength) {
		this.tokenMaxLength = tokenMaxLength;
		return this;
	}

	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		Assert.isTrue(tokenMinLength > 0, "'token-min-length' must > 0");
		Assert.isTrue(tokenMaxLength >= tokenMinLength, "'token-max-length' must > 'token-min-length'");
    }
}