/*
 * Copyright 2014 the original author or authors.
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
package leap.web.format;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.json.JSON;
import leap.lang.json.JsonSettings;
import leap.lang.json.JsonStringable;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.naming.NamingStyle;
import leap.lang.naming.NamingStyles;
import leap.web.json.JsonConfig;
import leap.web.json.JsonSerialize;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class JsonFormatWriter implements FormatWriter {

	private static final Log log = LogFactory.get(JsonFormatWriter.class);
	
	private JsonSettings defaultJsonSettings;

    protected @Inject BeanFactory factory;
	protected @Inject JsonConfig  defaultJsonConfig;
	
	@Override
    public void write(Writer out, Class<?> type, Type genericType, Annotation[] annotations, Object value) throws IOException {
		
		JsonSettings settings = getDefaultJsonSettings();
		
		JsonSerialize a = Classes.getAnnotation(annotations, JsonSerialize.class);
		
		if(null != a) {
			settings = createJsonSettings(a);
		}

		if(value instanceof JsonStringable) {
			((JsonStringable) value).toJson(out,settings);
			return;
		}

        if(log.isTraceEnabled()) {

            String json = JSON.encode(value, settings);

            log.trace("json output -> \n{}", json);

            out.write(json);
        }else{
            JSON.encode(value, settings, out);
        }
    }

	protected JsonSettings getDefaultJsonSettings() {
		if(null == defaultJsonSettings) {
			defaultJsonSettings =
                    new JsonSettings.Builder()
                            .setKeyQuoted(defaultJsonConfig.isDefaultSerializationKeyQuoted())
                            .setIgnoreNull(defaultJsonConfig.isDefaultSerializationIgnoreNull())
                            .setIgnoreEmpty(defaultJsonConfig.isDefaultSerializationIgnoreEmpty())
                            .setNamingStyle(defaultJsonConfig.getDefaultNamingStyle())
                            .build();
		}
		return defaultJsonSettings;
	}
	
	protected JsonSettings createJsonSettings(JsonSerialize a) {
		boolean keyQuoted   = a.keyQuoted().isNone()   ? defaultJsonConfig.isDefaultSerializationKeyQuoted()   : a.keyQuoted().getValue();
		boolean ignoreNull  = a.ignoreNull().isNone()  ? defaultJsonConfig.isDefaultSerializationIgnoreNull()  : a.ignoreNull().getValue();
		boolean ignoreEmpty = a.ignoreEmpty().isNone() ? defaultJsonConfig.isDefaultSerializationIgnoreEmpty() : a.ignoreEmpty().getValue();

        NamingStyle ns;

        if(Strings.isEmpty(a.namingStyle())){
			ns = getDefaultJsonSettings().getNamingStyle();
		}else {
			ns = NamingStyles.get(a.namingStyle());
			if(ns == null){
				ns = NamingStyles.get(a.namingStyle(), factory);
			}
			if(ns == null){
				throw new IllegalArgumentException("NamingStyle not found:"+a.namingStyle());
			}
		}

		return new JsonSettings.Builder()
                    .setKeyQuoted(keyQuoted)
                    .setIgnoreNull(ignoreNull)
                    .setIgnoreEmpty(ignoreEmpty)
                    .setNamingStyle(ns)
                    .build();
	}
}