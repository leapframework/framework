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

package leap.oauth2.proxy;

import leap.lang.Strings;
import leap.web.Request;

/**
 * Created by KAEL on 2016/6/3.
 */
public class XForwardedResolver implements UserAgentForwardedResolver {
    public static final String X_FORWARDED_HOST = "x-forwarded-host";
    public static final String X_FORWARDED_FOR = "x-forwarded-for";
    public static final String X_FORWARDED_SERVER = "x-forwarded-server";
    public static final String X_FORWARDED_PROTOCOL = "x-forwarded-protocol";
    @Override
    public boolean isProxyRequest(Request request) {
        return request.getHeader(X_FORWARDED_HOST)!=null||
                request.getHeader(X_FORWARDED_FOR)!=null||
                request.getHeader(X_FORWARDED_SERVER)!=null||
                request.getHeader(X_FORWARDED_PROTOCOL)!=null;
    }

    @Override
    public String resolveUserAgentForwarded(Request request) {
        if(isProxyRequest(request)){
            String host = request.getHeader(X_FORWARDED_HOST);
            if(Strings.isNotEmpty(host)){
                String protocol = resolveProtocol(request);
                if(Strings.isEmpty(protocol)){
                    protocol = "http";
                }
                protocol += "://";
                return protocol + host + request.getContextPath();
            }else{
                return host + request.getContextPath();
            }
        }else{
            throw new IllegalStateException("this request is not a proxied request");
        }
    }

    @Override
    public String resolveUserAgentRealIp(Request request) {
        if(isProxyRequest(request)){
            return request.getHeader(X_FORWARDED_FOR);
        }else{
            throw new IllegalStateException("this request is not a proxied request");
        }
    }

    @Override
    public String resolveProxyServerName(Request request) {
        if(isProxyRequest(request)){
            return request.getHeader(X_FORWARDED_SERVER);
        }else{
            throw new IllegalStateException("this request is not a proxied request");
        }
    }

    @Override
    public String resolveProtocol(Request request) {
        if(isProxyRequest(request)){
            return request.getHeader(X_FORWARDED_PROTOCOL);
        }else{
            throw new IllegalStateException("this request is not a proxied request");
        }
    }
}
