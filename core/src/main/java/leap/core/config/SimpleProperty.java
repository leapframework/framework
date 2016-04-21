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

package leap.core.config;

import leap.lang.Args;
import leap.lang.Types;
import leap.lang.convert.Converts;
import leap.lang.json.JSON;

public class SimpleProperty<T> extends AbstractProperty<T> {

    private final Class<T> type;
    private final boolean  complex;

    public SimpleProperty(Class<T> type) {
        this(type, null);
    }

    public SimpleProperty(Class<T> type, T value) {
        Args.notNull(type);
        this.type    = type;
        this.complex = Types.getTypeInfo(type, null).isComplexType();;
        this.value   = value;
    }

    @Override
    public void convert(String s) {
        set(doConvert(s));
    }

    @Override
    public String toString() {
        return null == value ? "null" : value.toString();
    }

    protected T doConvert(String s) {
        if(null == s) {
            return null;
        }else{
            if(complex) {
                return s.isEmpty() ? null : JSON.decode(s, type);
            }else{
                return Converts.convert(s, type);
            }
        }
    }
}
