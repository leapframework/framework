/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.orm;

import leap.core.value.Record;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;

import java.util.List;

public class RelationQueryExtension implements RelationQueryInterceptor {

    static RelationQueryExtension EMPTY = new RelationQueryExtension(null);

    protected final RelationQueryInterceptor[] interceptors;

    public RelationQueryExtension(RelationQueryInterceptor[] interceptors) {
        this.interceptors = null == interceptors ? new RelationQueryInterceptor[0] : interceptors;
    }

    @Override
    public boolean preRelateQueryOne(RelationExecutionContext context, Object relatedId, QueryOptionsBase options) {
        for(RelationQueryInterceptor interceptor : interceptors) {
            if(interceptor.preRelateQueryOne(context, relatedId, options)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean postRelateQueryOne(RelationExecutionContext context, Object relatedId, Record record) {
        for(RelationQueryInterceptor interceptor : interceptors) {
            if(interceptor.postRelateQueryOne(context, relatedId, record)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean completeRelateQueryOne(RelationExecutionContext context, Object relatedId, QueryOneResult result) {
        for(RelationQueryInterceptor interceptor : interceptors) {
            if(interceptor.completeRelateQueryOne(context, relatedId, result)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean preRelateQueryList(RelationExecutionContext context, Object relatedId, QueryOptions options) {
        for(RelationQueryInterceptor interceptor : interceptors) {
            if(interceptor.preRelateQueryList(context, relatedId, options)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean postRelateQueryList(RelationExecutionContext context, Object relatedId, List<Record> records) {
        for(RelationQueryInterceptor interceptor : interceptors) {
            if(interceptor.postRelateQueryList(context, relatedId, records)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean completeRelateQueryList(RelationExecutionContext context, Object relatedId, QueryListResult result) {
        for(RelationQueryInterceptor interceptor : interceptors) {
            if(interceptor.completeRelateQueryList(context, relatedId, result)) {
                return true;
            }
        }
        return false;
    }
}
