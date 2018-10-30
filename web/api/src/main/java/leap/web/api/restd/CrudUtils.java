/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.restd;

import leap.lang.Beans;
import leap.orm.mapping.EntityMapping;

import java.util.LinkedHashMap;
import java.util.Map;

public class CrudUtils {

    public static String getIdPathTemplate(RestdModel rm) {
        return getIdPathTemplate(rm.getEntityMapping());
    }

    public static String getIdPathTemplate(EntityMapping em) {
        if(em.getKeyFieldNames().length == 1) {
            return "/{" + em.getKeyFieldNames()[0] + "}";
        }else{
            StringBuilder p = new StringBuilder();
            p.append('/');

            for(int i=0;i<em.getKeyFieldNames().length;i++) {
                String name = em.getKeyFieldNames()[i];

                if(i > 0) {
                    p.append(',');
                }

                p.append('{').append(name).append('}');
            }

            return p.toString();
        }
    }

    public static String getIdPath(EntityMapping em, Object id) {
        if(em.getKeyFieldNames().length == 1) {
            return "/" + id;
        }else if(id instanceof Map){
            return getIdPath(em, (Map)id);
        }else {
            return getIdPath(em, Beans.toMap(id));
        }
    }

    public static Object getSingleOrMap(Map<String, Object> record, String... fields) {
        if(fields.length == 0) {
            return null;
        }

        if(fields.length == 1) {
            return record.get(fields[0]);
        }else {
            Map<String, Object> result = new LinkedHashMap<>();
            for(String field: fields) {
                Object v = record.get(field);
                if(null != v) {
                    result.put(field, v);
                }
            }
            return result.isEmpty() ? null : result;
        }
    }

    protected static String getIdPath(EntityMapping em, Map<String, Object> id) {
        StringBuilder s = new StringBuilder();
        s.append('/');
        for(int i=0;i<em.getKeyFieldNames().length;i++) {
            if(i > 0) {
                s.append(',');
            }
            s.append(id.get(em.getKeyFieldNames()[i]));
        }
        return s.toString();
    }

    protected CrudUtils() {

    }

}
