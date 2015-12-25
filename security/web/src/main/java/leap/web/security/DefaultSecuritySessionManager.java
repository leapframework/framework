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
package leap.web.security;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import leap.core.Session;
import leap.core.web.RequestBase;
import leap.web.security.authc.Authentication;

public class DefaultSecuritySessionManager implements SecuritySessionManager {
	
	private static final String SESSION_KEY = DefaultSecuritySessionManager.class.getName() + "$User";
	
	@Override
    public Authentication getAuthentication(RequestBase request) {
        Session sc = request.getSession(false);
        
        if(null == sc){
            return null;
        }
        
        return (Authentication)sc.getAttribute(SESSION_KEY);    
	}

    @Override
    public void saveAuthentication(RequestBase request, Authentication authentication) {
        //https://www.owasp.org/index.php/Talk:Session_Fixation
        
        //invalidate current session, .
        Map<String, Object> attrs = null;
        Session sc = request.getSession(false);
        if(null != sc){
            HttpSession hs = sc.getServletSession();
            
            Enumeration<String> names = hs.getAttributeNames();
            if(names.hasMoreElements()) {
                attrs = new HashMap<String, Object>();
                while(names.hasMoreElements()) {
                    String name = names.nextElement();
                    attrs.put(name, hs.getAttribute(name));
                }
            }
            
            sc.invalidate();
        }
        
        //force to create new session
        sc = request.getSession(true);

        //copy all the attributes to new session.
        if(null != attrs){
            for(Entry<String, Object> entry : attrs.entrySet()) {
                sc.setAttribute(entry.getKey(), entry.getValue());
            }
        }
        
        sc.setAttribute(SESSION_KEY, authentication);
    }

    @Override
    public Authentication removeAuthentication(RequestBase request) {
        Session sc = request.getSession(false);
        
        if(null == sc){
            return null;
        }
        
        Authentication us = (Authentication)sc.getAttribute(SESSION_KEY);

        //TODO : config , invalidate session after logout
        if(null != us){
            sc.removeAttribute(SESSION_KEY);
            sc.invalidate();
        }

        return us;
    }
}