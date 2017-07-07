/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package leap.oauth2.webapp;

import leap.core.annotation.Inject;
import leap.web.App;
import leap.web.ServerInfo;
import leap.webunit.WebTestBase;
import org.junit.Test;

/**
 * @author kael.
 */
public class DefaultOAuth2ConfigTest extends WebTestBase {
    
    protected @Inject OAuth2Config oc;
    protected @Inject App app;
    
    @Test
    public void testOnServerInfoResolved(){
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setScheme("https");
        serverInfo.setHost("127.0.0.1");
        serverInfo.setPort(8080);
        serverInfo.setContextPath("/");
        DefaultOAuth2Config config = (DefaultOAuth2Config)oc;
        config.setServerUrl("/");
        config.setAuthorizeUrl("@{~/oauth2/authorize}");
        
        config.onServerInfoResolved(app,serverInfo);
        
        assertEquals("https://127.0.0.1:8080/oauth2/authorize",config.getAuthorizeUrl());
        assertEquals("https://127.0.0.1:8080/oauth2/token",config.getTokenUrl());
        assertEquals("https://127.0.0.1:8080/oauth2/tokeninfo",config.getTokenInfoUrl());
        assertEquals("https://127.0.0.1:8080/oauth2/publickey",config.getPublicKeyUrl());
        assertEquals("https://127.0.0.1:8080/oauth2/logout",config.getLogoutUrl());
    }
        
}
