/*
 *  Copyright 2018 the original author or authors.
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

/*
 *  Copyright 2018 the original author or authors.
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

package leap.web.api.remote.executors;

import leap.orm.query.CriteriaQuery;
import leap.web.api.mvc.params.CountOptions;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.orm.*;

import java.util.Map;
import java.util.function.Consumer;

public class RestModelQueryExecutor extends ModelExecutorBase implements ModelQueryExecutor {

    public RestModelQueryExecutor(ModelExecutorContext context) {
        super(context);
    }

    @Override
    public ModelQueryExecutor fromSqlView(String sql) {
        throw new IllegalStateException("Remote query does not supports fromSqlView");
    }

    @Override
    public ModelQueryExecutor selectExclude(String... names) {
        throw new IllegalStateException("Remote query does not supports selectExclude");
    }

    @Override
    public QueryOneResult queryOne(Object id, QueryOptionsBase options) {
        return null;
    }

    @Override
    public QueryListResult queryList(QueryOptions options, Map<String, Object> filters, Consumer<CriteriaQuery> callback) {
        return null;
    }

    @Override
    public QueryListResult count(CountOptions options, Consumer<CriteriaQuery> callback) {
        return null;
    }
}
