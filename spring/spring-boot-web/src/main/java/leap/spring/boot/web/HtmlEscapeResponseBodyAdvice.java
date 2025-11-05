/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.spring.boot.web;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import leap.lang.Arrays2;
import leap.lang.html.HTML;
import leap.lang.json.JSON;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.AntPathMatcher;
import leap.web.json.JsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class HtmlEscapeResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final Log log = LogFactory.get(HtmlEscapeResponseBodyAdvice.class);

    protected final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Autowired
    protected JsonConfig jsonConfig;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return jsonConfig.isHtmlEscape() && MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        if (null == body) {
            return null;
        }

        String[] whiteList = jsonConfig.getHtmlEscapeWhiteList();
        if (Arrays2.isNotEmpty(whiteList)) {
            String requestPath = request.getURI().getPath();
            for (String pattern : whiteList) {
                if (antPathMatcher.match(pattern, requestPath)) {
                    return body;
                }
            }
        }

        try {
            Object value = JSON.decode(JSON.encode(body));
            if (value instanceof Collection<?>) {
                for (Object item : ((Collection<?>) value)) {
                    if (item instanceof Map) {
                        escapeMap((Map) item);
                    }
                }
            } else if (value instanceof Map) {
                escapeMap((Map) value);
            }
            return value;
        } catch (Exception e) {
            log.error("Error escape html", e);
            return null;
        }
    }

    protected void escapeMap(Map map) {
        deepForeach(map, HTML::escape);
    }

    protected void deepForeach(Map map, Function<String, String> function) {
        map.forEach((key, value) -> {
            if (value instanceof String) {
                map.replace(key, function.apply((String) value));
                return;
            }
            if (value instanceof Map) {
                deepForeach((Map) value, function);
                return;
            }
            if (value instanceof Collection) {
                for (Object item : ((Collection) value)) {
                    if (item instanceof Map) {
                        deepForeach((Map) item, function);
                    }
                }
            }
        });
    }

}
