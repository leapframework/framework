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
package leap.web.debug;

import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.web.Request;

@Configurable(prefix="app")
public class DefaultDebugDetector implements DebugDetector {
	
	public static final String DEFAULT_DEBUG_PARAMETER        = "$debug";
	public static final String DEFAULT_DEBUG_SECRET_PARAMETER = "$debug_secret";
	
	protected boolean debugEnabled         = false;
	protected String  debugParameter       = DEFAULT_DEBUG_PARAMETER;
	protected String  debugSecret          = null;
	protected String  debugSecretParameter = DEFAULT_DEBUG_SECRET_PARAMETER;
	
	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	@ConfigProperty
	public void setDebugEnabled(boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
	}

	public String getDebugParameter() {
		return debugParameter;
	}

	@ConfigProperty
	public void setDebugParameter(String debugParameter) {
		this.debugParameter = debugParameter;
	}

	public String getDebugSecret() {
		return debugSecret;
	}

	@ConfigProperty
	public void setDebugSecret(String debugSecret) {
		this.debugSecret = debugSecret;
	}

	public String getDebugSecretParameter() {
		return debugSecretParameter;
	}

	@ConfigProperty
	public void setDebugSecretParameter(String debugSecretParameter) {
		this.debugSecretParameter = debugSecretParameter;
	}

	@Override
    public void detectDebugStatus(Request request) {
		if(request.isDebug()) {
			String p = request.getParameter(debugParameter);
			if(!Strings.isEmpty(p) && !Converts.toBoolean(p)) {
				request.setDebug(false);
			}
		}else{
			if(debugEnabled && checkDebugSecret(request)){
				String p = request.getParameter(debugParameter);
				if(!Strings.isEmpty(p)){
					request.setDebug(Converts.toBoolean(p));
				}
			}
		}
    }
	
	protected boolean checkDebugSecret(Request request) {
		if(null == debugSecret){
			return true;
		}
		
		String parameter = request.getParameter(debugSecretParameter);
		return Strings.equals(debugSecret, parameter);
	}

}
