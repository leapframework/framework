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
import leap.lang.Lazy;
import leap.lang.Out;
import leap.lang.Strings;
import leap.lang.beans.BeanCreationException;
import leap.lang.reflect.ReflectValued;
import leap.orm.annotation.SqlKey;
import leap.orm.sql.SqlCommand;

import java.lang.annotation.Annotation;
import java.util.List;

public class DaoCommandInjector implements BeanInjector {

    protected @Inject Lazy<List<Dao>> daos;

    @Override
    public boolean resolveInjectValue(BeanDefinition bd, Object bean, ReflectValued v, Annotation a, Out<Object> value) {
        if (!leap.orm.dao.DaoCommand.class.equals(v.getType())) {
            return false;
        }

        String key;

        if(a.annotationType().equals(Inject.class)) {
            key = resolveSqlKey(bd, v, (Inject)a);
        }else if(a.annotationType().equals(SqlKey.class)) {
            key = resolveSqlKey(bd, v, (SqlKey)a);
        }else{
            return false;
        }

        for(Dao dao : daos.get()) {
            SqlCommand sc = dao.getOrmContext().getMetadata().tryGetSqlCommand(key);
            if(null != sc) {
                value.set(new SimpleDaoCommand(dao, sc));
                return true;
            }
        }

        throw new BeanCreationException("The sql key '" + key + "' not found, check the bean : " + bd);
    }

    protected String resolveSqlKey(BeanDefinition bd, ReflectValued v, Inject inject) {
        String key = Strings.firstNotEmpty(inject.name(), inject.value());
        if(!Strings.isEmpty(key)) {
            return key;
        }

        SqlKey sk = v.getAnnotation(SqlKey.class);
        if(null != sk) {
            return resolveSqlKey(bd, v, sk);
        }

        return v.getName();
    }

    protected String resolveSqlKey(BeanDefinition bd, ReflectValued v, SqlKey a) {
        String key = a.value();
        if(Strings.isEmpty(key)) {
            throw new BeanCreationException("The value of '" + SqlKey.class + "' must not be empty, check the bean : " + bd);
        }
        return key;
    }
}
