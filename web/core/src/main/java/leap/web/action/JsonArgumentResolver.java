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

package leap.web.action;

import leap.lang.Strings;
import leap.lang.TypeInfo;
import leap.lang.json.JSON;
import leap.lang.json.JsonParsable;
import leap.web.App;
import leap.web.route.RouteBase;

import java.lang.reflect.Type;

/**
 * Created by kael on 2017/2/20.
 */
public class JsonArgumentResolver extends AbstractArgumentResolver {

    protected final Class<?> cls;
    protected final Type genericType;
    
    protected JsonArgumentResolver(App app, RouteBase route, Argument arg) {
        super(app, route, arg);
        cls = arg.getType();
        genericType = arg.getGenericType();
    }

    @Override
    public Object resolveValue(ActionContext context, Argument argument) throws Throwable {
        String json = context.getRequest().getParameter(argument.getName());
        if(Strings.isEmpty(json)){
            return null;
        }
        if(JsonParsable.class.isAssignableFrom(cls)){
            JsonParsable parsable = (JsonParsable)cls.newInstance();
            parsable.parseJson(json);
            return parsable;
        }
        return JSON.decode(json,cls);
    }
    
}
