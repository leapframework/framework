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

import leap.lang.Classes;
import leap.lang.Enumerables;
import leap.lang.beans.BeanType;

import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                return convertToCompositeData(value, ct);
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

    static CompositeData convertToCompositeData(Object bean, CompositeType ct) throws OpenDataException{
        BeanType bt = BeanType.of(bean.getClass());

        Map<String,Object> map = new LinkedHashMap<>();
        for(String name : ct.keySet()) {
            OpenType type  = ct.getType(name);
            Object   value = bt.getProperty(name).getValue(bean);

            Object converted = convert(value, type);

            if(type instanceof ArrayType) {
                List list = (List)converted;

                Class<?> elementType = Classes.forName(((ArrayType) type).getElementOpenType().getClassName());
                converted = list.toArray((Object[])Array.newInstance(elementType, list.size()));
            }

            map.put(name, converted);
        }

        return new CompositeDataSupport(ct, map);
    }

    protected MConverts() {

    }
}