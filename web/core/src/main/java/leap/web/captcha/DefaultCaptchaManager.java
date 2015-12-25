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

import leap.core.annotation.Inject;
import leap.core.security.token.TokenVerifyException;
import leap.web.Request;
import leap.web.Response;

public class DefaultCaptchaManager implements CaptchaManager {
	
	protected @Inject 				  CaptchaStore     captchaStore;
	protected @Inject				  CaptchaGenerator captchaGenerator;
	protected @Inject(name="default") CaptchaHandler   defaultCaptchaHandler;
	protected @Inject				  CaptchaHandler[] captchaHandlers;
	
	@Override
    public CaptchaStore getCaptchaStore() {
	    return captchaStore;
    }

	@Override
    public CaptchaGenerator getCaptchaGenerator() {
	    return captchaGenerator;
    }

	@Override
	public String handleCaptchaRequest(Request request, Response response) throws Throwable {
		return handleCaptchaRequest(request, response, null);
	}
	
	@Override
    public String handleCaptchaRequest(Request request, Response response, CaptchaContext context) throws Throwable {
		if(null == context) {
			context = new SimpleCaptchaContext(captchaStore, captchaGenerator);	
		}
		
		String t = null;
		for(CaptchaHandler h : captchaHandlers) {
			if((t = h.handleCaptchaRequest(context, request, response)) != null){
				break;
			}
		}
		
		if(null == t) {
			t = defaultCaptchaHandler.handleCaptchaRequest(context, request, response);
		}

		return t;
    }

	@Override
    public boolean verifyCaptchaToken(Request request, Response response, String input) throws TokenVerifyException {
		return captchaStore.verifyCaptchaToken(request, response, input);
    }
}