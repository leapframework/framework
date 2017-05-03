/*
 * Copyright 2013 the original author or authors.
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
 */
package leap.orm.sql;

import leap.core.jdbc.BatchPreparedStatementHandler;
import leap.core.jdbc.PreparedStatementHandler;
import leap.core.jdbc.ResultSetReader;
import leap.db.Db;
import leap.lang.Chars;
import leap.lang.Strings;
import leap.lang.exception.NestedSQLException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.metadata.MetadataContext;
import leap.orm.query.QueryContext;
import leap.orm.reader.ResultSetReaders;

public class DefaultSqlCommand implements SqlCommand {

    private static final Log log = LogFactory.get(DefaultSqlCommand.class);

    protected final Object      source;
    protected final String      desc;
    protected final String      dbType;
    protected final SqlLanguage lang;
    protected final String      content;
    protected final String      dataSourceName;

    private boolean prepared;

	protected SqlClause[] clauses;

    public DefaultSqlCommand(Object source, String desc, String dbType, SqlLanguage lang, String content, String dataSourceName) {
        this.source  = source;
        this.desc    = desc;
        this.dbType  = dbType;
        this.lang    = lang;
        this.content = content;
        this.dataSourceName = dataSourceName;
    }

    @Override
    public void prepare(MetadataContext context) {
        if(prepared) {
            return;
        }

        try {
            this.clauses = lang.parseClauses(context,prepareSql(context, content)).toArray(new SqlClause[0]);
        } catch (Exception e) {
            throw new SqlConfigException("Error parsing sql (" + desc == null ? content : desc + "), source : " + source,e);
        }
        prepared = true;
    }

    protected void mustPrepare(SqlContext context) {
        if(!prepared) {
            prepare(context.getOrmContext());
        }
    }

    @Override
    public Object getSource() {
	    return source;
    }

    @Override
    public String getDbType() {
        return dbType;
    }

    @Override
    public String getDataSourceName() {
        return dataSourceName;
    }

    @Override
    public String getSql() {
        return content;
    }

	public SqlClause getClause() throws IllegalStateException {
		if(clauses.length > 1) {
            throw new IllegalStateException("Command '" + source + "' contains many clauses");
        }
        return clauses[0];
	}

	@Override
    public int executeUpdate(SqlContext context, Object params) throws NestedSQLException {
        log.debug("Execute update : sql '{}'", desc);
		return doExecuteUpdate(context, params, null);
    }
	
	@Override
    public int executeUpdate(SqlContext context, Object params, PreparedStatementHandler<Db> psHandler) throws IllegalStateException, NestedSQLException {
        log.debug("Execute update : sql '{}'", desc);
	    return doExecuteUpdate(context, params, psHandler);
    }

	@Override
    public <T> T executeQuery(QueryContext context, Object params,ResultSetReader<T> reader) throws NestedSQLException {
		//Assert.isTrue(null != queryClause,"This command is not a query, cannot execute query");
        log.debug("Execute query : sql '{}'", desc);
        mustPrepare(context);

		if(clauses.length == 1){
            return clauses[0].createQueryStatement(context, params).executeQuery(reader);
		}else{
			throw new IllegalStateException("Two or more sql statements in a sql command not supported now");
		}
    }
	
	@Override
    public long executeCount(QueryContext context, Object params) {
        log.debug("Execute count : sql '{}'", desc);
        mustPrepare(context);

		if(clauses.length == 1){
			return clauses[0].createCountStatement(context, params).executeQuery(ResultSetReaders.forScalarValue(Long.class, false));
		}else{
			throw new IllegalStateException("Two or more sql statements in a sql command not supported now");
		}
    }
	
	@Override
    public int[] executeBatchUpdate(SqlContext context, Object[] batchParams) throws IllegalStateException, NestedSQLException {
	    return doExecuteBatchUpdate(context, batchParams, null);
    }
	
	@Override
    public int[] executeBatchUpdate(SqlContext context, 
    								Object[] batchParams, 
    								BatchPreparedStatementHandler<Db> preparedStatementHandler) throws IllegalStateException, NestedSQLException {
	    return doExecuteBatchUpdate(context, batchParams, preparedStatementHandler);
    }

	protected int doExecuteUpdate(SqlContext context, Object params, PreparedStatementHandler<Db> psHandler) {
        mustPrepare(context);

		if(clauses.length == 1){
			return clauses[0].createUpdateStatement(context, params).executeUpdate(psHandler);
		}else{
			throw new IllegalStateException("Two or more sql statements in a sql command not supported now");
		}
	}
	
	protected int[] doExecuteBatchUpdate(SqlContext context, Object[] batchParams, BatchPreparedStatementHandler<Db> psHandler) {
        log.debug("Execute batch update : sql '{}'", desc);
        mustPrepare(context);

		if(clauses.length == 1){
			return clauses[0].createBatchStatement(context, batchParams).executeBatchUpdate(psHandler);
		}else{
			throw new IllegalStateException("Two or more sql statements in a sql command not supported now");
		}
	}

    protected String prepareSql(MetadataContext context, String content) {
        if(!Strings.containsIgnoreCase(content, IncludeProcessor.AT_INCLUDE)) {
            return content;
        }
        return new IncludeProcessor(context, content).process();
    }

	protected SqlClause checkQuery(){
		SqlClause queryClause = null;

		for(SqlClause clause : clauses){
            queryClause = clause;
            break;
		}

		return queryClause;
	}

	@Override
    public String toString() {
		return this.getClass().getSimpleName() + "[" + source + "]";
    }

    protected final class IncludeProcessor {

        private static final String INCLUDE = "include";
        private static final String AT_INCLUDE = "@" + INCLUDE;

        private final MetadataContext context;
        private final char[]          chars;

        private int pos;

        public IncludeProcessor(MetadataContext context, String content) {
            this.context = context;
            this.chars   = content.toCharArray();
        }

        public String process() {
            //@include(key;required=true|false)
            StringBuilder sb = new StringBuilder(chars.length);

            for(pos=0;pos<chars.length;pos++) {

                char c = chars[pos];

                if(c == '@') {
                    int mark = pos;
                    if(nextInclude()) {
                        String content = scanIncludeContent();
                        if(null != content) {
                            SqlFragment fragment = context.getMetadata().tryGetSqlFragment(content);
                            if(null == fragment) {
                                throw new SqlConfigException("The included sql fragment '" + content + "' not found in sql '" + desc + "', check " + source);
                            }
                            String fragmentContent = fragment.getContent();
                            if(!Strings.containsIgnoreCase(fragmentContent, IncludeProcessor.AT_INCLUDE)) {
                                sb.append(fragmentContent);
                            }else{
                                sb.append(new IncludeProcessor(context,fragmentContent).process());
                            }
                            continue;
                        }
                    }

                    sb.append(Chars.substring(chars, mark, pos));
                }

                sb.append(c);
            }

            return sb.toString();
        }

        protected boolean nextInclude() {
            int start = pos + 1;
            if(start == chars.length) {
                return false;
            }

            for(pos = start; pos < chars.length; pos++) {
                char c = chars[pos];

                if(!Character.isLetter(c)) {
                    if(pos > start) {
                        String word = Chars.substring(chars, start, pos);
                        if(Strings.equalsIgnoreCase(INCLUDE, word)) {
                            return true;
                        }
                    }

                    break;
                }
            }

            return false;
        }

        protected String scanIncludeContent() {
            if(pos == chars.length) {
                return null;
            }

            int left = 0;
            for(; pos < chars.length; pos++) {

                char c = chars[pos];

                if(left > 0) {

                    if(c == ')') {
                        return Chars.substring(chars, left + 1, pos);
                    }

                }else{
                    if(Character.isWhitespace(c)) {
                        continue;
                    }

                    if(c == '(') {
                        left = pos;
                    }
                }
            }

            return null;
        }

    }
}