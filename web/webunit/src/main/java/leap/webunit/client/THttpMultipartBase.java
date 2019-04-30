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
import leap.lang.http.MimeTypes;

import java.nio.charset.Charset;

abstract class THttpMultipartBase implements THttpMultipart {

    protected final THttpRequest request;
    protected final Charset      charset;

    protected boolean empty;

    public THttpMultipartBase(THttpRequest request) {
        this.request = request;
        this.charset = request.getCharset();
    }

    @Override
    public THttpRequest request() {
        return request;
    }

    @Override
    public THttpResponse send() {
        return request.send();
    }

    boolean isEmpty() {
        return empty;
    }


    @Override
    public THttpMultipart addBytes(String name, byte[] data) {
        addBinaryPart(name, data);
        empty = false;
        return this;
    }

    @Override
    public THttpMultipart addBytes(String name, byte[] data, String contentType) {
        addBinaryPart(name, data, contentType, null);
        empty = false;
        return this;
    }

    @Override
    public THttpMultipart addFile(String name, byte[] data, String filename, String contentType) {
        Args.notEmpty(filename, "filename");

        if(null == contentType) {
            contentType = MimeTypes.getMimeType(filename);
        }

        addBinaryPart(name, data, contentType, filename);
        empty = false;
        return this;
    }

    @Override
    public THttpMultipart addText(String name, String text) {
        addTextPart(name, text, null);
        empty = false;
        return this;
    }

    @Override
    public THttpMultipart addText(String name, String text, String contentType) {
        addTextPart(name, text, contentType);
        empty = false;
        return this;
    }

    @Override
    public THttpMultipart addFile(String name, String text, String filename) {
        return addFile(name, text, filename, null);
    }

    @Override
    public THttpMultipart addFile(String name, byte[] data, String filename) {
        return addFile(name, data, filename, null);
    }

    protected abstract void addTextPart(String name, String text, String contentType);

    protected void addBinaryPart(String name, byte[] b) {
        addBinaryPart(name, b, null, null);
    }

    protected abstract void addBinaryPart(String name, byte[] b, String contentType, String filename);
}
