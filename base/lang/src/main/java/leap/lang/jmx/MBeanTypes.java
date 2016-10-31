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

import leap.lang.Primitives;
import leap.lang.TypeInfo;
import leap.lang.Types;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;

import javax.management.openmbean.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Type utils for mbean.
 */
class MBeanTypes {

    private static final Map<String, SimpleType> SIMPLE_TYPES = new HashMap<>();
    static {
        SIMPLE_TYPES.put(SimpleType.BIGDECIMAL.getClassName(), SimpleType.BIGDECIMAL);
        SIMPLE_TYPES.put(SimpleType.BIGINTEGER.getClassName(), SimpleType.BIGINTEGER);
        SIMPLE_TYPES.put(SimpleType.BOOLEAN.getClassName(), SimpleType.BOOLEAN);
        SIMPLE_TYPES.put(SimpleType.BYTE.getClassName(), SimpleType.BYTE);
        SIMPLE_TYPES.put(SimpleType.CHARACTER.getClassName(), SimpleType.CHARACTER);
        SIMPLE_TYPES.put(SimpleType.DATE.getClassName(), SimpleType.DATE);
        SIMPLE_TYPES.put(SimpleType.DOUBLE.getClassName(), SimpleType.DOUBLE);
        SIMPLE_TYPES.put(SimpleType.FLOAT.getClassName(), SimpleType.FLOAT);
        SIMPLE_TYPES.put(SimpleType.INTEGER.getClassName(), SimpleType.INTEGER);
        SIMPLE_TYPES.put(SimpleType.LONG.getClassName(), SimpleType.LONG);
        SIMPLE_TYPES.put(SimpleType.SHORT.getClassName(), SimpleType.SHORT);
        SIMPLE_TYPES.put(SimpleType.STRING.getClassName(), SimpleType.STRING);
        SIMPLE_TYPES.put(SimpleType.VOID.getClassName(), SimpleType.VOID);
        SIMPLE_TYPES.put(SimpleType.OBJECTNAME.getClassName(), SimpleType.OBJECTNAME);
    }

    public static OpenType of(Class<?> type, Type genericType) {
        if(type == Void.class || type == Void.TYPE) {
            return SimpleType.VOID;
        }

        TypeInfo ti = Types.getTypeInfo(type, genericType);

        return of(ti);
    }

    public static OpenType of(TypeInfo ti) {

        if(ti.getType() == Void.class || ti.getType() == Void.TYPE) {
            return SimpleType.VOID;
        }

        if(ti.isSimpleType()) {
            return forSimpleType(ti.getType());
        }

        if(ti.isCollectionType()) {
            try {
                return new ArrayType(1, of(ti.getElementTypeInfo()));
            } catch (OpenDataException e) {
                throw new MException("Invalid array type", e);
            }
        }

        return forCompositeType(ti.getType());
    }

    public static SimpleType forSimpleType(Class<?> cls) {

        if(cls.isPrimitive()) {
            cls = Primitives.wrap(cls);
        }

        if(!SIMPLE_TYPES.containsKey(cls.getName())){
            throw new MException("Unsupported simple type '" + cls.getName() + "' in jmx");
        }

        return SIMPLE_TYPES.get(cls.getName());
    }

    public static CompositeType forCompositeType(Class<?> c) {
        BeanType bt = BeanType.of(c);

        List<String>   itemNames = new ArrayList<>();
        List<OpenType> itemTypes = new ArrayList<>();

        for(BeanProperty bp : bt.getProperties()) {
            if(bp.isReadable()) {
                itemNames.add(bp.getName());
                itemTypes.add(of(bp.getType(), bp.getGenericType()));
            }
        }

        String[] itemNamesArray = itemNames.toArray(new String[0]);

        try {
            return new CompositeType(c.getName(),
                                     c.getSimpleName(),
                                     itemNamesArray,
                                     itemNamesArray,
                                     itemTypes.toArray(new OpenType[0]));
        } catch (OpenDataException e) {
            throw new MException(e.getMessage(), e);
        }
    }

    protected MBeanTypes() {

    }

}