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

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.BeanDefinition;
import leap.core.ioc.BeanInjector;
import leap.lang.Out;
import leap.lang.Strings;
import leap.lang.beans.BeanCreationException;
import leap.lang.reflect.ReflectValued;
import leap.orm.annotation.SqlKey;

import java.lang.annotation.Annotation;

public class DaoCommandInjector implements BeanInjector {

    protected @Inject BeanFactory       beanFactory;
    protected @Inject DaoCommandFactory commandFactory;

    @Override
    public boolean resolveInjectValue(BeanDefinition bd, Object bean, ReflectValued v, Annotation a, Out<Object> value) {
        if (!leap.orm.dao.DaoCommand.class.equals(v.getType())) {
            return false;
        }

        if (null == a && Strings.isEmpty(bd.getId()) && Strings.isEmpty(bd.getName())) {
            return false;
        }

        boolean required = true;

        DaoCommandDef kds;
        if (null != a && a.annotationType().equals(Inject.class)) {
            kds = resolveSqlIdentity(bd, v, (Inject) a);
        } else if (null != a && a.annotationType().equals(SqlKey.class)) {
            SqlKey sqlKey = (SqlKey) a;
            kds = resolveSqlIdentity(bd, v, sqlKey);
            required = sqlKey.required();
        } else {
            String key = Strings.firstNotEmpty(bd.getId(), bd.getName());
            if (!Strings.isEmpty(key)) {
                kds = new DaoCommandDef(key, null);
            } else {
                return false;
            }
        }

        DaoCommand command = commandFactory.createDaoCommand(kds);
        if (null == command) {
            if (required) {
                throw new BeanCreationException("Sql key '" + kds.key + "' not found, check the bean : " + bd);
            } else {
                return false;
            }
        }
        value.set(command);
        return true;
    }

    protected DaoCommandDef resolveSqlIdentity(BeanDefinition bd, ReflectValued v, Inject inject) {
        String key = Strings.firstNotEmpty(inject.name(), inject.value());

        SqlKey sk = v.getAnnotation(SqlKey.class);
        if (null != sk) {
            return resolveSqlIdentity(bd, v, sk);
        }
        if (Strings.isEmpty(key)) {
            key = v.getName();
        }
        return new DaoCommandDef(key, null);
    }

    protected DaoCommandDef resolveSqlIdentity(BeanDefinition bd, ReflectValued v, SqlKey a) {
        String key        = Strings.firstNotEmpty(a.key(), a.value());
        String datasource = a.datasource();
        if (Strings.isEmpty(datasource)) {
            datasource = null;
        }
        if (Strings.isEmpty(key)) {
            throw new BeanCreationException("The value of '" + SqlKey.class + "' must not be empty, check the bean : " + bd);
        }
        return new DaoCommandDef(key, datasource);
    }

}
