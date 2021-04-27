/*
 * Copyright 2016 the original author or authors.
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
package leap.web;

import leap.lang.Strings;
import leap.lang.http.Headers;
import leap.lang.io.IO;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class RequestWrapper extends HttpServletRequestWrapper {

    private HttpServletRequest request;
    private byte[]             bytes;
    private boolean            get_reader;
    private boolean            get_stream;

    public RequestWrapper(HttpServletRequest wrapped) {
        super(wrapped);
        this.request = wrapped;
    }

    public BufferedInputStream peekInputStream() throws IOException {
        return new BufferedInputStream(new ByteArrayInputStream(peekBytes()));
    }

    public byte[] peekBytes() throws IOException {
        if(get_reader || get_stream) {
            throw new IllegalStateException("getInputStream or getReader has been called on this request");
        }

        if (null == bytes) {
            bytes = IO.readByteArray(request.getInputStream());
        }

        return bytes;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (get_stream) {
            throw new IllegalStateException("getInputStream method has been called on this request");
        }

        if (null != bytes) {
            return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), getCharacterEncoding()));
        }

        get_reader = true;
        return super.getReader();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (get_reader) {
            throw new IllegalStateException("getReader method has been called on this request");
        }

        if (null != bytes) {
            return new AppFilter.ServletInputStreamWrapper(new ByteArrayInputStream(bytes));
        }

        get_stream = true;
        return super.getInputStream();
    }

    public void destroy() {
        bytes = null;
    }

    @Override
    public String getMethod() {
        String overrideMethod = request.getHeader(Headers.X_HTTP_METHOD_OVERRIDE);

        if (!Strings.isEmpty(overrideMethod)) {
            return Strings.upperCase(overrideMethod);
        }

        return super.getMethod();
    }

    public Map<String, Object> getAttributes() {
        Enumeration<String> names = request.getAttributeNames();
        if (!names.hasMoreElements()) {
            return Collections.EMPTY_MAP;
        }
        Map<String, Object> attributes = new HashMap<>();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            attributes.put(name, request.getAttribute(name));
        }
        return Collections.unmodifiableMap(attributes);
    }

}
