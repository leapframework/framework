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


import leap.web.action.Argument;

public class MApiParameterBuilder extends MApiParameterBaseBuilder<MApiParameter> {

    protected Argument               argument;
    protected Argument               wrapperArgument;
	protected MApiParameter.Location location;

    public Argument getWrapperArgument() {
        return wrapperArgument;
    }

    public void setWrapperArgument(Argument wrapperArgument) {
        this.wrapperArgument = wrapperArgument;
    }

    public Argument getArgument() {
        return argument;
    }

    public void setArgument(Argument argument) {
        this.argument = argument;
    }

    public MApiParameter.Location getLocation() {
		return location;
	}

	public void setLocation(MApiParameter.Location location) {
		this.location = location;
	}

    @Override
    public MApiParameter build() {
	    return new MApiParameter(name, title, summary, description, type, format,
                                 file, password, location, required,
                                 defaultValue, enumValues,
                                 null == validation ? null : validation.build(), attrs);
    }

}