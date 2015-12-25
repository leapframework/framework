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

import leap.core.annotation.Configurable;
import leap.lang.Args;

@Configurable(prefix="webmvc.json")
public class DefaultJsonConfig implements JsonConfig,JsonConfigurator {

	protected boolean defaultSerializationKeyQuoted   = true;
	protected boolean defaultSerializationIgnoreNull  = false;
	protected boolean defaultSerializationIgnoreEmpty = false;
	protected boolean jsonpEnabled					  = true;
	protected String  jsonpParameter				  = DEFAULT_JSONP_PARAMETER;
	
	public DefaultJsonConfig() {
	    super();
    }

	@Override
    public JsonConfig config() {
	    return this;
    }

	public boolean isDefaultSerializationKeyQuoted() {
		return defaultSerializationKeyQuoted;
	}

	@Configurable.Property
	public JsonConfigurator setDefaultSerializationKeyQuoted(boolean keyQuoted) {
		this.defaultSerializationKeyQuoted = keyQuoted;
		return this;
	}

	public boolean isDefaultSerializationIgnoreNull() {
		return defaultSerializationIgnoreNull;
	}

	@Configurable.Property
	public JsonConfigurator setDefaultSerializationIgnoreNull(boolean ignoreNull) {
		this.defaultSerializationIgnoreNull = ignoreNull;
		return this;
	}

	public boolean isDefaultSerializationIgnoreEmpty() {
		return defaultSerializationIgnoreEmpty;
	}

	@Configurable.Property
	public JsonConfigurator setDefaultSerializationIgnoreEmpty(boolean ignoreEmpty) {
		this.defaultSerializationIgnoreEmpty = ignoreEmpty;
		return this;
	}

	@Configurable.Property
	public JsonConfigurator setJsonpEnabled(boolean enabled) {
		this.jsonpEnabled = enabled;
	    return this;
    }

	@Configurable.Property
    public JsonConfigurator setJsonpParameter(String jsonpParameter) {
		Args.notEmpty(jsonpParameter, "jsonp parameter");
		this.jsonpParameter = jsonpParameter;
	    return this;
    }

	@Override
    public boolean isJsonpEnabled() {
	    return jsonpEnabled;
    }

	@Override
    public String getJsonpParameter() {
	    return jsonpParameter;
    }
}