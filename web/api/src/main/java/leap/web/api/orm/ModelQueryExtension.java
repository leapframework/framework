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
import leap.lang.jdbc.WhereBuilder;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.query.CriteriaQuery;
import leap.orm.query.PageResult;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;

import java.util.List;
import java.util.Map;

public class ModelQueryExtension implements ModelQueryInterceptor {
    private static final Log log = LogFactory.get(ModelQueryExtension.class);

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
    public boolean processQueryOneOptions(ModelExecutionContext context, QueryOptionsBase options) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            if (interceptor.processQueryOneOptions(context, options)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean preQueryOne(ModelExecutionContext context) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            if (interceptor.preQueryOne(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean preQueryOne(ModelExecutionContext context, Object id, CriteriaQuery query) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            if (interceptor.preQueryOne(context, id, query)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object processQueryOneRecord(ModelExecutionContext context, Object id, Record record) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            Object v = interceptor.processQueryOneRecord(context, id, record);
            if (null != v) {
                return v;
            }
        }
        return null;
    }

    @Override
    public void completeQueryOne(ModelExecutionContext context, QueryOneResult result, Throwable e) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            try {
                interceptor.completeQueryOne(context, result, e);
            } catch (Throwable ex) {
                log.error("Err exec {}#completeQueryOne", interceptor, ex);
            }
        }
    }

    @Override
    public boolean processQueryListOptions(ModelExecutionContext context, QueryOptions options) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            if (interceptor.processQueryListOptions(context, options)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean preProcessQueryListWhere(ModelExecutionContext context, QueryOptions options, WhereBuilder where, Map<String, Object> filters) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            if (interceptor.preProcessQueryListWhere(context, options, where, filters)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean postProcessQueryListWhere(ModelExecutionContext context, QueryOptions options, WhereBuilder where) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            if (interceptor.postProcessQueryListWhere(context, options, where)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean preQueryList(ModelExecutionContext context, CriteriaQuery query) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            if (interceptor.preQueryList(context, query)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Record> executeQueryList(ModelExecutionContext context, QueryOptions options, CriteriaQuery<Record> query) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            List<Record> list = interceptor.executeQueryList(context, options, query);
            if (null != list) {
                return list;
            }
        }
        return null;
    }

    @Override
    public Object processQueryListResult(ModelExecutionContext context, PageResult page, long totalCount, List<Record> records) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            Object v = interceptor.processQueryListResult(context, page, totalCount, records);
            if (null != v) {
                return v;
            }
        }
        return null;
    }

    @Override
    public void completeQueryList(ModelExecutionContext context, QueryListResult result, Throwable e) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            try {
                interceptor.completeQueryList(context, result, e);
            } catch (Throwable ex) {
                log.error("Err exec {}#completeQueryList", interceptor, ex);
            }
        }
    }

    @Override
    public boolean preCount(ModelExecutionContext context, CriteriaQuery query) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            if (interceptor.preCount(context, query)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void preExpand(ModelExecutionContext context) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            interceptor.preExpand(context);
        }
    }

    @Override
    public void preExpand(ModelExecutionContext context, CriteriaQuery<Record> query) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            interceptor.preExpand(context, query);
        }
    }

    @Override
    public void completeExpand(ModelExecutionContext context) {
        for (ModelQueryInterceptor interceptor : interceptors) {
            try {
                interceptor.completeExpand(context);
            } catch (Throwable ex) {
                log.error("Err exec {}#completeExpand", interceptor, ex);
            }
        }
    }
}