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

import leap.lang.http.MimeTypes;
import leap.web.Request;
import leap.web.Response;
import leap.web.captcha.cage.Cage;
import leap.web.captcha.cage.GCage;

public class CageCaptchaHandler implements CaptchaHandler {
	
	protected static final String CONTENT_TYPE = MimeTypes.getMimeType("1.jpg");
	
	protected Cage cage = new GCage();

	public Cage getCage() {
		return cage;
	}

	public void setCage(Cage cage) {
		this.cage = cage;
	}

	@Override
	public String handleCaptchaRequest(CaptchaContext context, Request request, Response response) throws Throwable {
		response.setContentType(CONTENT_TYPE);
		
		//Generate token
		String token = generateToken(context, request);
		
		//Save token (must before wrting data to the output stream if using cookie store) 
		context.store().saveCaptchaToken(request, response, token);
		
		//Draw image
		cage.draw(token, response.getOutputStream());
		
		return token;
	}
	
	protected String generateToken(CaptchaContext context, Request request) {
		CaptchaGenerator generator = context.generator();
		if(null != generator) {
			return generator.next();
		}else{
			return cage.getTokenGenerator().next();
		}
	}
	
}