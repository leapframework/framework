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

package leap.web.api.orm;

import leap.lang.Arrays2;
import leap.lang.Strings;
import leap.lang.codec.Base64;
import leap.lang.convert.Converts;
import leap.lang.jdbc.JdbcTypeKind;
import leap.lang.meta.MSimpleType;
import leap.lang.meta.MSimpleTypeKind;
import leap.orm.OrmMetadata;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.web.api.config.ApiConfig;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.meta.model.MApiProperty;
import leap.web.api.remote.RestResourceFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class ModelExecutorBase<C extends ModelExecutorContext> {

    protected final C                    context;
    protected final ApiConfig            ac;
    protected final ApiMetadata          amd;
    protected final MApiModel            am;
    protected final Dao                  dao;
    protected final EntityMapping        em;
    protected final OrmMetadata          md;
    protected final boolean              remoteRest;
    protected final RestResourceFactory  restResourceFactory;

    public ModelExecutorBase(C context) {
        this.context = context;
        this.ac  = context.getApiConfig();
        this.amd = context.getApiMetadata();
        this.am  = context.getApiModel();
        this.dao = context.getDao();
        this.em  = context.getEntityMapping();
        this.md  = dao.getOrmContext().getMetadata();
        this.remoteRest = em.isRemoteRest();
        this.restResourceFactory = context.getRestResourceFactory();
    }

    protected void tryHandleSpecialValue(Map.Entry<String, Object> entry, MApiProperty p) {
        Object value = entry.getValue();
        if(null == value) {
            return;
        }

        if(value instanceof String) {
            String string = (String) value;
            if(Strings.isBlank(string)) {
                return;
            }

            if(!p.getType().isSimpleType()){
                return;
            }

            MSimpleType type = p.getType().asSimpleType();

            if(type.getJdbcType().getKind().equals(JdbcTypeKind.Temporal)) {
                Date date = Converts.convert(string, Date.class);
                entry.setValue(date);
                return;
            }

            if(type.getSimpleTypeKind().equals(MSimpleTypeKind.BINARY)) {
                try {
                    byte[] data = Base64.decodeToBytes(string);
                    entry.setValue(data);
                    return;
                }catch (Exception e) {

                }
            }
        }
    }

    protected MApiProperty[] getProperties(ModelExecutionContext context, MApiModel am) {
        if(null == context.getDynamic()) {
            return am.getProperties();
        }else {
            return Arrays2.concat(am.getProperties(), context.getDynamic().getProperties());
        }
    }

    protected MApiProperty tryGetProperty(ModelExecutionContext context, MApiModel am, String name) {
        MApiProperty p = am.tryGetProperty(name);
        if(null == p && null != context.getDynamic()) {
            p = context.getDynamic().tryGetProperty(name);
        }
        return p;
    }

    protected static class IdOrKey {
        protected final Object              id;
        protected final Map<String, Object> key;

        protected static IdOrKey ofId(Object id) {
            return new IdOrKey(id, null);
        }

        protected static IdOrKey ofKey(Map<String, Object> key) {
            return new IdOrKey(null, key);
        }

        private IdOrKey(Object id, Map<String, Object> key) {
            this.id = id;
            this.key = key;
        }

        public boolean isId() {
            return null != id;
        }

        public boolean isKey() {
            return null != key;
        }
    }
}