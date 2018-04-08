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
package leap.oauth2.server.code;

import java.util.LinkedHashMap;
import java.util.Map;

import leap.lang.expirable.TimeExpirableSeconds;


public class SimpleAuthzCode extends TimeExpirableSeconds implements AuthzCode {

    protected String code;
    protected String clientId;
    protected String userId;
    protected String sessionId;
    
    private Map<String, Object> extendedParameters= new LinkedHashMap<>();

    public SimpleAuthzCode() {
        super();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Map<String, Object> getExtendedParameters() {
		return extendedParameters;
	}

	public void setExtendedParameters(Map<String, Object> extendedParameters) {
		if(null == extendedParameters) {
			extendedParameters = new LinkedHashMap<>();
		}
		this.extendedParameters.putAll(extendedParameters);
		//this.extendedParameters = extendedParameters;
	}

	@Override
	public void addExtendedParameters(String key, Object value) {
		putExtendedParameter(key,value);
	}

	public void putExtendedParameter(String name, Object value) {
		if(null == extendedParameters) {
			extendedParameters = new LinkedHashMap<>();
		}
		extendedParameters.put(name, value);
	}

}