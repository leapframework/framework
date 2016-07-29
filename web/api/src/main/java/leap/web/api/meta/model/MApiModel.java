/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.api.meta.model;

import leap.lang.Args;
import leap.lang.meta.MComplexType;

import java.util.Map;

public class MApiModel extends MApiNamedWithDesc {
	
	protected final MComplexType   type;
	protected final MApiProperty[] properties;
	
	public MApiModel(MComplexType type, MApiProperty[] properties) {
		this(type.getName(), type.getTitle(), type.getSummary(), type.getDescription(), type, properties, null);
	}

	public MApiModel(String name, MComplexType type, MApiProperty[] properties) {
		this(name, type.getTitle(), type.getSummary(), type.getDescription(), type, properties, null);
	}
	
	public MApiModel(String name, String title, MComplexType type, MApiProperty[] properties) {
		this(name, title, type.getSummary(), type.getDescription(), type, properties, null);
	}

	public MApiModel(String name, String title, String summary, String description,
                     MComplexType type, MApiProperty[] properties, Map<String, Object> attrs) {
	    super(name, title, summary, description, attrs);
	    
	    Args.notNull(type, "type");
	    this.type       = type;
	    this.properties = properties;
    }

	public MComplexType getType() {
		return type;
	}

	public MApiProperty[] getProperties() {
		return properties;
	}
	
}
