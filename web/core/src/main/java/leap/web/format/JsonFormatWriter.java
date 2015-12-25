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

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import leap.core.annotation.Inject;
import leap.lang.Classes;
import leap.lang.json.JSON;
import leap.lang.json.JsonSettings;
import leap.lang.json.JsonStringable;
import leap.web.json.JsonConfig;
import leap.web.json.JsonSerialize;

public class JsonFormatWriter implements FormatWriter {
	
	private JsonSettings defaultJsonSettings;
	
	protected @Inject JsonConfig defaultJsonConfig;
	
	@Override
    public void write(Writer out, Class<?> type, Type genericType, Annotation[] annotations, Object value) throws IOException {
		
		if(value instanceof JsonStringable) {
			((JsonStringable) value).toJson(out);
			return;
		}
		
		JsonSettings settings = getDefaultJsonSettings();
		
		JsonSerialize a = Classes.getAnnotation(annotations, JsonSerialize.class);
		
		if(null != a) {
			settings = createJsonSettings(a);
		}
		
		JSON.createEncoder(value, settings).encode(out);
    }

	protected JsonSettings getDefaultJsonSettings() {
		if(null == defaultJsonSettings) {
			defaultJsonSettings = new JsonSettings(defaultJsonConfig.isDefaultSerializationKeyQuoted(),
												   defaultJsonConfig.isDefaultSerializationIgnoreNull(),
												   defaultJsonConfig.isDefaultSerializationIgnoreEmpty());
		}
		return defaultJsonSettings;
	}
	
	protected JsonSettings createJsonSettings(JsonSerialize a) {
		boolean keyQuoted   = a.keyQuoted().isNone()   ? defaultJsonConfig.isDefaultSerializationKeyQuoted()   : a.keyQuoted().getValue();
		boolean ignoreNull  = a.ignoreNull().isNone()  ? defaultJsonConfig.isDefaultSerializationIgnoreNull()  : a.ignoreNull().getValue();
		boolean ignoreEmpty = a.ignoreEmpty().isNone() ? defaultJsonConfig.isDefaultSerializationIgnoreEmpty() : a.ignoreEmpty().getValue();
		
		return new JsonSettings(keyQuoted, ignoreNull, ignoreEmpty);
	}
}