/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app1.controllers;


import leap.core.annotation.Inject;
import leap.core.security.annotation.AllowAnonymous;
import leap.lang.New;
import leap.oauth2.proxy.UserAgentForwardedResolver;
import leap.web.Request;

import java.util.Map;

public class HomeController {

    private @Inject UserAgentForwardedResolver userAgentForwardedResolver;

    public String index() {
        return "It works!";
    }

    @AllowAnonymous
    public Map<String, String> proxyServer(Request request){
        Map<String, String> map = New.hashMap();
        boolean isProxy = userAgentForwardedResolver.isProxyRequest(request);
        String host = userAgentForwardedResolver.resolveUserAgentForwarded(request);
        String ip   = userAgentForwardedResolver.resolveUserAgentRealIp(request);
        String serverName = userAgentForwardedResolver.resolveProxyServerName(request);
        String protocol = userAgentForwardedResolver.resolveProtocol(request);
        map.put("isProxy",String.valueOf(isProxy));
        map.put("host",host);
        map.put("ip",ip);
        map.put("serverName",serverName);
        map.put("protocol",protocol);
        return map;
    }
    
}