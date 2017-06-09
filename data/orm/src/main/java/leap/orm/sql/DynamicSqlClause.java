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
import leap.lang.params.BeanParams;
import leap.lang.params.EmptyParams;
import leap.lang.params.Params;
import leap.lang.value.Limit;
import leap.orm.query.QueryContext;
import leap.orm.sql.Sql.Type;
import leap.orm.sql.ast.*;
import leap.orm.value.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DynamicSqlClause extends AbstractSqlClause implements SqlClause {
	
	private static final Log log = LogFactory.get(DynamicSqlClause.class);
	
	public static final String ORDER_BY_PLACEHOLDER = "$orderBy$";
	
	protected final DynamicSqlLanguage lang;
    protected final DynamicSql         sql;

    private PreparedBatchSqlStatement preparedBatchStatement;
	
    public DynamicSqlClause(DynamicSqlLanguage lang, DynamicSql sql){
        this.lang = lang;
        this.sql  = sql;
    }

	@Override
    public SqlStatement createUpdateStatement(SqlContext context, Object p) {
        Params params = createParameters(context, p);
        DynamicSql.ExecutionSqls sqls = sql.resolveExecutionSqls(params);

        return doCreateStatement(context, sqls, params, false);
    }
	
	@Override
    public SqlStatement createQueryStatement(QueryContext context, Object p) {
		if(log.isDebugEnabled()) {
			log.debug("Creating query statement for sql : \n {}",sql);
		}

        Params params = createParameters(context, p);
        DynamicSql.ExecutionSqls sqls = sql.resolveExecutionSqls(params);

        if(sqls.sql.isSelect()) {
            Limit limit = context.getLimit();

            if (null != limit) {
                createSqlWithoutOrderBy(sqls);
                return createLimitQueryStatement(context, sqls, params);
            }

            if (!Strings.isEmpty(context.getOrderBy())) {
                createSqlWithoutOrderBy(sqls);
                return createOrderByQueryStatement(context, sqls, params);
            }
        }

	    return doCreateStatement(context, sqls, params, true);
    }
	
	protected SqlStatement createLimitQueryStatement(QueryContext context, DynamicSql.ExecutionSqls sqls, Params params) {
        DynamicSqlLimitQuery limitQuery = new DynamicSqlLimitQuery(context, sqls, params);
		
		String sql = context.dialect().getLimitQuerySql(limitQuery);
	    
		return new DefaultSqlStatement(context, limitQuery.sql, sql, limitQuery.getArgs().toArray(), Arrays2.EMPTY_INT_ARRAY);
	}
	
	protected SqlStatement createOrderByQueryStatement(QueryContext context, DynamicSql.ExecutionSqls sqls, Params params) {
		DynamicSqlLimitQuery limitQuery = new DynamicSqlLimitQuery(context, sqls, params);
		
		DefaultSqlStatementBuilder stm = limitQuery.buildStatement(context.getOrmContext().getDb());
	    
		return stm.build();
	}
	
	@Override
    public SqlStatement createCountStatement(QueryContext context, Object p) {
        Params params = createParameters(context, p);
        DynamicSql.ExecutionSqls sqls = sql.resolveExecutionSqls(params);

        createSqlForCount(sqls);
		
		DefaultSqlStatementBuilder statement = new DefaultSqlStatementBuilder(context, sqls.sqlForCount, true);
	    
		sqls.sqlForCount.buildStatement(context, statement, params);
		
		return statement.build();
    }

	@Override
    public BatchSqlStatement createBatchStatement(SqlContext context, Object[] params) {
        DynamicSql.ExecutionSqls sqls = sql.resolveExecutionSqls(Params.empty());

		PreparedBatchSqlStatement stmt = prepareBatchSqlStatement(context, sqls,params);
		
		return stmt.createBatchSqlStatement(context, resolveBatchArgs(context, stmt, params));
    }
	
	private void createSqlForCount(DynamicSql.ExecutionSqls sqls) {
		if(null == sqls.sqlForCount) {
			List<SqlSelect> selects = new ArrayList<>();
			for(AstNode astNode : sqls.sql.nodes()){
				if(astNode instanceof SqlSelect){
					selects.add((SqlSelect)astNode);
				}
			}

			SqlSelect countSelect = new SqlSelect();

			if(selects.size() == 1){
				SqlSelect select = (SqlSelect)sqls.sql.nodes()[0];
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

				countSelect.setDistinct(select.isDistinct());
				countSelect.setTop(select.getTop());
				countSelect.setAlias(select.getAlias());
				countSelect.setNodes(nodes.toArray(new AstNode[nodes.size()]));

			}else{

				List<AstNode> nodes = new ArrayList<>();
				nodes.add(new SqlSelect());
				nodes.add(new Text("SELECT count(*) FROM (\n\t\t"));
				nodes.addAll(Arrays.asList(sqls.sql.nodes()));
				nodes.add(new Text("\n\t\t) t"));
				countSelect.setNodes(nodes.toArray(new AstNode[nodes.size()]));

			}
			sqls.sqlForCount = new Sql(sqls.sql.type(), new AstNode[]{countSelect});
		}
	}
	
	private void createSqlWithoutOrderBy(DynamicSql.ExecutionSqls sqls) {
		if(null == sqls.sqlWithoutOrderByRaw) {
			if(lang.isSimple()) {
                sqls.sqlWithoutOrderByRaw      = createSqlWithoutOrderBySimple(sqls, sqls.raw);
                sqls.sqlWithoutOrderByResolved = createSqlWithoutOrderBySimple(sqls, sqls.sql);
			}else{
                sqls.sqlWithoutOrderByRaw      = createSqlWithoutOrderByComplex(sqls, sqls.raw);
                sqls.sqlWithoutOrderByResolved = createSqlWithoutOrderByComplex(sqls, sqls.sql);
			}
		}
	}
	
	protected Sql createSqlWithoutOrderBySimple(DynamicSql.ExecutionSqls sqls, Sql sql) {
		List<AstNode> nodes = createSqlNodesWithoutOrderBy(sqls, sql.nodes());
		
		return new Sql(sql.type(), nodes.toArray(new AstNode[nodes.size()]));
	}
	
	protected Sql createSqlWithoutOrderByComplex(DynamicSql.ExecutionSqls sqls, Sql sql) {
		AstNode[] astNodes = sql.nodes();

		List<AstNode> selects = New.arrayList();
		for(AstNode node : astNodes){

			if(node instanceof SqlSelect){
				SqlSelect select = (SqlSelect)node;
				List<AstNode> nodes = createSqlNodesWithoutOrderBy(sqls, select.getNodes());
				SqlSelect newSelect = new SqlSelect();
				newSelect.setDistinct(select.isDistinct());
				newSelect.setTop(select.getTop());
                newSelect.addSelectItemAliases(select.getSelectItemAliases());
				newSelect.setAlias(select.getAlias());
				newSelect.setNodes(nodes.toArray(new AstNode[nodes.size()]));
                newSelect.setUnion(select.isUnion());
				selects.add(newSelect);
			}else {
				selects.add(node);
			}
		}

		return new Sql(sql.type(), selects.toArray(new AstNode[selects.size()]));
	}
	
	protected List<AstNode> createSqlNodesWithoutOrderBy(DynamicSql.ExecutionSqls sqls, AstNode[]nodes) {
		List<AstNode> newNodes = new ArrayList<>();
		
		for(AstNode node : nodes){
			
			if(node instanceof SqlOrderBy){
				newNodes.add(new Text(ORDER_BY_PLACEHOLDER));
				
				sqls.defaultOrderBy = node.toString();
				sqls.hasOrderByPlaceHolder = true;
				continue;
			}
			
			if(node instanceof ParamReplacement){
				if(((ParamReplacement) node).getName().equalsIgnoreCase("orderBy")){
					newNodes.add(new Text(ORDER_BY_PLACEHOLDER));
					sqls.hasOrderByPlaceHolder = true;
					continue;
				}
			}
			
			newNodes.add(node);
		}
		
		return newNodes;
	}
	
	protected PreparedBatchSqlStatement prepareBatchSqlStatement(SqlContext context, DynamicSql.ExecutionSqls sqls,Object[] params) {
		if(null == preparedBatchStatement) {
			synchronized (this) {
	            if(null == preparedBatchStatement){
	            	DefaultPreparedBatchSqlStatementBuilder builder =
                            new DefaultPreparedBatchSqlStatementBuilder(sqls.sql);
	            	sqls.sql.prepareBatchSqlStatement(context, builder,params);
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

        Boolean writeGeneratedValue = null;
		
		for(int i=0;i<batchArgs.length;i++){
            Object object     = params[i];
			Params parameters = createParameters(context, object);
			
			Object[] args = new Object[len];
			
			for(int j=0;j<len;j++) {
                SqlParameter p = batchParameters[j];
                SqlValue     v = p.getValue(context, parameters);

				args[j] = null == v ? null : v.get();

                if(v.isGenerated()) {

                    if(null == writeGeneratedValue && !parameters.isReadonly()) {
                        writeGeneratedValue = true;
                    }

                    if(null != writeGeneratedValue && writeGeneratedValue) {
                        String name = ((NamedSqlParameter) p).getName();
                        parameters.set(name, v.get());
                    }
                }
			}
			
			batchArgs[i] = args;
		}
		
		return batchArgs;
	}

	protected SqlStatement doCreateStatement(SqlContext context, DynamicSql.ExecutionSqls sqls, Params params, boolean query){
		return doCreateStatement(context, sqls.sql, params, query);
	}

	protected SqlStatement doCreateStatement(SqlContext context, Sql sql, Params params, boolean query){
		DefaultSqlStatementBuilder statement = new DefaultSqlStatementBuilder(context, sql, query);
	    
		sql.buildStatement(context, statement, params);
		
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

        private final QueryContext             context;
        private final DynamicSql.ExecutionSqls sqls;
        private final Params                   params;

        private Sql                        sql;
		private String                     orderBy;
		private DefaultSqlStatementBuilder statement;
		
		//private boolean hasOrderByPlaceHolder = false;
		
		protected DynamicSqlLimitQuery(QueryContext context, DynamicSql.ExecutionSqls sqls, Params params){
			this.context = context;
            this.sqls    = sqls;
			this.params  = params;

			if(!Strings.isEmpty(context.getOrderBy())){
				orderBy = "order by " + context.getOrderBy();
			}else{
				orderBy = sqls.defaultOrderBy;
			}
		}
		
		@Override
        public String getSql(Db db) {
			buildStatement(db);
			return statement.getText().toString();
        }
		
		public DefaultSqlStatementBuilder buildStatement(Db db) {
			if(Strings.isEmpty(orderBy)) {
				sql = mergeMultipleSelect(sqls.sqlWithoutOrderByResolved);
			}else{
				//TODO : optimize
				String sqlWithOrderBy;
				
				String sqlWithoutOrderBy = getSqlWithoutOrderBy(db);
				
				if(sqls.hasOrderByPlaceHolder){
					sqlWithOrderBy = Strings.replace(sqlWithoutOrderBy, ORDER_BY_PLACEHOLDER, orderBy);
				}else{
					sqlWithOrderBy = db.getDialect().addOrderBy(sqlWithoutOrderBy, orderBy);
				}
				
				sql = lang.parseExecutionSqls(context.getOrmContext(), sqlWithOrderBy).sql;
			}

            if(sql.isSelect()) {
                Sql original = sqls.sql;
                if(original.isSelect() && original.nodes()[0] instanceof SqlSelect) {
                    SqlSelect select = (SqlSelect)sql.nodes()[0];
                    select.addSelectItemAliases(((SqlSelect)original.nodes()[0]).getSelectItemAliases());
                }
            }

            this.statement = new DefaultSqlStatementBuilder(context, sql, true);
			sql.buildStatement(context, statement, params);
			
			return statement;
		}

		@Override
        public String getSqlWithoutOrderBy(Db db) {
			return mergeMultipleSelect(sqls.sqlWithoutOrderByRaw).toSql();
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

		protected void buildSqlStatement(SqlContext context, AstNode node,DefaultSqlStatementBuilder statement,Params parameters) {
			
			if(node instanceof SqlOrderBy){
				statement.appendText(ORDER_BY_PLACEHOLDER);
				
				if(Strings.isEmpty(orderBy)) {
					orderBy = node.toString();
				}
				
				sqls.hasOrderByPlaceHolder = true;
				return;
			}
			
			if(node instanceof ParamReplacement){
				if(((ParamReplacement) node).getName().equalsIgnoreCase("orderBy")){
					statement.appendText(ORDER_BY_PLACEHOLDER);
					sqls.hasOrderByPlaceHolder = true;
					return;
				}
			}
			
			if(node instanceof SqlNodeContainer) {
				for(AstNode c : ((SqlNodeContainer) node).getNodes()){
					buildSqlStatement(context, c, statement, parameters);
				}
				return;
			}

            node.buildStatement(context, statement, parameters);
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