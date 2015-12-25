/*
 * Copyright 2014 the original author or authors.
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
package leap.web.ajax;

import leap.lang.Strings;
import leap.lang.http.Headers;
import leap.web.Request;

public class DefaultAjaxDetector implements AjaxDetector {
	
	public static final String ANDROID_41_X_REQUESTED_WITH_HEADER = "com.android.browser";
	
	@Override
    public boolean detectAjaxRequest(Request request) {
		String header = request.getHeader(Headers.X_REQUESTED_WITH);
		
		if(Strings.isEmpty(header)){
			return false;
		}
		
		//The default browser in Android 4.1+ has added 
		//a new header to all requests "X-Requested-With: com.android.browser".
		if(header.equals(ANDROID_41_X_REQUESTED_WITH_HEADER)){
			return false;
		}
		
		return true;
	}
}