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

import leap.lang.exception.ObjectExistsException;
import leap.orm.OrmMetadata;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.metadata.OrmMetadataManager;

import javax.sql.DataSource;

public class DefaultOrmDynaContext implements OrmDynaContext {

    protected DataSource         dataSource;
    protected Dao                dao;
    protected OrmMetadata        metadata;
    protected OrmMetadataManager omm;

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public Dao getDao() {
        return dao;
    }

    @Override
    public OrmMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void addEntity(EntityMapping em) throws ObjectExistsException {

    }

    @Override
    public void destroy() {
        this.metadata   = null;
        this.dao        = null;
        this.dataSource = null;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDao(Dao dao) {
        this.dao = dao;
    }

    public void setMetadata(OrmMetadata metadata) {
        this.metadata = metadata;
    }

    public void setMetadataManager(OrmMetadataManager omm) {
        this.omm = omm;
    }

}
