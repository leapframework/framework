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

import leap.web.Request;

/**
 * Created by KAEL on 2016/6/3.
 */
public interface UserAgentForwardedResolver {
    /**
     * test if this request is proxied by proxy server
     * @param request current request
     * @return
     */
    boolean isProxyRequest(Request request);

    /**
     * if this request is proxied by proxy server,resolver the user agent request location,otherwise it will throw an exception
     * @param request current request
     * @return
     */
    String resolveUserAgentForwarded(Request request);

    /**
     * if this request is proxied by proxy server,resolver the real ip of the user agent,otherwise it will throw an exception
     * @param request current request
     * @return
     */
    String resolveUserAgentRealIp(Request request);
    /**
     * if this request is proxied by proxy server,resolver the server name of the proxy server,otherwise it will throw an exception
     * @param request current request
     * @return
     */
    String resolveProxyServerName(Request request);
    /**
     * if this request is proxied by proxy server,resolver the protocol of this request,otherwise it will throw an exception
     * @param request current request
     * @return
     */
    String resolveProtocol(Request request);
}
