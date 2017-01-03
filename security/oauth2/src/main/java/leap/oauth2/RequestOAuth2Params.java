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

import java.util.Map;

import leap.lang.Strings;
import leap.web.Request;

public class RequestOAuth2Params implements OAuth2Params {
	
	protected final Request request;
	protected final String  grantType;
	
	public RequestOAuth2Params(Request request) {
		this.request   = request;
		this.grantType = null;
	}
	
	public RequestOAuth2Params(Request request, String grantType) {
		this.request   = request;
		this.grantType = grantType;
	}
	
	@Override
    public String getGrantType() {
		return grantType;
    }

	@Override
    public String getParameter(String name) {
	    return request.getParameter(name);
    }

	@Override
	public String getResourceServerId() {
		String rsId = request.getHeader(RS_ID);
		if(Strings.isEmpty(rsId)){
			rsId = getParameter(RS_ID);
		}
		return rsId;
	}
	
}