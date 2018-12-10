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
package leap.htpl;

import leap.lang.Strings;

import java.util.HashMap;
import java.util.Map;

public class DefaultHtplPage implements HtplPage {
	
	protected final HtplTemplate 		template;
	protected final Map<String, Object> properties = new HashMap<String, Object>(1);
	
	public DefaultHtplPage(HtplTemplate template) {
		this.template = template;
		if(!Strings.isEmpty(template.getName())) {
            this.setProperty("name", extractPageName(template.getName()));
        }
	}

	@Override
	public Object getProperty(String name) {
		return properties.get(name);
	}

	@Override
	public void setProperty(String name, Object value) {
		properties.put(name, value);
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	@Override
    public void putProperties(Map<String, ? extends Object> m) {
		if(null != m){
			properties.putAll(m);
		}
	}

	@Override
	public HtplTemplate getTemplate() {
		return template;
	}

	private String extractPageName(String path) {
        int index = path.lastIndexOf('/');
        return index < 0 ? path : path.substring(index + 1);
    }
}
