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
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.beans.BeanCreationException;
import leap.lang.beans.BeanType;
import leap.lang.reflect.ReflectValued;
import leap.orm.annotation.SqlKey;
import leap.orm.sql.SqlCommand;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

public class DaoCommandInjector implements BeanInjector {

    protected @Inject Lazy<List<Dao>> daos;

    @Override
    public Set<Class<? extends Annotation>> getSupportedAnnotationTypes() {
        return New.hashSet(SqlKey.class);
    }

    @Override
    public Object resolveInjectValue(BeanDefinition bd, Object bean, BeanType bt, ReflectValued v, Annotation a) {
        if (!leap.orm.dao.DaoCommand.class.equals(v.getType())) {
            throw new BeanCreationException("The type of '" + v + "' must be '" + DaoCommand.class + "' in bean '" + bean + "'");
        }

        SqlKey c = (SqlKey)a;

        String key = c.value();
        if(Strings.isEmpty(key)) {
            throw new BeanCreationException("The value of '" + SqlKey.class + "' must not be empty, check the bean : " + bd);
        }

        for(Dao dao : daos.get()) {
            SqlCommand sc = dao.getOrmContext().getMetadata().tryGetSqlCommand(key);
            if(null != sc) {
                return new SimpleDaoCommand(dao, sc);
            }
        }

        throw new BeanCreationException("The sql key '" + key + "' not found, check the bean : " + bd);
    }

}
