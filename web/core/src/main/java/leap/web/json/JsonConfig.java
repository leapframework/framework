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

import leap.lang.naming.NamingStyle;

import java.text.DateFormat;
import java.util.Collection;
import java.util.List;

public interface JsonConfig {

	boolean isDefaultSerializationKeyQuoted();
	
	boolean isDefaultSerializationIgnoreNull();
	
	boolean isDefaultSerializationIgnoreEmpty();

	boolean isJsonpEnabled();
	
	boolean isJsonpResponseHeaders();

	boolean isHtmlEscape();

	/**
	 * 
	 * Returns the headers name that allow response in jsonp callback
	 */
	Collection<String> getJsonpAllowResponseHeaders();
	
    /**
     * Required.
     *
     * Returns the request parameter of jsonp callback.
     */
	String getJsonpParameter();

    /**
     * Required.
     *
     * Returns the default {@link NamingStyle} for writing property name.
     */
	NamingStyle getDefaultNamingStyle();

    /**
     * Optional.
     *
     * Returns the pattern of default {@link DateFormat} for {@link java.util.Date} type.
     */
    String getDefaultDateFormat();

}