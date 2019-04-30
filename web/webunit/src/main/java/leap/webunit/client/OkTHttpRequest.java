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
 *
 */
package leap.webunit.client;

import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.HTTP;
import leap.lang.http.MimeType;
import leap.lang.http.exception.HttpException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import okhttp3.*;
import okhttp3.internal.http.HttpMethod;

class OkTHttpRequest extends THttpRequestBase<OkTHttpClient> {

    private static final Log log = LogFactory.get(OkTHttpRequest.class);

    private static final RequestBody EMPTY_REQUEST_BODY = RequestBody.create(MediaType.parse("text/plain"), "");

    private final OkHttpClient     httpClient;
    private final Headers.Builder  headers  = new Headers.Builder();

    private FormBody.Builder formBody;
    private RequestBody      requestBody;
    private Request.Builder  request;
    private OkTHttpMultipart multipart;

    public OkTHttpRequest(OkTHttpClient client, String uri) {
        super(client, uri);
        this.httpClient = client.getHttpClient();
    }

    @Override
    public THttpMultipart multipart() {
        if(null == multipart) {
            multipart = new OkTHttpMultipart(this);
        }
        return multipart;
    }

    @Override
    public THttpRequest setHeader(String name, String value) {
        headers.set(name, value);
        return this;
    }

    @Override
    public THttpRequest addHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

    @Override
    public THttpRequest addFormParam(String name, String value) {
        if(null == formBody) {
            formBody = new FormBody.Builder();
        }
        formBody.add(name, value);
        return this;
    }

    @Override
    public THttpResponse send() {
        String url = buildRequestUrl();
        try {
            initRequest(url);

            log.debug("Sending '{}' request to '{}'...", method, url);

            OkTHttpResponse response = new OkTHttpResponse(this, httpClient.newCall(request.build()).execute());

            if(log.isDebugEnabled()) {
                log.debug("Response result : [status={}, content-type='{}', content-length={}]",
                        response.getStatus(),
                        response.getContentType(),
                        response.getContentLength());

                MimeType contentType = response.getContentType();
                if(null != contentType && ContentTypes.isText(contentType.getMediaType())) {
                    log.debug("Content -> \n{}", Strings.abbreviate(response.getContent(), 200));
                }
            }
            return response;
        } catch (Exception e) {
            throw new HttpException("Error send http request : " + e.getMessage(),e);
        }
    }

    private void initRequest(String url) {
        if(null != formBody) {
            requestBody = formBody.build();
        }else if(null != body && body.length > 0) {
            requestBody = RequestBody.create(null, body);
        }else if(multipart != null && !multipart.isEmpty()) {
            requestBody = multipart.buildRequestBody();
        }

        if(null != requestBody && method == null) {
            method = HTTP.Method.POST;
        }

        if(null == requestBody && HttpMethod.requiresRequestBody(method.name())) {
            requestBody = EMPTY_REQUEST_BODY;
        }

        request = new Request.Builder();
        request.url(url);
        request.headers(headers.build());
        request.method(method.name(), requestBody);
    }
}
