/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.lang.http.client.apache;

import leap.lang.Arrays2;
import leap.lang.http.MimeType;
import leap.lang.http.MimeTypes;
import leap.lang.http.client.AbstractHttpResponse;
import leap.lang.http.client.HttpHeaders;
import leap.lang.http.client.HttpResponse;
import leap.lang.http.client.SimpleHttpHeaders;
import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;

public class ApacheHttpResponse extends AbstractHttpResponse implements HttpResponse {

    private final ApacheHttpRequest            request;
    private final org.apache.http.HttpResponse response;

    public ApacheHttpResponse(ApacheHttpClient client,
                              ApacheHttpRequest request,
                              org.apache.http.HttpResponse response,
                              boolean immediately) throws IOException{
        super(client);

        this.request  = request;
        this.response = response;

        if(immediately) {
            readHead();
            readBody();
        }
    }

    @Override
    public void forEachHeaders(BiConsumer<String, String> func) {
        Header[] headers = response.getAllHeaders();
        if(null != headers){
            for(Header header : headers) {
                func.accept(header.getName(), header.getValue());
            }
        }
    }

    @Override
    public String getHeader(String name) {
        Header[] headers = response.getHeaders(name);
        return Arrays2.isEmpty(headers) ? null : headers[0].getValue();
    }

    @Override
    public String[] getHeaderValues(String name) {
        Header[] headers = response.getHeaders(name);

        if(null == headers || headers.length == 0) {
            return Arrays2.EMPTY_STRING_ARRAY;
        }else{
            String[] values = new String[headers.length];
            for(int i=0;i<values.length;i++) {
                values[i] = headers[i].getValue();
            }
            return values;
        }
    }

    @Override
    public HttpHeaders getHeaders() {
        if(null == headers) {
            Header[] rawHeaders = response.getAllHeaders();
            headers = new SimpleHttpHeaders();
            if(null != headers){
                for(Header header : rawHeaders) {
                    headers.add(header.getName(), header.getValue());
                }
            }
        }
        return headers;
    }

    @Override
    public MimeType getContentType() {
        if(null == contentType){
            HttpEntity entity = response.getEntity();
            Header header = null == entity ? null : entity.getContentType();

            if(null != header){
                contentType = MimeTypes.parse(header.getValue());
            }
        }
        return contentType;
    }

    @Override
    protected InputStream getUnderlyingInputStream() throws IOException {
        HttpEntity entity = response.getEntity();
        return null == entity ? null : entity.getContent();
    }

    @Override
    protected void readHead() {
        status = response.getStatusLine().getStatusCode();
    }

    public long getContentLength(){
        HttpEntity entity = response.getEntity();
        return null == entity ? -1L : entity.getContentLength();
    }
}
