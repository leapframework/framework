/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.oauth2.as.store;

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.core.store.JdbcStore;
import leap.orm.Orm;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;

public abstract class AbstractJdbcAuthzStore implements JdbcStore {

    private @Inject AppConfig config;
    
    protected Dao dao;
    protected Dmo dmo;
    
    @Override
    public void setDataSourceName(String name) {
        this.dao = Orm.dao(name);
        this.dmo = Orm.dmo(name);
        
        this.init(config);
    }

    protected abstract void init(AppConfig config);

}