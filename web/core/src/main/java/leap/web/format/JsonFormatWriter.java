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
import leap.lang.Arrays2;
import leap.lang.Strings;
import leap.lang.json.JSON;
import leap.lang.json.JsonProcessor;
import leap.lang.json.JsonSettings;
import leap.lang.json.JsonStringable;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.naming.NamingStyle;
import leap.lang.naming.NamingStyles;
import leap.web.action.Action;
import leap.web.action.ActionContext;
import leap.web.action.ActionInitializable;
import leap.web.json.JsonConfig;
import leap.web.json.JsonSerialize;
import leap.web.route.RouteBuilder;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class JsonFormatWriter implements FormatWriter,ActionInitializable {

	private static final Log log = LogFactory.get(JsonFormatWriter.class);
	
	private JsonSettings defaultJsonSettings;

    protected @Inject BeanFactory factory;
	protected @Inject JsonConfig  defaultJsonConfig;

    @Override
    public void postActionInit(RouteBuilder route, Action action) {
        JsonSerialize a = action.searchAnnotation(JsonSerialize.class);
        if(null != a) {
            JsonSettings settings = createJsonSettings(a);
            route.setExtension(settings);
        }
    }

    @Override
    public void write(ActionContext context, Object value, Writer out) throws IOException {
		JsonSettings settings = context.getRoute().getExtension(JsonSettings.class);
        if(null == settings) {
            settings = getDefaultJsonSettings();
        }
		
		if(value instanceof JsonStringable) {
			((JsonStringable) value).toJson(out,settings);
			return;
		}

        if(log.isTraceEnabled()) {

            String json = JSON.encode(value, settings);

            log.trace("json output -> \n{}", Strings.abbreviate(json, 1024));

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
                            .setDateFormat(defaultJsonConfig.getDefaultDateFormat())
                            .build();
		}
		return defaultJsonSettings;
	}
	
	protected JsonSettings createJsonSettings(JsonSerialize a) {
		boolean keyQuoted   = a.keyQuoted().isNone()   ? defaultJsonConfig.isDefaultSerializationKeyQuoted()   : a.keyQuoted().getValue();
		boolean ignoreNull  = a.ignoreNull().isNone()  ? defaultJsonConfig.isDefaultSerializationIgnoreNull()  : a.ignoreNull().getValue();
		boolean ignoreEmpty = a.ignoreEmpty().isNone() ? defaultJsonConfig.isDefaultSerializationIgnoreEmpty() : a.ignoreEmpty().getValue();
        String  dateFormat  = Strings.isEmpty(a.dateFormat()) ? defaultJsonConfig.getDefaultDateFormat() : a.dateFormat();
		Class<JsonProcessor>[] processors = a.processors();
		List<JsonProcessor> processorInstances = null;
		if(Arrays2.isNotEmpty(processors)) {
			processorInstances = new ArrayList<>();
			for (Class<JsonProcessor> clazz : processors) {
				JsonProcessor processorInstance = factory.getBean(clazz);
				processorInstances.add(processorInstance);
			}
		}

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
                    .setDateFormat(dateFormat)
					.setProcessors(processorInstances)
                    .build();
	}
}