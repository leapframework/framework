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
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.web.captcha.cage.token.RandomTokenGenerator;


public class DefaultCaptchaGenerator implements CaptchaGenerator, PostCreateBean {
	
	protected @Inject CaptchaConfig config;

	protected RandomTokenGenerator cageTokenGenerator;

	@Override
    public String next() {
	    return cageTokenGenerator.next();
    }

	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		int tokenMinLength = config.getTokenMinLength();
		int tokenDelta     = config.getTokenMaxLength() - tokenMinLength;
		
		cageTokenGenerator = new RandomTokenGenerator(null, tokenMinLength, tokenDelta);
    }
}
