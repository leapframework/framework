/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.action.dyna;

import leap.lang.Buildable;
import leap.lang.Classes;
import leap.lang.Types;
import leap.web.action.Argument;
import leap.web.action.ArgumentValidator;

public class DynaArgumentBuilder implements Buildable<Argument> {

    protected String            name;
    protected Class<?>          type;
    protected Boolean           required;
    protected Argument.Location location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Argument.Location getLocation() {
        return location;
    }

    public void setLocation(Argument.Location location) {
        this.location = location;
    }

    @Override
    public Argument build() {
        return new Argument(name,
                            name,
                            null,
                            type,
                            null,
                            Types.getTypeInfo(type),
                            required,
                            location,
                            Classes.EMPTY_ANNOTATION_ARRAY,
                            null,
                            new ArgumentValidator[0],
                            new Argument[0]);
    }

}
