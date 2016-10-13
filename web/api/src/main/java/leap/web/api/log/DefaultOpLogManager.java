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
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.http.HTTP;
import leap.orm.dao.Dao;
import leap.orm.model.Model;
import leap.web.action.ActionContext;
import leap.web.action.Argument;
import leap.web.api.Apis;
import leap.web.api.annotation.OpLog;
import leap.web.api.config.ApiConfig;
import leap.web.api.log.model.OpLogModel;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiOperation;
import leap.web.api.meta.model.MApiPath;
import leap.web.route.Route;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by kael on 2016/10/11.
 */
@Configurable(prefix = "webapi.oplog")
public class DefaultOpLogManager implements OpLogManager {
    @ConfigProperty
    protected String logClassName;
    @Inject
    protected Dao dao;
    @Inject
    protected Apis apis;

    protected Class<? extends OpLogModel> logClass;

    public void init() throws ClassNotFoundException {
        if(!Strings.isEmpty(logClassName)){
            logClass = (Class<? extends OpLogModel>) Class.forName(logClassName);
        }
        dao.getOrmContext().getMetadata().getEntityMappingSnapshotList().forEach((em)->{
            Class<?> cls = em.getModelClass();
            if(OpLogModel.class.isAssignableFrom(cls)){
                if(logClass != null){
                    throw new RuntimeException("duplicate subclass of leap.web.api.log.model.OpLogModel, please config class name or make sure there is only one subclass of leap.web.api.log.model.OpLogModel");
                }
                logClass = (Class<? extends OpLogModel>) cls;
            }
        });

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

    protected Map<String,Object> parseVars(OpLog annotation, ActionContext context, Object args[], OpLogModel log){
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
        return logClass;
    }
    @Override
    public void saveLog(OpLog annotation, ActionContext context, Object args[]) throws IllegalAccessException, InstantiationException {
        if(logClass == null){
            return;
        }

        OpLogModel log = logClass.newInstance();
        log.setId(UUID.randomUUID().toString());
        log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        Map<String, Object> vars = parseVars(annotation,context,args,log);

        MApiOperation operation = parseApiOperation(context);

        log.setTitle(parseTitle(annotation,operation,vars));
        log.setDescription(parseDescription(annotation,operation,vars));

        log.create();
    }

    protected String parseTitle(OpLog annotation, MApiOperation operation, Map<String, Object> vars){
        String title = Strings.isEmpty(annotation.value())?annotation.title():annotation.value();
        if(Strings.isEmpty(title) && operation != null){
            title = operation.getTitle();
        }
        return title;
    }

    protected String parseDescription(OpLog annotation, MApiOperation operation, Map<String, Object> vars){
        String description = annotation.description();
        if(Strings.isNotEmpty(description)){
            description = parseDescription(description,vars);
        }
        if(Strings.isEmpty(description) && operation != null){
            description = parseDescription(operation.getDescription(),vars);
        }
        return description;
    }

    protected MApiOperation parseApiOperation(ActionContext context){
        ApiMetadata metadata = null;
        label : for(ApiConfig config : apis.getConfigurations()){
            for(Route route : config.getRoutes()){
                if(context.getResult()==route){
                    metadata = apis.tryGetMetadata(config.getName());
                    break label;
                }
            }
        }
        MApiOperation operation = null;
        if(metadata != null){
            for(Map.Entry<String, MApiPath> entry : metadata.getPaths().entrySet()){
                if(Strings.equals(entry.getKey(),context.getPath())){
                    operation = entry.getValue().getOperation(HTTP.Method.valueOf(context.getRoute().getMethod()));
                    break;
                }
            }
        }
        return operation;
    }

    protected void putVars(OpLog annotation, ActionContext context, Object args[], Map<String, Object> vars){

    }

}
