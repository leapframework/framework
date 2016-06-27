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

package leap.lang.http.client;

import leap.lang.http.exception.HttpIOException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

class JdkHttpResponse extends AbstractHttpResponse {

    protected final JdkHttpRequest    request;
    protected final HttpURLConnection conn;

    public JdkHttpResponse(JdkHttpClient client, JdkHttpRequest request, boolean immediately) throws IOException {
        super(client);

        this.request = request;
        this.conn    = request.conn;

        if(immediately) {
            readHead();
            readBody();
        }
    }

    @Override
    protected InputStream getUnderlyingInputStream() throws IOException {
        if (conn.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
            return conn.getErrorStream();
        } else {
            return conn.getInputStream();
        }
    }

    protected void readHead() {
        try{
            status  = conn.getResponseCode();
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
        }catch(IOException e) {
            throw new HttpIOException(e);
        }
    }
}
