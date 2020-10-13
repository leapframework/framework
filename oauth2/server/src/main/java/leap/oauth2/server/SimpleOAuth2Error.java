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
package leap.oauth2.server;

import leap.core.i18n.MessageKey;
import leap.core.i18n.MessageSource;
import leap.lang.Strings;

import java.util.Map;

public class SimpleOAuth2Error implements OAuth2Error {

    protected int status;
    protected String error;
    protected String errorCode;
    protected String referral;
    protected String errorDescription;
    protected MessageKey key;
    protected MessageSource messageSource;
    protected Map<String, Object> properties;

    public SimpleOAuth2Error(int status, String error, String errorDescription) {
        this(status,error,errorDescription,null);
    }

    public SimpleOAuth2Error(int status, String error, String errorDescription,MessageKey key) {
        this(status, error, null,errorDescription,key);
    }
    
    public SimpleOAuth2Error(int status, String error, String referral, String errorDescription,MessageKey key) {
        this(status, error, error,referral, errorDescription, key);
    }
    
    public SimpleOAuth2Error(int status, String error, String errorCode, String referral, String errorDescription, MessageKey key) {
        this.status = status;
        this.error = error;
        this.errorCode = errorCode;
        this.referral = referral;
        this.errorDescription = errorDescription;
        this.key = key;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    @Override
    public String getErrorDescription() {
        if(null == key || null == messageSource){
            return errorDescription;
        }
        String description = messageSource.tryGetMessage(key);
        return Strings.isEmpty(description)?errorDescription:description;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getReferral() {
        return referral;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }
    public void setStatus(int status) {
        this.status = status;
    }

	public MessageKey getKey() {
		return key;
	}

	public void setKey(MessageKey key) {
		this.key = key;
	}

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setReferral(String referral) {
        this.referral = referral;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
