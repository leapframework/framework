/*
 *
 *  * Copyright 2019 the original author or authors.
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

package leap.web.api.orm;

import leap.core.value.Record;
import leap.orm.query.CriteriaQuery;
import leap.web.api.mvc.params.QueryOptionsBase;

public class ModelFindToQueryInterceptor implements ModelQueryInterceptor {

    public static ModelQueryInterceptor[] of(ModelFindInterceptor... findInterceptors) {
        ModelQueryInterceptor[] queryInterceptors = new ModelQueryInterceptor[findInterceptors.length];
        for(int i=0;i<queryInterceptors.length;i++) {
            queryInterceptors[i] = new ModelFindToQueryInterceptor(findInterceptors[i]);
        }
        return queryInterceptors;
    }

    private final ModelFindInterceptor findInterceptor;

    public ModelFindToQueryInterceptor(ModelFindInterceptor findInterceptor) {
        this.findInterceptor = findInterceptor;
    }

    @Override
    public boolean processQueryOneOptions(ModelExecutionContext context, QueryOptionsBase options) {
        return findInterceptor.processQueryOneOptions(context, options);
    }

    @Override
    public boolean preQueryOne(ModelExecutionContext context, Object id, CriteriaQuery query) {
        return findInterceptor.preQueryOne(context, id, query);
    }

    @Override
    public Object processQueryOneRecord(ModelExecutionContext context, Object id, Record record) {
        return findInterceptor.processQueryOneRecord(context, id, record);
    }

}
