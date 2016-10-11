/*
 *
 *  * Copyright 2013 the original author or authors.
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

package leap.web.api.log;

import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.el.EL;
import leap.lang.Classes;
import leap.lang.New;
import leap.lang.Strings;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.model.Model;
import leap.web.action.ActionContext;
import leap.web.api.annotation.Log;
import leap.web.api.log.model.LogModel;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by kael on 2016/10/11.
 */
@Configurable(prefix = "api.log")
public class DefaultLogManager implements LogManager {
    @ConfigProperty(defaultValue = "leap.web.api.log.model.DefaultLogModel")
    private String logClassName;
    @Inject
    private Dao dao;

    private Class<? extends LogModel> logClass;

    private EntityMapping em;

    public void init() throws ClassNotFoundException {
        if(Strings.isEmpty(logClassName)){
            throw new ClassNotFoundException("logClassName can not be null!");
        }
        logClass = (Class<? extends LogModel>) Class.forName(logClassName);
        em = dao.getOrmContext().getMetadata().getEntityMapping(logClass);
    }

    @Override
    public Class<? extends Model> getLogClass() {
        if(logClass == null){
            throw new NullPointerException("property of logClass is null, is this object init?");
        }
        return logClass;
    }
    @Override
    public void saveLog(Log annotation, ActionContext context, Object args[]) throws IllegalAccessException, InstantiationException {
        LogModel log = logClass.newInstance();
        log.setId(UUID.randomUUID().toString());
        String title = Strings.isEmpty(annotation.value())?annotation.title():annotation.value();
        log.setTitle(title);
        log.setCreatedTime(new Timestamp(System.currentTimeMillis()));
        if(Strings.isNotEmpty(annotation.description())){
            log.setDescription(annotation.description());
        }
        log.create();
    }


}
