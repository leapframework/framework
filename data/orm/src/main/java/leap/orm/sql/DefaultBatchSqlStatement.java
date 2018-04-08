/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package leap.orm.sql;

import leap.core.jdbc.BatchPreparedStatementHandler;
import leap.db.Db;
import leap.lang.Arrays2;
import leap.lang.exception.NestedSQLException;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultBatchSqlStatement implements BatchSqlStatement {

    protected final SqlContext context;
    protected final Sql        sql;
    protected final Map<String, Object[][]> sqlParamMap;

    public DefaultBatchSqlStatement(SqlContext context, Sql sql, Map<String, Object[][]> sqlParamMap){
        this.context   = context;
        this.sql       = sql;
        this.sqlParamMap = sqlParamMap;
    }

    @Override
    public int[] executeBatchUpdate() throws NestedSQLException {
        return executeBatchUpdate(null);
}

    @Override
    public int[] executeBatchUpdate(BatchPreparedStatementHandler<Db> psHandler) throws NestedSQLException {
        List<Integer> results = new ArrayList<>();
        context.getOrmContext().getDao().doTransaction(t -> {
            for (String sqlString : sqlParamMap.keySet()) {

                Object[][] params = sqlParamMap.get(sqlString);

                int[] result = context.getJdbcExecutor().executeBatchUpdate(sqlString, params, null, psHandler);

                results.addAll(Arrays.stream(result).boxed().collect(Collectors.toList()));
            }
        });
        return Arrays2.toIntArray(results);
    }
}
