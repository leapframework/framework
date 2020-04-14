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

import java.util.Map;
import java.util.Set;

public class ModelCreateExtension implements ModelCreateInterceptor {

    static ModelCreateExtension EMPTY = new ModelCreateExtension(null, null);

    protected final ModelCreateHandler       handler;
    protected final ModelCreateInterceptor[] interceptors;

    public ModelCreateExtension(ModelCreateHandler handler, ModelCreateInterceptor[] interceptors) {
        this.handler = handler;
        this.interceptors = null == interceptors ? new ModelCreateInterceptor[0] : interceptors;
    }

    public ModelCreateHandler getHandler() {
        return handler;
    }

    @Override
    public Object processCreationParams(ModelExecutionContext context, Object params) {
        for(ModelCreateInterceptor interceptor : interceptors) {
            Object v = interceptor.processCreationParams(context, params);
            if(null != v) {
                return v;
            }
        }
        return null;
    }

    @Override
    public boolean processCreationRecord(ModelExecutionContext context, Map<String, Object> record) {
        for(ModelCreateInterceptor interceptor : interceptors) {
            if(interceptor.processCreationRecord(context, record)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ModelDynamic resolveCreateDynamic(ModelExecutionContext context, Map<String, Object> record) {
        ModelDynamic dynamic = null;
        for(ModelCreateInterceptor interceptor : interceptors) {
            if(null != (dynamic = interceptor.resolveCreateDynamic(context, record))) {
                break;
            }
        }
        return dynamic;
    }

    @Override
    public boolean handleCreationPropertyNotFound(ModelExecutionContext context, String name, Object value, Set<String> removes) {
        for(ModelCreateInterceptor interceptor : interceptors) {
            if(interceptor.handleCreationPropertyNotFound(context, name, value, removes)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleCreationPropertyReadonly(ModelExecutionContext context, String name, Object value, Set<String> removes) {
        for(ModelCreateInterceptor interceptor : interceptors) {
            if(interceptor.handleCreationPropertyReadonly(context, name, value, removes)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean preCreateRecord(ModelExecutionContext context, Map<String, Object> record) {
        for(ModelCreateInterceptor interceptor : interceptors) {
            if(interceptor.preCreateRecord(context, record)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object postCreateRecord(ModelExecutionContext context, Object id, Record record) {
        for(ModelCreateInterceptor interceptor : interceptors) {
            Object v = interceptor.postCreateRecord(context, id, record);
            if(null != v) {
                return v;
            }
        }
        return null;
    }
}
