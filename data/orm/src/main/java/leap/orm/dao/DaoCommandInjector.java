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

package leap.orm.dao;

import leap.core.annotation.Inject;
import leap.core.ioc.BeanDefinition;
import leap.core.ioc.BeanInjector;
import leap.lang.Out;
import leap.lang.Strings;
import leap.lang.beans.BeanCreationException;
import leap.lang.reflect.ReflectValued;
import leap.orm.annotation.SqlKey;
import leap.orm.metadata.SqlRegistry;
import leap.orm.sql.SqlCommand;

import java.lang.annotation.Annotation;

public class DaoCommandInjector implements BeanInjector {

    protected @Inject SqlRegistry sqls;

    @Override
    public boolean resolveInjectValue(BeanDefinition bd, Object bean, ReflectValued v, Annotation a, Out<Object> value) {
        if (!leap.orm.dao.DaoCommand.class.equals(v.getType())) {
            return false;
        }

        KeyAndDataSource kds;
        if (a.annotationType().equals(Inject.class)) {
            kds = resolveSqlIdentity(bd, v, (Inject) a);
        } else if (a.annotationType().equals(SqlKey.class)) {
            kds = resolveSqlIdentity(bd, v, (SqlKey) a);
        } else {
            return false;
        }

        SqlCommand sql = sqls.tryGetSqlCommand(kds.key);
        if (null == sql) {
            throw new BeanCreationException("Sql key '" + kds.key + "' not found, check the bean : " + bd);
        }

        Dao dao;
        if (!Strings.isEmpty(sql.getDataSourceName())) {
            dao = Dao.get(sql.getDataSourceName());
        } else if (!Strings.isEmpty(kds.dataSource)) {
            dao = Dao.get(kds.dataSource);
        } else {
            dao = Dao.get();
        }

        value.set(new SimpleDaoCommand(dao, sql));
        return true;
    }

    protected KeyAndDataSource resolveSqlIdentity(BeanDefinition bd, ReflectValued v, Inject inject) {
        String key = Strings.firstNotEmpty(inject.name(), inject.value());

        SqlKey sk = v.getAnnotation(SqlKey.class);
        if (null != sk) {
            return resolveSqlIdentity(bd, v, sk);
        }
        if (Strings.isEmpty(key)) {
            key = v.getName();
        }
        return new KeyAndDataSource(key, null);
    }

    protected KeyAndDataSource resolveSqlIdentity(BeanDefinition bd, ReflectValued v, SqlKey a) {
        String key = a.value();
        String datasource = a.datasource();
        if (Strings.isEmpty(datasource)) {
            datasource = null;
        }
        if (Strings.isEmpty(key)) {
            throw new BeanCreationException("The value of '" + SqlKey.class + "' must not be empty, check the bean : " + bd);
        }
        return new KeyAndDataSource(key, datasource);
    }

    protected final class KeyAndDataSource {
        public final String key;
        public final String dataSource;

        public KeyAndDataSource(String key, String dataSource) {
            this.key = key;
            this.dataSource = dataSource;
        }
    }
}
