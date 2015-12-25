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
package leap.web.cookie;

import leap.lang.Args;
import leap.web.config.WebConfig;

public class CookieBean extends AbstractCookieBean {

    protected String  cookieName;
    protected boolean cookieHttpOnly;
    protected boolean cookieCrossContext;
    protected String  cookieExpiresParameter;
    protected int     cookieExpires = -1;
    
    public CookieBean(WebConfig webConfig, String cookieName) {
        Args.notNull(webConfig, "web config");
        Args.notEmpty(cookieName, "cookie name");
        this.webConfig  = webConfig;
        this.cookieName = cookieName;
    }
    
    @Override
    public String getCookieName() {
        return cookieName;
    }
    
    public boolean isCookieHttpOnly() {
        return cookieHttpOnly;
    }
    
    public void setCookieHttpOnly(boolean httpOnly) {
        this.cookieHttpOnly = httpOnly;
    }

    public boolean isCookieCrossContext() {
        return cookieCrossContext;
    }
    
    public void setCookieCrossContext(boolean cookieCrossContext) {
        this.cookieCrossContext = cookieCrossContext;
    }

    public String getCookieExpiresParameter() {
        return cookieExpiresParameter;
    }
    
    public void setCookieExpiresParameter(String cookieExpiresParameter) {
        this.cookieExpiresParameter = cookieExpiresParameter;
    }

    public int getCookieExpires() {
        return cookieExpires;
    }

    public void setCookieExpires(int cookieExpires) {
        this.cookieExpires = cookieExpires;
    }
}
