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

package leap.web.api.orm;

import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.web.api.meta.model.MApiModel;

public class DefaultModelExecutorFactory implements ModelExecutorFactory {

    @Override
    public ModelCreateExecutor newCreateExecutor(ModelExecutorConfig c, MApiModel am, Dao dao, EntityMapping em) {
        return new DefaultModelCreateExecutor(c, am, dao, em);
    }

    @Override
    public ModelUpdateExecutor newUpdateExecutor(ModelExecutorConfig c, MApiModel am, Dao dao, EntityMapping em) {
        return new DefaultModelUpdateExecutor(c, am, dao, em);
    }

    @Override
    public ModelQueryExecutor newQueryExecutor(ModelExecutorConfig c, MApiModel am, Dao dao, EntityMapping em) {
        return new DefaultModelQueryExecutor(c, am, dao, em);
    }

    @Override
    public ModelDeleteExecutor newDeleteExecutor(ModelExecutorConfig c, MApiModel am, Dao dao, EntityMapping em) {
        return new DefaultModelDeleteExecutor(c, am, dao, em);
    }

}
