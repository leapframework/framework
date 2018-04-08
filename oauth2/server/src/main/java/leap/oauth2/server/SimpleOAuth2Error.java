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

public class SimpleOAuth2Error implements OAuth2Error {

    protected int status;
    protected String error;
    protected String errorDescription;
    private MessageKey key;

    public SimpleOAuth2Error() {
        super();
    }

    public SimpleOAuth2Error(int status, String error, String errorDescription) {
        this.status = status;
        this.error = error;
        this.errorDescription = errorDescription;
    }

    public SimpleOAuth2Error(int status, String error, String errorDescription,MessageKey key) {
        this.status = status;
        this.error = error;
        this.errorDescription = errorDescription;
        this.key=key;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    @Override
    public int getStatus() {
        return status;
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
}
