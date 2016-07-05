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

import leap.lang.Strings;
import leap.lang.http.Headers;
import leap.lang.http.MimeType;
import leap.lang.http.MimeTypes;
import leap.lang.http.exception.HttpIOException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractHttpResponse<C extends HttpClient> implements HttpResponse {

    protected C           client;
    protected Integer     status;
    protected HttpHeaders headers;
    protected MimeType    contentType;
    protected byte[]      bytes;

    public AbstractHttpResponse(C client) {
        this.client = client;
    }

    @Override
    public int getStatus() {
        if(null == status) {
            readHead();
        }
        return status;
    }

    @Override
    public HttpHeaders getHeaders() {
        if(null == headers) {
            readHead();
        }
        return headers;
    }

    @Override
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
    public byte[] getBytes() throws HttpIOException {
        if(null == bytes) {
            readBody();
        }
        return bytes;
    }

    public String getString() throws HttpIOException{
        return Strings.newString(getBytes(), charset());
    }

    public InputStream getInputStream() throws HttpIOException {
        if(null == bytes){
            try {
                return getUnderlyingInputStream();
            }catch(IOException e) {
                throw new HttpIOException(e);
            }
        }else{
            return new ByteArrayInputStream(bytes);
        }
    }

    protected String charset() {
        MimeType mt = getContentType();
        if(null != mt && !Strings.isEmpty(mt.getCharset())) {
            return mt.getCharset();
        }else{
            return client.getDefaultCharset().name();
        }
    }

    protected abstract void readHead();

    protected abstract InputStream getUnderlyingInputStream() throws IOException;

    protected void readBody() throws HttpIOException {
        try {
            try(InputStream is = getInputStream()) {
                if (is == null) {
                    bytes = new byte[0];
                } else {
                    final byte[] buffer = new byte[8192];
                    final ByteArrayOutputStream output = new ByteArrayOutputStream();
                    for (int bytes = is.read(buffer); bytes != -1; bytes = is.read(buffer)) {
                        output.write(buffer, 0, bytes);
                    }
                    bytes = output.toByteArray();
                }
            }
        } catch (IOException e) {
            throw new HttpIOException(e);
        }
    }
}
