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
import leap.lang.json.*;
import leap.lang.reflect.Reflection;
import leap.web.json.JsonSerialize;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.*;

public class JsonMessageConverter extends AbstractHttpMessageConverter implements GenericHttpMessageConverter {

    protected Boolean htmlEscape;

    public JsonMessageConverter() {
        super(MediaType.APPLICATION_JSON);
    }

    public JsonMessageConverter(Boolean htmlEscape) {
        super(MediaType.APPLICATION_JSON);
        this.htmlEscape = htmlEscape;
    }

    @Override
    protected boolean supports(Class clazz) {
        return false;
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
        return JsonParsable.class.isAssignableFrom(c) ||
                (c.isArray() && JsonParsable.class.isAssignableFrom(c.getComponentType())) ||
                c.isAnnotationPresent(JsonSerialize.class);
    }

    protected boolean canWrite(Class<?> c) {
        return JsonStringable.class.isAssignableFrom(c) ||
                (c.isArray() && JsonStringable.class.isAssignableFrom(c.getComponentType())) ||
                c.isAnnotationPresent(JsonSerialize.class);
    }

    @Override
    public boolean canRead(Type type, Class contextClass, MediaType mediaType) {
        if (type instanceof Class) {
            return canRead((Class<?>) type, mediaType);
        } else {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type              rawType           = parameterizedType.getRawType();
                if (!(rawType instanceof Class)) {
                    return false;
                }
                Class rawClass = (Class) rawType;
                if (!Collection.class.isAssignableFrom(rawClass)) {
                    return false;
                }
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length != 1) {
                    return false;
                }
                Type typeArgument = typeArguments[0];
                if (!(typeArgument instanceof Class)) {
                    return false;
                }
                return canRead((Class) typeArgument);
            }
            return false;
        }
    }


    @Override
    public boolean canWrite(Type type, Class clazz, MediaType mediaType) {
        if (type instanceof Class) {
            return canWrite((Class<?>) type, mediaType);
        } else {
            if (!Collection.class.isAssignableFrom(clazz)) {
                return false;
            }
            if (!(type instanceof ParameterizedType)) {
                return false;
            }
            Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (typeArguments.length != 1) {
                return false;
            }

            Type typeArgument = typeArguments[0];
            if (!(typeArgument instanceof Class)) {
                return false;
            }

            return canWrite((Class) typeArgument);
        }
    }

    @Override
    public Object read(Type type, Class contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        if (type instanceof Class<?>) {
            return readInternal((Class) type, inputMessage);
        } else {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class             typeArgument      = (Class) parameterizedType.getActualTypeArguments()[0];

            Collection c = newCollection((Class) parameterizedType.getRawType());
            try (InputStream is = inputMessage.getBody()) {
                try (InputStreamReader reader = new InputStreamReader(is, getCharset(inputMessage))) {
                    try {
                        JsonArray a = JSON.parse(reader).asJsonArray();
                        a.forEach(item -> {
                            if (null == item) {
                                c.add(null);
                            } else {
                                c.add(JSON.decode(item.asJsonObject(), typeArgument));
                            }
                        });
                    } catch (Exception e) {
                        throw new HttpMessageNotReadableException(e.getMessage(), e);
                    }
                }
            }
            return c;
        }
    }

    protected Collection newCollection(Class c) {
        if (List.class.equals(c)) {
            return new ArrayList();
        }
        if (Set.class.equals(c)) {
            return new LinkedHashSet();
        }
        return (Collection) Reflection.newInstance(c);
    }

    @Override
    public void write(Object o, Type type, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        if (type instanceof Class) {
            writeInternal(o, outputMessage);
        } else {
            try (OutputStream os = outputMessage.getBody()) {
                try (OutputStreamWriter writer = new OutputStreamWriter(os, getCharset(outputMessage))) {
                    JsonWriter jsonWriter = JSON.writer(writer).create();
                    jsonWriter.value(o);
                }
            }
        }
    }

    @Override
    protected Object readInternal(Class clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        try (InputStream is = inputMessage.getBody()) {
            if (clazz.isArray()) {
                try (InputStreamReader reader = new InputStreamReader(is, getCharset(inputMessage))) {
                    try {
                        JsonArray a = JSON.parse(reader).asJsonArray();

                        Object o = Array.newInstance(clazz.getComponentType(), a.length());
                        for (int i = 0; i < a.length(); i++) {
                            Array.set(o, i, JSON.decode(a.getObject(i), clazz.getComponentType()));
                        }
                        return o;
                    } catch (Exception e) {
                        throw new HttpMessageNotReadableException(e.getMessage(), e);
                    }
                }
            } else {
                try {
                    if (clazz.isAssignableFrom(JsonParsable.class)) {
                        Object o = Reflection.newInstance(clazz);
                        ((JsonParsable) o).parseJson(IO.readString(is, getCharset(inputMessage)));
                        return o;
                    } else {
                        return JSON.decode(IO.readString(is, getCharset(inputMessage)), clazz);
                    }
                } catch (Exception e) {
                    throw new HttpMessageNotReadableException(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try (OutputStream os = outputMessage.getBody()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(os, getCharset(outputMessage))) {
                JsonWriterCreator creator = JSON.writer(writer);
                if (null != htmlEscape) {
                    creator.setHtmlEscape(htmlEscape);
                }
                JsonWriter jsonWriter = creator.create();
                jsonWriter.value(o);
            }
        }
    }

    protected Charset getCharset(HttpMessage message) {
        MediaType contentType = message.getHeaders().getContentType();
        Charset   charset     = null == contentType ? null : contentType.getCharset();
        return null == charset ? Charsets.defaultCharset() : charset;
    }
}
