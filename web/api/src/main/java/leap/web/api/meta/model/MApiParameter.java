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

import java.util.Map;

import leap.lang.meta.MNamed;
import leap.lang.meta.MType;

public class MApiParameter extends MApiParameterBase implements MNamed {

	/**
	 * The location of parameter.
	 */
	public static enum Location {
		QUERY,
		BODY,
		FORM,
		PATH,
		HEADER
	}
	
	protected final Location location;

	public MApiParameter(String name, String title, String summary, String description,
                         MType type, String format, Location location, boolean required,
                         String defaultValue, String[] enumValues,
                         MApiValidation validation,
                         Map<String, Object> attrs) {
		
		super(name, title, summary, description, type, format, required, defaultValue, enumValues, validation, attrs);
		
		this.location = location;
	}
	
	public Location getLocation() {
		return location;
	}

}