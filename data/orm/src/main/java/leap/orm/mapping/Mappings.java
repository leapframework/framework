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

import java.util.LinkedHashMap;
import java.util.Map;

import leap.lang.Objects2;
import leap.lang.Strings;
import leap.lang.accessor.Getter;
import leap.lang.beans.BeanType;

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
	
	public static Object getOptimisticLockVersion(EntityMapping em, Map<String, Object> attributes) {
		if(em.hasOptimisticLock()){
			return attributes.get(em.getOptimisticLockField().getFieldName());
		}
		return null;
	}
	
	protected Mappings(){
		
	}
}
