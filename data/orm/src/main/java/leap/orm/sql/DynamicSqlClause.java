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

import leap.core.params.ParamsFactory;
import leap.db.Db;
import leap.db.DbLimitQuery;
import leap.lang.*;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.params.ArrayParams;
import leap.lang.params.EmptyParams;
import leap.lang.params.Params;
import leap.lang.value.Limit;
import leap.orm.query.QueryContext;
import leap.orm.sql.Sql.Type;
import leap.orm.sql.ast.*;

import java.util.ArrayList;
import java.util.List;

public class DynamicSqlClause extends AbstractSqlClause implements SqlClause {
	
	private static final Log log = LogFactory.get(DynamicSqlClause.class);
	
	public static final String ORDER_BY_PLACEHOLDER = "$orderBy$";
	
	protected final DynamicSqlLanguage lang;
    protected final Sql                raw;
	protected final Sql 			   sql;
	
	private Sql 	sqlForCount;	
	
	private Sql     sqlWithoutOrderByRaw;
    private Sql     sqlWithoutOrderByResolved;
	private String  defaultOrderBy;
	private boolean hasOrderByPlaceHolder;
	
	private PreparedBatchSqlStatement preparedBatchStatement;
	
    public DynamicSqlClause(DynamicSqlLanguage lang, Sql raw, Sql sql){
        Args.notNull(lang,"lang");
        Args.notNull(sql, "sql");
        this.lang = lang;
        this.raw  = raw;
        this.sql  = sql;
    }

	public Sql getSql() {
		return sql;
	}

	@Override
    public SqlStatement createUpdateStatement(SqlContext context, Object params) {
		return doCreateStatement(context, params, false);
    }
	
	@Override
    public SqlStatement createQueryStatement(QueryContext context, Object params) {
		if(log.isDebugEnabled()) {
			log.debug("Creating query statement for sql : \n {}",sql);
		}

        if(sql.isSelect()) {
            Limit limit = context.getLimit();

            if (null != limit) {
                createSqlWithoutOrderBy();
                return createLimitQueryStatement(context, params);
            }

            if (!Strings.isEmpty(context.getOrderBy())) {
                createSqlWithoutOrderBy();
                return createOrderByQueryStatement(context, params);
            }
        }

	    return doCreateStatement(context, params, true);
    }
	
	protected SqlStatement createLimitQueryStatement(QueryContext context, Object params) {
		DbLimitQuery limitQuery = new DynamicSqlLimitQuery(context, params);
		
		String sql = context.getOrmContext().getDb().getDialect().getLimitQuerySql(limitQuery);
	    
		return new DefaultSqlStatement(context, sql, limitQuery.getArgs().toArray(), Arrays2.EMPTY_INT_ARRAY);
	}
	
	protected SqlStatement createOrderByQueryStatement(QueryContext context, Object params) {
		DynamicSqlLimitQuery limitQuery = new DynamicSqlLimitQuery(context, params);
		
		DefaultSqlStatementBuilder stm = limitQuery.buildStatement(context.getOrmContext().getDb());
	    
		return stm.build();
	}
	
	@Override
    public SqlStatement createCountStatement(QueryContext context, Object params) {
		createSqlForCount();
		
		Params				       parameters = createParameters(context,params);
		DefaultSqlStatementBuilder statement  = new DefaultSqlStatementBuilder(context,true);
	    
		sqlForCount.buildStatement(statement, parameters);
		
		return statement.build();
    }

	@Override
    public BatchSqlStatement createBatchStatement(SqlContext context, Object[] params) {
		PreparedBatchSqlStatement stmt = prepareBatchSqlStatement(context);
		
		return stmt.createBatchSqlStatement(context, resolveBatchArgs(context, stmt, params));
    }
	
	private void createSqlForCount() {
		if(null == sqlForCount) {
			SqlSelect select = (SqlSelect)sql.nodes()[0];
			List<AstNode> nodes = new ArrayList<>();
			for(AstNode node : select.getNodes()){
				if(node instanceof SqlOrderBy){
					continue;
				}
				
				if(node instanceof SqlSelectList) {
					nodes.add(new Text("count(*) "));
				}else{
					nodes.add(node);	
				}
			}
			SqlSelect countSelect = new SqlSelect();
			countSelect.setDistinct(select.isDistinct());
			countSelect.setTop(select.getTop());
			countSelect.setAlias(select.getAlias());
			countSelect.setNodes(nodes.toArray(new AstNode[nodes.size()]));
			
			sqlForCount = new Sql(sql.type(), new AstNode[]{countSelect});
		}
	}
	
	private void createSqlWithoutOrderBy() {
		if(null == sqlWithoutOrderByRaw) {
			if(lang.isSimple()) {
                sqlWithoutOrderByRaw      = createSqlWithoutOrderBySimple(raw);
                sqlWithoutOrderByResolved = createSqlWithoutOrderBySimple(sql);
			}else{
                sqlWithoutOrderByRaw      = createSqlWithoutOrderByComplex(raw);
                sqlWithoutOrderByResolved = createSqlWithoutOrderByComplex(sql);
			}
		}
	}
	
	protected Sql createSqlWithoutOrderBySimple(Sql sql) {
		List<AstNode> nodes = createSqlNodesWithoutOrderBy(sql.nodes());
		
		return new Sql(sql.type(), nodes.toArray(new AstNode[nodes.size()]));
	}
	
	protected Sql createSqlWithoutOrderByComplex(Sql sql) {
		AstNode[] astNodes = sql.nodes();

		List<AstNode> selects = New.arrayList();
		for(AstNode node : astNodes){

			if(node instanceof SqlSelect){
				SqlSelect select = (SqlSelect)node;
				List<AstNode> nodes = createSqlNodesWithoutOrderBy(select.getNodes());
				SqlSelect countSelect = new SqlSelect();
				countSelect.setDistinct(select.isDistinct());
				countSelect.setTop(select.getTop());
				countSelect.setAlias(select.getAlias());
				countSelect.setNodes(nodes.toArray(new AstNode[nodes.size()]));
				selects.add(countSelect);
			}else {
				selects.add(node);
			}
		}

		return new Sql(sql.type(), selects.toArray(new AstNode[selects.size()]));
	}
	
	protected List<AstNode> createSqlNodesWithoutOrderBy(AstNode[] nodes) {
		List<AstNode> newNodes = new ArrayList<>();
		
		for(AstNode node : nodes){
			
			if(node instanceof SqlOrderBy){
				newNodes.add(new Text(ORDER_BY_PLACEHOLDER));
				
				defaultOrderBy = node.toString();
				hasOrderByPlaceHolder = true;
				continue;
			}
			
			if(node instanceof ParamReplacement){
				if(((ParamReplacement) node).getName().equalsIgnoreCase("orderBy")){
					newNodes.add(new Text(ORDER_BY_PLACEHOLDER));
					hasOrderByPlaceHolder = true;
					continue;
				}
			}
			
			newNodes.add(node);
		}
		
		return newNodes;
	}
	
	protected PreparedBatchSqlStatement prepareBatchSqlStatement(SqlContext context) {
		if(null == preparedBatchStatement) {
			synchronized (this) {
	            if(null == preparedBatchStatement){
	            	DefaultPreparedBatchSqlStatementBuilder builder = new DefaultPreparedBatchSqlStatementBuilder(sql);
	            	sql.prepareBatchSqlStatement(context, builder);
	            	preparedBatchStatement = builder.build();
	            }
            }
		}
		
		return preparedBatchStatement;
	}
	
	protected Object[][] resolveBatchArgs(SqlContext context, PreparedBatchSqlStatement stmt, Object[] params) {
		Object[][] batchArgs = new Object[params.length][];
		
		SqlParameter[] batchParameters = stmt.getBatchParameters();
		int len = batchParameters.length;
		
		for(int i=0;i<batchArgs.length;i++){
			Params parameters = createParameters(context, params[i]);
			
			Object[] args = new Object[len];
			
			for(int j=0;j<len;j++) {
				args[j] = batchParameters[j].getValue(context, parameters);
			}
			
			batchArgs[i] = args;
		}
		
		return batchArgs;
	}

	protected SqlStatement doCreateStatement(SqlContext context,Object params, boolean query){
		return doCreateStatement(sql, context, params, query);
	}

	protected SqlStatement doCreateStatement(Sql sql, SqlContext context,Object params, boolean query){
		Params				   parameters = createParameters(context,params);
		DefaultSqlStatementBuilder statement  = new DefaultSqlStatementBuilder(context,query);
	    
		sql.buildStatement(statement, parameters);
		
		return statement.build();
	}
	
	protected Params createParameters(SqlContext context, Object params){
		ParamsFactory factory = context.getOrmContext().getParameterStrategy();
		
		if(null == params){
			return EmptyParams.INSTANCE;
		}
		
		if(!params.getClass().isArray()){
			return factory.createParams(params);
		}
		
		Object[] values = Objects2.toObjectArray(params);
		
		if(values.length == 1){
			return factory.createParams(values[0]);
		}
		
		return new ArrayParams(values);
	}

	@Override
    public String toString() {
	    return sql.toString();
    }
	
	protected class DynamicSqlLimitQuery implements DbLimitQuery {
		
		private final QueryContext         context;
		private final Params               params;
		private String                     orderBy;
		private DefaultSqlStatementBuilder statement;
		
		//private boolean hasOrderByPlaceHolder = false;
		
		protected DynamicSqlLimitQuery(QueryContext context,Object params){
			this.context   = context;
			this.params    = createParameters(context,params);
			this.statement = new DefaultSqlStatementBuilder(context, true);
			
			if(!Strings.isEmpty(context.getOrderBy())){
				orderBy = "order by " + context.getOrderBy();
			}else{
				orderBy = defaultOrderBy;
			}
		}
		
		@Override
        public String getSql(Db db) {
			buildStatement(db);
			return statement.getSql().toString();
        }
		
		public DefaultSqlStatementBuilder buildStatement(Db db) {
			Sql sql;
			
			if(Strings.isEmpty(orderBy)) {
				sql = mergeMultipleSelect(sqlWithoutOrderByResolved);
			}else{
				//TODO : optimize
				String sqlWithOrderBy;
				
				String sqlWithoutOrderBy = getSqlWithoutOrderBy(db);
				
				if(hasOrderByPlaceHolder){
					sqlWithOrderBy = Strings.replace(sqlWithoutOrderBy, ORDER_BY_PLACEHOLDER, orderBy);
				}else{
					sqlWithOrderBy = db.getDialect().addOrderBy(sqlWithoutOrderBy, orderBy);
				}
				
				DynamicSqlClause sqlClauseWithOrderBy = lang.parseClause(context.getOrmContext(), sqlWithOrderBy);
				sql = sqlClauseWithOrderBy.getSql();
			}
			
			sql.buildStatement(statement, params);
			
			return statement;
		}

		@Override
        public String getSqlWithoutOrderBy(Db db) {
			return mergeMultipleSelect(sqlWithoutOrderByRaw).toSql();
        }

		protected Sql mergeMultipleSelect(Sql sqlWithoutOrderBy){
			Sql sql;

			if(sqlWithoutOrderBy.nodes().length > 1){
                /*
                        select * from t1
                        union
                        select * from t2

                        ->

                        select * from (
                            select * from t1
                            union
                            select * from t2
                        ) t
                 */
				SqlSelect select = new SqlSelect();
				String tableName = "t";
				List<AstNode> nodes = New.arrayList();
				nodes.add(new Text("select "));
				nodes.add(new SqlAllColumns(tableName));
				nodes.add(new Text(" from (\n\t\t"));
				Collections2.addAll(nodes,sqlWithoutOrderBy.nodes());
				nodes.add(new Text("\n\t) "+tableName));
				select.setNodes(nodes.toArray(new AstNode[nodes.size()]));
				sql = new Sql(Type.SELECT,new AstNode[]{select});
			}else{
				sql = sqlWithoutOrderBy;
			}
			return sql;
		}

		protected void buildSqlStatement(AstNode node,DefaultSqlStatementBuilder statement,Params parameters) {
			
			if(node instanceof SqlOrderBy){
				statement.appendText(ORDER_BY_PLACEHOLDER);
				
				if(Strings.isEmpty(orderBy)) {
					orderBy = node.toString();
				}
				
				hasOrderByPlaceHolder = true;
				return;
			}
			
			if(node instanceof ParamReplacement){
				if(((ParamReplacement) node).getName().equalsIgnoreCase("orderBy")){
					statement.appendText(ORDER_BY_PLACEHOLDER);
					hasOrderByPlaceHolder = true;
					return;
				}
			}
			
			if(node instanceof SqlNodeContainer) {
				for(AstNode c : ((SqlNodeContainer) node).getNodes()){
					buildSqlStatement(c, statement, parameters);
				}
				return;
			}

            node.buildStatement(statement, parameters);
		}

		@Override
        public String getOrderBy() {
	        return orderBy;
        }

		@Override
        public Limit getLimit() {
	        return context.getLimit();
        }

		@Override
        public List<Object> getArgs() {
	        return statement.getArgs();
        }
	}
}