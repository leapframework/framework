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

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.api.mvc.params.DeleteOptions;

public class ModelDeleteExtension implements ModelDeleteInterceptor {
    private static final Log log = LogFactory.get(ModelDeleteExtension.class);

    public static final ModelDeleteExtension EMPTY = new ModelDeleteExtension(null, null);

    protected final ModelDeleteHandler       handler;
    protected final ModelDeleteInterceptor[] interceptors;

    public ModelDeleteExtension(ModelDeleteHandler handler, ModelDeleteInterceptor[] interceptors) {
        this.handler = handler;
        this.interceptors = null == interceptors ? new ModelDeleteInterceptor[0] : interceptors;
    }

    @Override
    @Deprecated
    public boolean processDeleteOneOptions(ModelExecutorContext context, Object id, DeleteOptions options) {
        for (ModelDeleteInterceptor interceptor : interceptors) {
            if (interceptor.processDeleteOneOptions(context, id, options)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean processDeleteOneOptions(ModelExecutionContext context, Object id, DeleteOptions options) {
        for (ModelDeleteInterceptor interceptor : interceptors) {
            if (interceptor.processDeleteOneOptions(context, id, options)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean preDeleteOne(ModelExecutionContext context, Object id) {
        for (ModelDeleteInterceptor interceptor : interceptors) {
            if (interceptor.preDeleteOne(context, id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DeleteOneResult handleDeleteOne(ModelExecutionContext context, Object id, DeleteOptions options) {
        for (ModelDeleteInterceptor interceptor : interceptors) {
            DeleteOneResult result = interceptor.handleDeleteOne(context, id, options);
            if (null != result) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Object processDeleteOneResult(ModelExecutionContext context, Object id, boolean success) {
        for (ModelDeleteInterceptor interceptor : interceptors) {
            Object v = interceptor.processDeleteOneResult(context, id, success);
            if (null != v) {
                return v;
            }
        }
        return null;
    }

    @Override
    public void completeDeleteOne(ModelExecutionContext context, DeleteOneResult result, Throwable e) {
        for (ModelDeleteInterceptor interceptor : interceptors) {
            try {
                interceptor.completeDeleteOne(context, result, e);
            } catch (Throwable ex) {
                log.error("Err exec {}#completeDeleteOne", interceptor, e);
            }
        }
    }
}
