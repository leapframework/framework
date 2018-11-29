/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.spring.boot.web;

import leap.lang.Charsets;
import leap.lang.io.IO;
import leap.lang.json.JSON;
import leap.lang.json.JsonParsable;
import leap.lang.json.JsonStringable;
import leap.lang.json.JsonWriter;
import leap.lang.reflect.Reflection;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class JsonMessageConverter extends AbstractHttpMessageConverter {

    public JsonMessageConverter() {
        super(MediaType.APPLICATION_JSON);
    }

    @Override
    public boolean canRead(Class clazz, MediaType mediaType) {
        return canRead(clazz) && canRead(mediaType);
    }

    @Override
    public boolean canWrite(Class clazz, MediaType mediaType) {
        return canWrite(clazz) && canWrite(mediaType);
    }

    protected boolean canRead(Class<?> c) {
        return JsonParsable.class.isAssignableFrom(c);
    }

    protected boolean canWrite(Class<?> c) {
        return JsonStringable.class.isAssignableFrom(c);
    }

    @Override
    protected boolean supports(Class clazz) {
        return false;
    }

    protected Charset getCharset(HttpMessage message) {
        MediaType contentType = message.getHeaders().getContentType();
        Charset charset = null == contentType ? null: contentType.getCharset();
        return null == charset ? Charsets.defaultCharset() : charset;
    }

    @Override
    protected Object readInternal(Class clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        try(InputStream is = inputMessage.getBody()) {
            Object o = Reflection.newInstance(clazz);
            ((JsonParsable)o).parseJson(IO.readString(is, getCharset(inputMessage)));
            return o;
        }
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try (OutputStream os = outputMessage.getBody()){
            try (OutputStreamWriter writer = new OutputStreamWriter(os, getCharset(outputMessage))) {
                JsonWriter jsonWriter = JSON.writer(writer).create();
                if(o == null) {
                    jsonWriter.null_();
                }else {
                    ((JsonStringable)o).toJson(jsonWriter);
                }
            }
        }
    }
}
