/*
 * Copyright 2016 the original author or authors.
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
package leap.web.action;

import leap.core.validation.Validation;
import leap.lang.Enumerables;
import leap.lang.Out;
import leap.lang.TypeInfo;
import leap.core.validation.Valid;

public class NestedArgumentValidator implements ArgumentValidator {

    protected final Valid   config;
    protected final boolean required;

    public NestedArgumentValidator(boolean required) {
        this.required = required;
        this.config   = null;
    }

    public NestedArgumentValidator(Valid config) {
        this.config    = config;
        this.required  = config.required();
    }

    @Override
    public boolean validate(Validation validation, Argument arg, Object value, Out<Object> out) {
        if(required && !validation.stateRequired(arg.getName(), value)) {
            return false;
        }

        return validate(validation, arg.getTypeInfo(), value);
    }

    protected boolean validate(Validation validation, TypeInfo ti, Object value) {
        if(ti.isComplexType()) {
            return validation.stateValidate(value);
        }

        if(ti.isCollectionType()) {
            TypeInfo eti = ti.getElementTypeInfo();
            if(eti.isComplexType()) {
                for(Object item : Enumerables.of(value)){
                    if(!validate(validation, eti, item)) {
                        return false;
                    }
                }
                return true;
            } else {
                return true;
            }
        }

        //Ignore simple type.
        return true;
    }
}