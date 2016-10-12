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
import leap.web.action.Argument;
import leap.web.api.annotation.Log;
import leap.web.api.log.model.LogModel;

import javax.swing.plaf.ButtonUI;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by kael on 2016/10/11.
 */
@Configurable(prefix = "api.log")
public class DefaultLogManager implements LogManager {
    @ConfigProperty(defaultValue = "leap.web.api.log.model.DefaultLogModel")
    protected String logClassName;
    @Inject
    protected Dao dao;

    protected Class<? extends LogModel> logClass;

    protected EntityMapping em;

    public void init() throws ClassNotFoundException {
        if(Strings.isEmpty(logClassName)){
            throw new ClassNotFoundException("logClassName can not be null!");
        }
        logClass = (Class<? extends LogModel>) Class.forName(logClassName);
        em = dao.getOrmContext().getMetadata().getEntityMapping(logClass);
    }

    protected String parseDescription(String description, Map<String, Object> vars){
        if (Strings.contains(description,EL.PREFIX)){
            StringBuilder builder = new StringBuilder();
            do{
                int prefixIndex = description.indexOf(EL.PREFIX);
                if(prefixIndex == -1){
                    builder.append(description);
                    break;
                }
                int suffixIndex = description.indexOf(EL.SUFFIX);
                builder.append(description.substring(0,prefixIndex));
                String expression = description.substring(prefixIndex,suffixIndex+1);
                description = description.substring(suffixIndex+1);
                String value = Objects.toString(EL.eval(expression,vars));
                builder.append(value);
                if(Strings.isEmpty(description)){
                    break;
                }
            }while (true);
            return builder.toString();
        }
        return description;
    }

    protected Map<String,Object> parseVars(Log annotation, ActionContext context, Object args[],LogModel log){
        Map<String, Object> vars = New.hashMap();
        vars.put("log",log);

        if(args != null && args.length > 0){
            Argument[] arguments = context.getAction().getArguments();
            for(int i = 0; i < args.length; i ++){
                vars.put(arguments[i].getName(),args[i]);
            }
        }
        if(context.getRequest().getUser() != null){
            vars.put("user",context.getRequest().getUser());
        }
        putVars(annotation,context,args,vars);
        return vars;
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
        log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        if(Strings.isNotEmpty(annotation.description())){
            String description = parseDescription(annotation.description(),parseVars(annotation,context,args,log));
            log.setDescription(description);
        }
        log.create();
    }

    protected void putVars(Log annotation, ActionContext context, Object args[],Map<String, Object> vars){

    }

}
