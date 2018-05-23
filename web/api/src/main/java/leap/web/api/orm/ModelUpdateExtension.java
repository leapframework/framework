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

import java.util.Map;
import java.util.Set;

public class ModelUpdateExtension implements ModelUpdateInterceptor, ModelReplaceInterceptor {

    static ModelUpdateExtension EMPTY = new ModelUpdateExtension(null, null, null);

    protected final ModelUpdateHandler        handler;
    protected final ModelUpdateInterceptor[]  updateInterceptors;
    protected final ModelReplaceInterceptor[] replaceInterceptors;

    public ModelUpdateExtension(ModelUpdateHandler handler,
                                ModelUpdateInterceptor[] updateInterceptors, ModelReplaceInterceptor[] replaceInterceptors) {
        this.handler = handler;
        this.updateInterceptors = null == updateInterceptors ? new ModelUpdateInterceptor[0] : updateInterceptors;
        this.replaceInterceptors = null == replaceInterceptors ? new ModelReplaceInterceptor[0] : replaceInterceptors;
    }

    public ModelUpdateHandler getHandler() {
        return handler;
    }

    @Override
    public boolean processUpdateProperties(ModelExecutionContext context, Object id, Map<String, Object> properties) {
        for(ModelUpdateInterceptor interceptor : updateInterceptors) {
            if(interceptor.processUpdateProperties(context, id, properties)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleUpdatePropertyNotFound(ModelExecutionContext context, String name, Object value, Set<String> removes) {
        for(ModelUpdateInterceptor interceptor : updateInterceptors) {
            if(interceptor.handleUpdatePropertyNotFound(context, name, value, removes)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleUpdatePropertyReadonly(ModelExecutionContext context, String name, Object value, Set<String> removes) {
        for(ModelUpdateInterceptor interceptor : updateInterceptors) {
            if(interceptor.handleUpdatePropertyReadonly(context, name, value, removes)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object postUpdateProperties(ModelExecutionContext context, Object id, int affected) {
        for(ModelUpdateInterceptor interceptor : updateInterceptors) {
            Object v = interceptor.postUpdateProperties(context, id, affected);
            if(null != v) {
                return v;
            }
        }
        return null;
    }

    @Override
    public boolean processReplaceRecord(ModelExecutionContext context, Object id, Map<String, Object> record) {
        for(ModelReplaceInterceptor interceptor : replaceInterceptors) {
            if(interceptor.processReplaceRecord(context, id, record)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleReplacePropertyNotFound(ModelExecutionContext context, String name, Object value, Set<String> removes) {
        for(ModelReplaceInterceptor interceptor : replaceInterceptors) {
            if(interceptor.handleReplacePropertyNotFound(context, name, value, removes)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleReplacePropertyReadonly(ModelExecutionContext context, String name, Object value, Set<String> removes) {
        for(ModelReplaceInterceptor interceptor : replaceInterceptors) {
            if(interceptor.handleReplacePropertyReadonly(context, name, value, removes)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object postReplaceRecord(ModelExecutionContext context, Object id, int affected) {
        for(ModelReplaceInterceptor interceptor : replaceInterceptors) {
            Object v = interceptor.postReplaceRecord(context, id, affected);
            if(null != v) {
                return v;
            }
        }
        return null;
    }
}
