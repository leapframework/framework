/*
 * Copyright 2015 the original author or authors.
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
package leap.web.json;

import leap.core.validation.annotations.NotNull;
import leap.lang.naming.NamingStyle;

import java.text.DateFormat;
import java.util.Collection;

public interface JsonConfigurator {
	
	String DEFAULT_JSONP_PARAMETER = "callback";
	
	JsonConfig config();

	JsonConfigurator setDefaultSerializationKeyQuoted(boolean keyQuoted);
	
	JsonConfigurator setDefaultSerializationIgnoreNull(boolean ignoreNull);
	
	JsonConfigurator setDefaultSerializationIgnoreEmpty(boolean ignoreEmpty);

	JsonConfigurator setDefaultNamingStyle(NamingStyle namingStyle);

    /**
     * Sets the pattern of default {@link DateFormat} for writing {@link java.util.Date} value.
     */
    JsonConfigurator setDefaultDateFormat(String f);
	
	JsonConfigurator setJsonpEnabled(boolean enabled);

	JsonConfigurator setJsonpResponseHeaders(boolean enabled);

	JsonConfigurator setJsonpAllowResponseHeaders(Collection<String> headerNames);
	
	JsonConfigurator setJsonpParameter(String jsonpParameter);

}