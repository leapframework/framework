/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.orm.sql;

import leap.lang.params.Params;
import leap.orm.metadata.MetadataContext;

public class DynamicSql {

    private final DynamicSqlLanguage  lang;
    private final MetadataContext     context;
    private final Sql                 sql;
    private final ExecutionSqls       sqls;
    private final SqlLanguage.Options options;

    public DynamicSql(DynamicSqlLanguage lang, MetadataContext context, Sql sql, SqlLanguage.Options options) {
        this.lang = lang;
        this.context = context;
        this.sql = sql;
        this.options = options;

        if(!sql.isDynamic()) {
            sqls = lang.parseExecutionSqls(context, sql.toSql(), options);
        }else{
            sqls = null;
        }
    }

    public ExecutionSqls resolveExecutionSqls(Params params, SqlLanguage.Options options) {
        if(null != sqls) {
            return sqls;
        }

        String text = sql.resolveDynamicSql(params);
        return lang.parseExecutionSqls(context, text, options);
    }

    @Override
    public String toString() {
        return sql.toString();
    }

    static final class ExecutionSqls  {
        public final Sql raw;
        public final Sql sql;

        public Sql sqlForCount;
        public Sql sqlWithoutOrderByRaw;
        public Sql sqlWithoutOrderByResolved;

        public String  defaultOrderBy;
        public boolean hasOrderByPlaceHolder;

        public ExecutionSqls(Sql raw, Sql sql) {
            this.raw = raw;
            this.sql = sql;
        }
    }

}