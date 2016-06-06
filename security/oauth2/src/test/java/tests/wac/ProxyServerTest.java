/*
 * Copyright 2016 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.wac;

import leap.lang.json.JSON;
import leap.oauth2.proxy.XForwardedResolver;
import org.junit.Test;
import tests.OAuth2TestBase;

import java.util.Map;

/**
 * Created by KAEL on 2016/6/3.
 */
public class ProxyServerTest extends OAuth2TestBase {
    @Test
    public void testProxyRequest(){
        if(!isLogin()){
            loginAuthzServer();
        }
        String json = client().request("/clientapp1/proxy_server")
                .addHeader(XForwardedResolver.X_FORWARDED_FOR,"127.0.0.1")
                .addHeader(XForwardedResolver.X_FORWARDED_SERVER,"www.leap.com")
                .addHeader(XForwardedResolver.X_FORWARDED_HOST,"www.leap.org")
                .addHeader(XForwardedResolver.X_FORWARDED_PROTOCOL,"https")
                .get().getContent();
        Map<String, Object> map = JSON.decodeToMap(json);

        assertEquals("true",map.get("isProxy"));
        assertEquals("https://www.leap.org/clientapp1",map.get("host"));
        assertEquals("www.leap.com",map.get("serverName"));
        assertEquals("https",map.get("protocol"));
        assertEquals("127.0.0.1",map.get("ip"));
    }
}
