/*
 * Copyright 2017 the original author or authors.
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

package leap.orm.sql;

import leap.lang.Strings;
import leap.orm.metadata.MetadataContext;

public abstract class AbstractSqlCommand implements SqlCommand,SqlLanguage.Options {

    protected final SqlInfo info;

    protected boolean     prepared;
    protected SqlClause[] clauses;

    protected AbstractSqlCommand(SqlInfo info) {
        this.info = info;
    }

    @Override
    public final Object getSource() {
        return info.getSource();
    }

    @Override
    public final String getDataSourceName() {
        return info.getDataSourceName();
    }

    @Override
    public final String getDbType() {
        return info.getDbType();
    }

    @Override
    public final String getSql() {
        return info.getContent();
    }

    @Override
    public SqlMetadata getMetadata() {
        if(null == clauses) {
            throw new IllegalStateException("The command must be prepared before getting the metadata");
        }
        return clauses.length == 1 ? clauses[0].getMetadata() : null;
    }

    @Override
    public SqlClause getSqlClause() {
        return getClause();
    }

    public SqlClause getClause() throws IllegalStateException {
        if(clauses.length > 1) {
            throw new IllegalStateException("Command '" + info.getSource() + "' contains multi clauses");
        }
        return clauses[0];
    }

    @Override
    public SqlCommand prepare(MetadataContext context) {
        if(prepared) {
            return this;
        }

        try {
            this.clauses = info.getLang().parseClauses(context,prepareSql(context, info.getContent()),this).toArray(new SqlClause[0]);
        } catch (Exception e) {
            throw new SqlConfigException("Error parsing sql (" + desc() + "), source : " + info.getSource(),e);
        }

        prepared = true;
        return this;
    }

    protected void mustPrepare(SqlContext context) {
        if(!prepared) {
            prepare(context.getOrmContext());
        }
    }

    protected String prepareSql(MetadataContext context, String content) {
        if(!Strings.containsIgnoreCase(content, SqlIncludeProcessor.AT_INCLUDE)) {
            return content;
        }
        return new SqlIncludeProcessor(context, this, content).process();
    }

    protected final String desc() {
        return info.getDesc() == null ? info.getContent() : info.getDesc();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + desc() + "]";
    }
}