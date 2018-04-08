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

import leap.orm.OrmMetadata;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.web.api.config.ApiConfig;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;

public abstract class ModelExecutorBase {

    protected final ModelExecutorContext context;
    protected final ApiConfig           ac;
    protected final ApiMetadata         amd;
    protected final MApiModel           am;
    protected final Dao                 dao;
    protected final EntityMapping       em;
    protected final OrmMetadata         md;

    public ModelExecutorBase(ModelExecutorContext context) {
        this.context = context;
        this.ac  = context.getApiConfig();
        this.amd = context.getApiMetadata();
        this.am  = context.getApiModel();
        this.dao = context.getDao();
        this.em  = context.getEntityMapping();
        this.md  = dao.getOrmContext().getMetadata();
    }

}