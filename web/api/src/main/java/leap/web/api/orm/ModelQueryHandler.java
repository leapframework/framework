/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.orm;

import leap.core.value.Record;
import leap.orm.query.CriteriaQuery;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;

import java.util.List;

public interface ModelQueryHandler {

    void processQueryOneOptions(ModelExecutorContext context, Object id, QueryOptionsBase options);

    void preQueryOne(ModelExecutorContext context, Object id, CriteriaQuery<Record> query);

    void postQueryOne(ModelExecutorContext context, Object id, Record record);

    void processQueryListOptions(ModelExecutorContext context, QueryOptions options);

    void preProcessQueryListWhere(ModelExecutorContext context, QueryOptions options, StringBuilder where, List<Object> args);

    void handleQueryListView(ModelExecutorContext context, String viewId, StringBuilder where, List<Object> args);

    void postProcessQueryListWhere(ModelExecutorContext context, QueryOptions options, StringBuilder where, List<Object> args);

    void preQueryList(ModelExecutorContext context, CriteriaQuery<Record> query);

    void postQueryList(ModelExecutorContext context, List<Record> list);

}