/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package leap.web.api.orm;

import leap.core.value.Record;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;

import java.util.List;

public class ModelQueryExtension implements ModelQueryInterceptor {

    public static final ModelQueryExtension EMPTY = new ModelQueryExtension(null, null);

    protected final ModelQueryHandler       handler;
    protected final ModelQueryInterceptor[] interceptors;

    public ModelQueryExtension(ModelQueryHandler handler, ModelQueryInterceptor[] interceptors) {
        this.handler = handler;
        this.interceptors = null == interceptors ? new ModelQueryInterceptor[0] : interceptors;
    }

    public ModelQueryHandler getHandler() {
        return handler;
    }

    @Override
    public boolean processQueryOneOptions(ModelExecutorContext context, QueryOptionsBase options) {
        for(ModelQueryInterceptor interceptor : interceptors) {
            if(interceptor.processQueryOneOptions(context, options)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object processQueryOneRecord(ModelExecutorContext context, Object id, Record record) {
        for(ModelQueryInterceptor interceptor : interceptors) {
            Object v = interceptor.processQueryOneRecord(context, id, record);
            if(null != v) {
                return v;
            }
        }
        return null;
    }

    @Override
    public boolean processQueryListOptions(ModelExecutorContext context, QueryOptions options) {
        for(ModelQueryInterceptor interceptor : interceptors) {
            if(interceptor.processQueryListOptions(context, options)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean preProcessQueryListWhere(ModelExecutorContext context, QueryOptions options, StringBuilder where, List<Object> args) {
        for(ModelQueryInterceptor interceptor : interceptors) {
            if(interceptor.preProcessQueryListWhere(context, options, where, args)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean postProcessQueryListWhere(ModelExecutorContext context, QueryOptions options, StringBuilder where, List<Object> args) {
        for(ModelQueryInterceptor interceptor : interceptors) {
            if(interceptor.postProcessQueryListWhere(context, options, where, args)) {
                return true;
            }
        }
        return false;
    }
}