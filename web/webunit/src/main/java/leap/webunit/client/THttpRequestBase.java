/*
 * Copyright 2017 the original author or authors.
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
package leap.webunit.client;

import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.http.HTTP;
import leap.lang.http.QueryStringBuilder;
import leap.lang.net.Urls;

import java.net.URI;
import java.nio.charset.Charset;

abstract class THttpRequestBase<C extends THttpClient> implements THttpRequest {

    protected final C      client;
    protected final String uri;
    protected final QueryStringBuilder queryString = new QueryStringBuilder();

    protected HTTP.Method method;
    protected Charset     charset;
    protected byte[]      body;

    public THttpRequestBase(C client, String uri) {
        this.client = client;
        this.uri = uri;
        this.method = null;
        this.charset = client.getDefaultCharset();
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public THttpRequest setCharset(Charset charset) {
        Args.notNull(charset, "charset");
        this.charset = charset;
        return this;
    }

    @Override
    public HTTP.Method getMethod() {
        if (null == method) {
            return HTTP.Method.GET;
        }
        return method;
    }

    @Override
    public THttpRequest setMethod(HTTP.Method method) {
        Args.notNull(method, "method");
        this.method = method;
        return this;
    }

    @Override
    public THttpRequest addQueryParam(String name, String value) {
        Args.notEmpty(name, "name");
        queryString.add(name, value);
        return this;
    }

    @Override
    public THttpRequest setBody(byte[] content) {
        this.body = content;
        return this;
    }

    protected String buildRequestUrl() {
        String url = null;

        if (Strings.isEmpty(uri)) {
            url = client.getBaseUrl();
        } else if (uri.indexOf("://") > 0) {
            url = uri;
        } else if (Strings.startsWith(uri, "/")) {
            url = client.getBaseUrl() + uri;
        } else {
            url = client.getBaseUrl() + "/" + uri;
        }

        if (!queryString.isEmpty()) {
            url = Urls.appendQueryString(url, queryString.build());
        }

        URI uri = URI.create(url);
        String path = uri.getPath();
        if (!"".equals(path)) {
            for (String contextPath : client.getContextPaths()) {
                if (path.equals(contextPath)) {
                    url = uri.getScheme() + ":" + uri.getSchemeSpecificPart() + "/";
                    if (null != uri.getQuery()) {
                        url = url + "?" + uri.getRawQuery();
                    }
                    break;
                }
            }
        }

        return url;
    }
}
