/*
 *
 *  * Copyright 2019 the original author or authors.
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

package leap.orm.dao;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.orm.metadata.SqlRegistry;
import leap.orm.sql.SqlCommand;

public class SimpleDaoCommandFactory implements DaoCommandFactory {

    protected @Inject SqlRegistry sqls;
    protected @Inject BeanFactory beanFactory;

    @Override
    public DaoCommand createDaoCommand(DaoCommandDef def) {
        SqlCommand sql = sqls.tryGetSqlCommand(def.getKey());
        if (null == sql) {
            return null;
        }

        Dao dao;
        if (!Strings.isEmpty(sql.getDataSourceName())) {
            dao = beanFactory.getBean(Dao.class, sql.getDataSourceName());
        } else if (!Strings.isEmpty(def.dataSource)) {
            dao = beanFactory.getBean(Dao.class, def.dataSource);
        } else {
            dao = beanFactory.getBean(Dao.class);
        }

        return new SimpleDaoCommand(dao, sql);
    }
}
