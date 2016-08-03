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

import leap.lang.Args;
import leap.lang.meta.MType;

public abstract class MApiParameterBase extends MApiNamedWithDesc {
	
	protected final MType          type;
	protected final String         format;
    protected final boolean        file;
	protected final boolean        required;
	protected final String         defaultValue;
    protected final String[]       enumValues;
	protected final MApiValidation validation;

	public MApiParameterBase(String name, String title, String summary, String description,
                             MType type, String format, boolean file, boolean required,
                             String defaultValue, String[] enumValues, MApiValidation validation,
                             Map<String, Object> attrs) {
		super(name, title, summary, description, attrs);
		
		Args.notNull(type, "type");
		
		this.type = type;
		this.format = format;
        this.file = file;
		this.required = required;
		this.defaultValue = defaultValue;
        this.enumValues = enumValues;
		this.validation = validation;
	}

	public MType getType() {
		return type;
	}

	public String getFormat() {
		return format;
	}

    public boolean isFile() {
        return file;
    }

    public boolean isRequired() {
		return required;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

    public String[] getEnumValues() {
        return enumValues;
    }

    public MApiValidation getValidation() {
		return validation;
	}

}