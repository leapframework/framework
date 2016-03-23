/*
 * Copyright 2015 the original author or authors.
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
package leap.lang.http.client;

import leap.lang.Strings;
import leap.lang.http.Headers;
import leap.lang.http.MimeType;
import leap.lang.http.MimeTypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class JdkHttpResponse implements HttpResponse {

    protected final JdkHttpClient  client;
    protected final JdkHttpRequest request;

    protected int         status;
    protected String      reason;
    protected HttpHeaders headers;
    protected byte[]      bytes;
    protected MimeType    contentType;
    
    protected JdkHttpResponse(JdkHttpClient client, JdkHttpRequest request, HttpURLConnection conn) throws IOException {
        this.client  = client;
        this.request = request;
        
        init(conn);
    }
    
    @Override
    public int getStatus() {
        return status;
    }
    
    @Override
    public String getReason() {
        return reason;
    }
    
    public String getHeader(String name) {
        List<String> values = headers.get(name);
        if(null == values || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }
    
    @Override
    public String[] getHeaderValues(String name) {
        List<String> values = headers.get(name);
        if(null == values) {
            return null;
        }else{
            return values.toArray(new String[values.size()]); 
        }
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    public MimeType getContentType() {
        if(null == contentType) {
            String header = getHeader(Headers.CONTENT_TYPE);
            if(!Strings.isEmpty(header)) {
                contentType = MimeTypes.parse(header);
            }
        }
        return contentType;
    }
    
    @Override
    public byte[] getBytes() {
        return bytes;
    }
    
    public String getString() {
        return Strings.newString(getBytes(), charset());
    }

    @Override
    public void forEachHeaders(BiConsumer<String, String> func) {
        headers.forEach(func);
    }

    protected String charset() {
        MimeType mt = getContentType();
        if(null != mt && !Strings.isEmpty(mt.getCharset())) {
            return mt.getCharset();
        }else{
            return client.getDefaultCharset().name();
        }
    }
    
    protected void init(HttpURLConnection conn) throws IOException {
        status = conn.getResponseCode();
        reason = conn.getResponseMessage();

        headers = new SimpleHttpHeaders();

        for(Map.Entry<String,List<String>> entry : conn.getHeaderFields().entrySet()) {
            String name = entry.getKey();
            if(null == name) {
                continue;
            }

            for(String value : entry.getValue()) {
                headers.add(name, value);
            }
        }

        bytes = readBody(conn);
    }
    
    protected byte[] readBody(HttpURLConnection conn) throws IOException {
        final InputStream input;
        if (conn.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
            input = conn.getErrorStream();
        } else {
            input = conn.getInputStream();
        }
        final byte[] body;
        if (input == null) {
            body = new byte[0];
        } else {
            try {
                final byte[] buffer = new byte[8192];
                final ByteArrayOutputStream output = new ByteArrayOutputStream();
                for (int bytes = input.read(buffer); bytes != -1; bytes = input.read(buffer)) {
                    output.write(buffer, 0, bytes);
                }
                body = output.toByteArray();
            } finally {
                input.close();
            }
        }
        return body;
    }
}
