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

public class ModelUpdateExtension implements ModelUpdateInterceptor {

    static ModelUpdateExtension EMPTY = new ModelUpdateExtension(null, null);

    protected final ModelUpdateHandler       handler;
    protected final ModelUpdateInterceptor[] interceptors;

    public ModelUpdateExtension(ModelUpdateHandler handler, ModelUpdateInterceptor[] interceptors) {
        this.handler = handler;
        this.interceptors = null == interceptors ? new ModelUpdateInterceptor[0] : interceptors;
    }

    public ModelUpdateHandler getHandler() {
        return handler;
    }

    @Override
    public boolean processUpdateProperties(ModelExecutorContext context, Object id, Map<String, Object> properties) {
        for(ModelUpdateInterceptor interceptor : interceptors) {
            if(interceptor.processUpdateProperties(context, id, properties)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleUpdatePropertyNotFound(ModelExecutorContext context, String name, Object value) {
        for(ModelUpdateInterceptor interceptor : interceptors) {
            if(interceptor.handleUpdatePropertyNotFound(context, name, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleUpdatePropertyReadonly(ModelExecutorContext context, String name, Object value) {
        for(ModelUpdateInterceptor interceptor : interceptors) {
            if(interceptor.handleUpdatePropertyReadonly(context, name, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean preUpdateProperties(ModelExecutorContext context, Object id, Map<String, Object> properties) {
        for(ModelUpdateInterceptor interceptor : interceptors) {
            if(interceptor.preUpdateProperties(context, id, properties)) {
                return true;
            }
        }
        return false;
    }
}
