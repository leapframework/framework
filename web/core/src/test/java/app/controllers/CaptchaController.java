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
package app.controllers;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.web.Request;
import leap.web.Response;
import leap.web.captcha.CaptchaContext;
import leap.web.captcha.CaptchaGenerator;
import leap.web.captcha.CaptchaManager;
import leap.web.captcha.CaptchaStore;
import leap.web.captcha.SimpleCaptchaContext;

public class CaptchaController {
	
	protected @Inject CaptchaManager captchaManager;

	public void index(Request request, Response response) throws Throwable {
		String token = request.getParameter("token");
		
		CaptchaStore     store     = captchaManager.getCaptchaStore();
		CaptchaGenerator generator = captchaManager.getCaptchaGenerator();
		
		if(!Strings.isEmpty(token)) {
			generator = () -> { return token; };
		}
		
		CaptchaContext context = new SimpleCaptchaContext(store, generator);
		
		captchaManager.handleCaptchaRequest(request, response, context);
	}
	
	public String verify(Request request, Response response) throws Throwable {
		String text = request.getParameter("text");
		
		if(captchaManager.verifyCaptchaToken(request, response, text)){
			return "ok";
		}else{
			return "failed";
		}
	}
}
