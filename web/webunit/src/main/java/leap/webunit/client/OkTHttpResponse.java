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

import leap.lang.Exceptions;
import leap.lang.http.Header;
import leap.lang.http.MimeType;
import leap.lang.http.MimeTypes;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class OkTHttpResponse extends THttpResponseBase<OkTHttpClient> {

    private final Response     response;
    private final ResponseBody body;

    public OkTHttpResponse(OkTHttpRequest request, Response response) {
        super(request);
        this.response = response;
        this.body     = response.body();
    }

    @Override
    public Integer getStatus() {
        return response.code();
    }

    @Override
    public long getContentLength() {
        return body.contentLength();
    }

    @Override
    public MimeType getContentType() {
        return null == body.contentType() ? null : MimeTypes.parse(body.contentType().toString());
    }

    @Override
    public String getHeader(String name) {
        return response.header(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        List<String> values = response.headers(name);
        List<Header> headers = new ArrayList<>();
        values.forEach(v -> headers.add(new Header(name, v)));
        return headers.toArray(new Header[0]);
    }

    @Override
    public InputStream getInputStream() {
        return body.byteStream();
    }
}
