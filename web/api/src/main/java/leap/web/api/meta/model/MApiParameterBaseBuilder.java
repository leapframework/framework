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

import leap.lang.meta.MType;

public abstract class MApiParameterBaseBuilder<T extends MApiParameterBase> extends MApiNamedWithDescBuilder<T> {
	
	protected MType                 type;
	protected String                format;
	protected Boolean               required;
    protected boolean               file;
    protected boolean               password;
	protected Object                defaultValue;
    protected String[]              enumValues;
	protected MApiValidationBuilder validation;
	
	public MType getType() {
		return type;
	}

	public void setType(MType type) {
		this.type = type;
	}
	
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

    public boolean isFile() {
        return file;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    public boolean isPassword() {
        return password;
    }

    public void setPassword(boolean password) {
        this.password = password;
    }

    public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String[] getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(String[] enumValues) {
        this.enumValues = enumValues;
    }

    public MApiValidationBuilder getValidation() {
		return validation;
	}

	public void setValidation(MApiValidationBuilder validation) {
		this.validation = validation;
	}

}