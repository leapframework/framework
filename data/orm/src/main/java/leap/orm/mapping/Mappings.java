/*
 * Copyright 2013 the original author or authors.
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
package leap.orm.mapping;

import java.util.*;
import leap.lang.Beans;
import leap.lang.Classes;
import leap.lang.Objects2;
import leap.lang.Strings;
import leap.lang.accessor.Getter;
import leap.lang.beans.BeanType;
import leap.lang.collection.WrappedCaseInsensitiveMap;
import leap.lang.convert.Converts;
import leap.lang.params.Params;
import leap.orm.value.EntityWrapper;

/**
 * Util class
 */
public class Mappings {

    public static Object getId(EntityMapping em, Object bean) {
        BeanType bt = BeanType.of(bean.getClass());

        String[] keyNames = em.getKeyFieldNames();
        if(keyNames.length == 1){
            return bt.tryGetProperty(bean, keyNames[0]);
        }

        if(keyNames.length == 0){
            return null;
        }

        Map<String, Object> id = new LinkedHashMap<String, Object>();
        for(int i=0;i<keyNames.length;i++){
            id.put(keyNames[i], bt.tryGetProperty(bean, keyNames[i]));
        }

        return id;
    }
	
	public static Object getId(EntityMapping em , Map<String, Object> attributes){
        return getId(em, attributes::get);
	}

    public static Object getId(EntityMapping em, Params params) {
        return getId(em, (Getter)params);
    }

    public static Object getId(EntityMapping em, EntityWrapper entity) {
        return getId(em, (Getter)entity);
    }

    public static Object getId(EntityMapping em, Getter getter) {
        String[] keyNames = em.getKeyFieldNames();
        if(keyNames.length == 1){
            return getter.get(keyNames[0]);
        }

        if(keyNames.length == 0){
            return null;
        }

        Map<String, Object> id = new LinkedHashMap<String, Object>();
        for(int i=0;i<keyNames.length;i++){
            id.put(keyNames[i], getter.get(keyNames[i]));
        }
        return id;
    }

    public static Object[] getIdArgs(EntityMapping em, Object id) {
        if(id instanceof Object[]) {
            return (Object[])id;
        }

        if(id instanceof Collection) {
            return ((Collection)id).toArray();
        }

        String[] keyFieldNames = em.getKeyFieldNames();
        if (keyFieldNames.length > 1) {
            Map map;
            if(id instanceof Map) {
                map = (Map)id;
            }else {
                map = Beans.toMap(id);
            }
            List<Object> args = new ArrayList<>();
            for(String name : keyFieldNames) {
                args.add(trySerialize(em, name, map.get(name)));
            }
            return args.toArray();
        } else if (keyFieldNames.length == 1) {
            return new Object[]{trySerialize(em, keyFieldNames[0], id)};
        } else {
            return new Object[]{id};
        }
    }

    protected static Object trySerialize(EntityMapping em, String field, Object value) {
        if (null == value) {
            return null;
        }
        FieldMapping fm = em.getFieldMapping(field);
        if (null != fm.getSerializer()) {
            return fm.getSerializer().trySerialize(fm, value);
        }
        Class<?> javaType = fm.getJavaType();
        if (Classes.isSimpleValueType(javaType) && !javaType.isAssignableFrom(value.getClass())) {
            return Converts.convert(value, javaType);
        }
        return value;
    }

    public static Map<String, Object> getIdAsMap(EntityMapping em, Object id) {
        Map<String, Object> m = WrappedCaseInsensitiveMap.create();
        if(em.getKeyFieldMappings().length == 1) {
            m.put(em.getKeyFieldNames()[0], id);
        }else {
            if(id instanceof Map) {
                m.putAll((Map)id);
            }else {
                m.putAll(Beans.toMap(id));
            }
        }
        return m;
    }
	
	public static String getIdToString(EntityMapping em , Map<String, Object> attributes){
		String[] keyNames = em.getKeyFieldNames();
		if(keyNames.length == 1){
			return Objects2.toStringOrEmpty(attributes.get(keyNames[0]));
		}
		
		if(keyNames.length == 0){
			return Strings.NULL;
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(int i=0;i<keyNames.length;i++){
			if(i > 0){
				sb.append(",");
			}
			sb.append(keyNames[i]).append("=").append(Objects2.toStringOrEmpty(attributes.get(keyNames[i])));
		}

		return sb.toString();
	}

	public static void setId(EntityMapping em, Map<String, Object> map, Object id) {
        String[] keyNames = em.getKeyFieldNames();
        if(keyNames.length == 1) {
            map.put(keyNames[0], id);
        }else {
            Map idMap;
            if(id instanceof Map) {
                idMap = (Map)id;
            }else {
                idMap = Beans.toMap(id);
            }
            map.putAll(idMap);
        }
    }
	
	public static Object getOptimisticLockVersion(EntityMapping em, Map<String, Object> attributes) {
		if(em.hasOptimisticLock()){
			return attributes.get(em.getOptimisticLockField().getFieldName());
		}
		return null;
	}
	
	protected Mappings(){
		
	}
}
