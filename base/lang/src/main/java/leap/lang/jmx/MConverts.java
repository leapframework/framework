/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.lang.jmx;

import leap.lang.Beans;
import leap.lang.Enumerables;

import javax.management.openmbean.*;
import java.util.ArrayList;
import java.util.List;

public class MConverts {

    static Object convert(Object value, OpenType targetType) {
        if(null == value) {
            return null;
        }

        if(targetType instanceof SimpleType) {
            return value;
        }

        if(targetType instanceof CompositeType) {
            CompositeType ct = (CompositeType)targetType;

            try {
                return new CompositeDataSupport(ct, Beans.toMap(value));
            } catch (OpenDataException e) {
                throw new MException(e);
            }
        }

        if(targetType instanceof ArrayType) {
            ArrayType at = (ArrayType)targetType;

            OpenType elementType = at.getElementOpenType();

            if(elementType instanceof SimpleType) {
                return Enumerables.of(value).toList();
            }

            List<Object> list = new ArrayList<>();

            for(Object item : Enumerables.of(value)) {
                list.add(convert(item, elementType));
            }

            return list;
        }

        //todo : support
        throw new IllegalStateException("Not supported open type '" + targetType + "'");
    }

    protected MConverts() {

    }
}