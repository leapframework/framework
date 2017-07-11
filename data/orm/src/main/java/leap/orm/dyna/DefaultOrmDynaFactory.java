/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.orm.dyna;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.db.Db;
import leap.db.DbFactory;
import leap.orm.OrmMetadata;
import leap.orm.dao.Dao;
import leap.orm.dao.DefaultDao;
import leap.orm.metadata.OrmMetadataManager;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultOrmDynaFactory implements OrmDynaFactory {

    protected @Inject BeanFactory        bf;
    protected @Inject OrmMetadataManager omm;

    private final AtomicLong counter = new AtomicLong();

    @Override
    public OrmDynaContext createDynaContext(DataSource ds) {
        final String      name = "dyna_" + counter.incrementAndGet();
        final Db          db   = DbFactory.createInstance(ds);
        final OrmMetadata md   = omm.createMetadata();

        DefaultOrmDynaContext context = bf.inject(new DefaultOrmDynaContext(name, db, md));

        Dao dao = bf.inject(new DefaultDao(context));
        context.setDao(dao);

        return context;
    }

}
