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
package leap.orm.query;

import leap.core.exception.TooManyRecordsException;
import leap.core.jdbc.ResultSetReader;
import leap.core.jdbc.SimpleScalarReader;
import leap.core.jdbc.SimpleScalarsReader;
import leap.core.value.Scalar;
import leap.core.value.Scalars;
import leap.db.DbDialect;
import leap.lang.*;
import leap.lang.beans.DynaBean;
import leap.lang.params.ArrayParams;
import leap.lang.params.MapArrayParams;
import leap.lang.params.Params;
import leap.lang.value.Limit;
import leap.orm.OrmContext;
import leap.orm.dao.Dao;
import leap.orm.mapping.*;
import leap.orm.reader.ResultSetReaders;
import leap.orm.sql.SqlClause;
import leap.orm.sql.SqlFactory;
import leap.orm.sql.SqlStatement;
import leap.orm.sql.ast.SqlObjectName;
import leap.orm.sql.ast.SqlWhereExpr;
import leap.orm.sql.parser.SqlParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DefaultCriteriaQuery<T> extends AbstractQuery<T> implements CriteriaQuery<T>,QueryContext {

    protected SqlBuilder              builder;
    protected Predicate<FieldMapping> selectFilter;
    protected String                  sqlView;
    protected List<JoinBuilder>       joins = new ArrayList<>(1);
    protected String                  where;
    protected ArrayParams             whereParameters;
    protected StringBuilder           joinByIdWhere;
    protected List                    joinByIdArgs;
    protected boolean                 distinct;
    protected String                  groupBy;
    protected String                  having;

	public DefaultCriteriaQuery(Dao dao, EntityMapping em, Class<T> targetType) {
	    super(dao, targetType, em);
	    Args.notNull(em,"entity mapping");
	    this.builder = new SqlBuilder();
    }

    @Override
    public CriteriaQuery<T> fromSqlView(String sql) {
        this.sqlView = sql;
        return this;
    }

    @Override
    public CriteriaQuery<T> params(Map<String, Object> params) {
	    return (CriteriaQuery<T>)super.params(params);
    }
	
	@Override
    public CriteriaQuery<T> params(Params params) {
	    return (CriteriaQuery<T>)super.params(params);
    }
	
	@Override
    public CriteriaQuery<T> params(DynaBean bean) {
	    return (CriteriaQuery<T>)super.params(bean);
    }
	
	@Override
    public CriteriaQuery<T> param(String name, Object value) {
	    return (CriteriaQuery<T>)super.param(name, value);
    }
	
	@Override
    public CriteriaQuery<T> limit(int startRows, int endRows) {
	    return (CriteriaQuery<T>)super.limit(startRows, endRows);
    }

	@Override
    public EntityMapping getEntityMapping() {
	    return em;
    }

    @Override
    public String alias() {
        return builder.alias;
    }

    @Override
    public CriteriaQuery<T> alias(String alias) {
		Args.notEmpty(alias,"alias");
		builder.alias = alias;
	    return this;
    }

    @Override
    public CriteriaQuery<T> join(JoinBuilder builder) {
        joins.add(builder);
        return this;
    }

    @Override
    public CriteriaQuery<T> join(Class<?> targetEntityClass, String alias) {
        return join(targetEntityClass, null, alias);
    }

    @Override
    public CriteriaQuery<T> join(Class<?> targetEntityClass, String localRelation, String alias) {
        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityClass);

        return join(em, localRelation, alias, JoinType.INNER);
    }

    @Override
    public CriteriaQuery<T> join(String targetEntityName, String alias) {
        return join(targetEntityName, null, alias);
    }

    @Override
    public CriteriaQuery<T> join(String targetEntityName, String localRelation, String alias) {
        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityName);

        return join(em, localRelation, alias, JoinType.INNER);
    }

    @Override
    public CriteriaQuery<T> joinById(Class<?> targetEntityClass, String alias, Object id) {
        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityClass);

        return joinById(em, null, alias, JoinType.INNER, id);
    }

    @Override
    public CriteriaQuery<T> joinById(String targetEntityName, String alias, Object id) {
        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityName);

        return joinById(em, null, alias, JoinType.INNER, id);
    }

    @Override
    public CriteriaQuery<T> joinById(Class<?> targetEntityName, String localRelation, String alias, Object id) {
        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityName);
        return joinById(em, localRelation, alias, JoinType.INNER, id);
    }

    @Override
    public CriteriaQuery<T> joinById(String targetEntityName, String localRelation, String alias, Object id) {
        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityName);

        return joinById(em, localRelation, alias, JoinType.INNER, id);
    }

    public CriteriaQuery<T> joinWithWhere(Class<?> targetEntityClass, String alias,
                                          Appendable where, Consumer<FieldMapping> args) {

        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityClass);

        return join(em, null, alias, JoinType.INNER, where, args);
    }

    public CriteriaQuery<T> joinWithWhere(String targetEntityName, String localRelation, String alias,
                                          Appendable where, Consumer<FieldMapping> args) {

        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityName);

        return join(em, localRelation, alias, JoinType.INNER, where, args);
    }

    public CriteriaQuery<T> joinWithWhere(String targetEntityName, String alias,
                                          Appendable where, Consumer<FieldMapping> args) {

        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityName);

        return join(em, null, alias, JoinType.INNER, where, args);
    }

    @Override
    public CriteriaQuery<T> joinWithWhere(Class<?> targetEntityClass, String localRelation, String alias,
                                          Appendable where, Consumer<FieldMapping> args) {

        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityClass);

        return join(em, localRelation, alias, JoinType.INNER, where, args);
    }

    @Override
    public CriteriaQuery<T> leftJoin(Class<?> targetEntityClass, String alias) {
        return join(targetEntityClass, null, alias);
    }

    @Override
    public CriteriaQuery<T> leftJoin(Class<?> targetEntityClass, String localRelation, String alias) {
        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityClass);

        return join(em, localRelation, alias, JoinType.LEFT);
    }

    @Override
    public CriteriaQuery<T> leftJoin(String targetEntityName, String alias) {
        return join(targetEntityName, null, alias);
    }

    @Override
    public CriteriaQuery<T> leftJoin(String targetEntityName, String localRelation, String alias) {
        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityName);

        return join(em, localRelation, alias, JoinType.LEFT);
    }

    @Override
    public CriteriaQuery<T> LeftJoinById(String targetEntityName, String alias, Object id) {
        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityName);

        return joinById(em, null, alias, JoinType.LEFT, id);
    }

    @Override
    public CriteriaQuery<T> LeftJoinById(Class<?> targetEntityClass, String alias, Object id) {
        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityClass);

        return joinById(em, null, alias, JoinType.LEFT, id);
    }

    @Override
    public CriteriaQuery<T> LeftJoinById(String targetEntityName, String localRelation, String alias, Object id) {
        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityName);

        return joinById(em, localRelation, alias, JoinType.LEFT, id);
    }

    @Override
    public CriteriaQuery<T> LeftJoinById(Class<?> targetEntityClass, String localRelation, String alias, Object id) {
        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityClass);

        return joinById(em, localRelation, alias, JoinType.LEFT, id);
    }

    public CriteriaQuery<T> leftJoinWithWhere(Class<?> targetEntityClass, String alias,
                                              Appendable where, Consumer<FieldMapping> args) {

        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityClass);

        return join(em, null, alias, JoinType.LEFT, where, args);
    }

    public CriteriaQuery<T> leftJoinWithWhere(Class<?> targetEntityClass, String localRelation, String alias,
                                              Appendable where, Consumer<FieldMapping> args) {

        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityClass);

        return join(em, localRelation, alias, JoinType.LEFT, where, args);
    }

    public CriteriaQuery<T> leftJoinWithWhere(String targetEntityName, String alias,
                                              Appendable where, Consumer<FieldMapping> args) {

        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityName);

        return join(em, null, alias, JoinType.LEFT, where, args);
    }

    public CriteriaQuery<T> leftJoinWithWhere(String targetEntityName, String localRelation, String alias,
                                              Appendable where, Consumer<FieldMapping> args) {

        EntityMapping em =
                context.getMetadata().getEntityMapping(targetEntityName);

        return join(em, localRelation, alias, JoinType.LEFT, where, args);
    }

    protected CriteriaQuery<T> joinById(EntityMapping target, String relation, String alias, JoinType type, Object id) {
        if(null == joinByIdWhere) {
            joinByIdWhere = new StringBuilder();
            joinByIdArgs = new ArrayList();
        }else{
            joinByIdWhere.append(" and ");
        }

        return join(target, relation, alias, type, joinByIdWhere, (n) -> joinByIdArgs.add(id));
    }

    protected CriteriaQuery<T> join(EntityMapping target, String relation, String alias, JoinType type) {
        return join(target, relation, alias, type, null, null);
    }

    protected CriteriaQuery<T> join(EntityMapping target, String relation, String alias, JoinType type,
                                    Appendable where, Consumer<FieldMapping> args) {

        Args.notEmpty(alias, "alias");

        RelationMapping rm;
        if(!Strings.isEmpty(relation)) {
            rm = em.getRelationMapping(relation);
        }else{
            rm = em.tryGetRelationMappingOfTargetEntity(target.getEntityName());

            if(null == rm) {
                throw new IllegalStateException("Cannot join : no unique relation of the join entity '" +
                        target.getEntityName() + "' in entity '" + em.getEntityName() + "'");
            }
        }

        joins.add(new RelationJoin(target, alias, type, rm));

        if(null != where) {

            try{
                //many-to-one
                if(rm.isManyToOne() || rm.isOneToMany()) {
                    //todo : only one key columns allowed.
                    FieldMapping key = target.getKeyFieldMappings()[0];

                    where.append(alias).append('.').append(key.getFieldName()).append(" in ?");

                    args.accept(key);
                    return this;
                }

                //many-to-many
                if(rm.isManyToMany()) {
//                    EntityMapping join = context.getMetadata().getEntityMapping(rm.getJoinEntityName());
//
//                    RelationMapping joinRelation =
//                            join.tryGetKeyRelationMappingOfTargetEntity(target.getEntityName());

                    //todo : only one key columns allowed.
                    FieldMapping key =
                            target.getKeyFieldMappings()[0];

                    where.append(alias).append('.').append(key.getFieldName()).append(" in ?");
                    args.accept(key);
                    return this;
                }

                throw new IllegalStateException("Not handled relation type '" + rm.getType() + "'");

            }catch(IOException e) {
                throw Exceptions.wrap(e);
            }
        }

        return this;
    }

    @Override
    public CriteriaQuery<T> whereById(Object id) {

        StringBuilder s = new StringBuilder();

        for(int i=0;i<em.getKeyColumnNames().length;i++){
            if(i > 0){
                s.append(" and ");
            }

            s.append(builder.alias).append('.')
                    .append(em.getKeyColumnNames()[i])
                    .append("=?");
        }

        return where(s.toString(), Mappings.getIdArgs(em, id));
    }

    @Override
    public CriteriaQuery<T> whereByReference(Class<?> refEntityClass, Object refToId) {
        EntityMapping target = context.getMetadata().getEntityMapping(refEntityClass);
        RelationMapping rm = em.tryGetRefRelationMappingOfTargetEntity(target.getEntityName());

        if(null == rm) {
            throw new IllegalStateException("No unique many-to-one relation in entity '" +
                                            em.getEntityName() + "' ref to '" + target.getEntityName() + "'");
        }

        return whereByReference(rm,refToId);
    }

    @Override
    public CriteriaQuery<T> whereByReference(String refEntityName, Object refToId) {
        RelationMapping rm = em.tryGetRefRelationMappingOfTargetEntity(refEntityName);

        if(null == rm) {
            throw new IllegalStateException("No unique many-to-one relation in entity '" +
                    em.getEntityName() + "' ref to '" + refEntityName + "'");
        }

        return whereByReference(rm,refToId);
    }

    @Override
    public CriteriaQuery<T> whereByReference(RelationMapping rm, Object id) {
        Args.notNull(rm, "relation");
        Assert.isTrue(rm.isManyToOne(), "The relation must be many-to-one");

        StringBuilder s = new StringBuilder();

        for(int i=0;i<rm.getJoinFields().length;i++){
            if(i > 0){
                s.append(" and ");
            }

            JoinFieldMapping jf = rm.getJoinFields()[i];

            s.append(builder.alias).append('.')
                    .append(jf.getLocalFieldName())
                    .append("=?");
        }

        return where(s.toString(), id);
    }

    @Override
    public CriteriaWhere<T> where() {
	    return new DefaultCriteriaWhere<T>(getOrmContext(), this);
    }
	
	@Override
    public CriteriaQuery<T> where(String expression) {
		Args.notEmpty(expression = Strings.trim(expression),"where expression");
		where = expression;
	    return this;
    }
	
	@Override
    public CriteriaQuery<T> where(String expression, Object... args) {
		Args.notEmpty(expression = Strings.trim(expression),"where expression");
		where = expression;
		
		if(null != args && args.length > 0){
            if(args.length == 1 && args[0] instanceof ArrayParams){
                this.whereParameters = (ArrayParams)args[0];
            }else{
                this.whereParameters = new ArrayParams(args);
            }
		}
		
	    return this;
    }

    @Override
    public CriteriaQuery<T> distinct() {
        this.distinct = true;
        return this;
    }

    @Override
    public CriteriaQuery<T> select(String... selection) {
		if(null == selection || selection.length == 0){
			builder.selects = null;
		}else{
			builder.selects = columns(selection);
		}
	    return this;
    }

    @Override
    public CriteriaQuery<T> selectExclude(String... fields) {
        if(null != fields && fields.length > 0) {
            List<String> select = new ArrayList<>();
            for(FieldMapping fm : em.getFieldMappings()) {
                boolean exclude = false;
                for(String excludeField : fields) {
                    if(fm.getFieldName().equalsIgnoreCase(excludeField)) {
                        exclude = true;
                        break;
                    }
                }
                if(!exclude) {
                    select.add(fm.getFieldName());
                }
            }
            builder.selects = columns(select.toArray(new String[select.size()]));
        }
        return this;
    }

    @Override
    public CriteriaQuery<T> select(Predicate<FieldMapping> filter) {
		this.selectFilter = filter;
	    return this;
    }

	@Override
    public CriteriaQuery<T> groupBy(String expression) {
	    this.groupBy = expression;
	    return this;
    }

	@Override
    public CriteriaQuery<T> having(String expression) {
		this.having = expression;
	    return this;
    }
	
	@Override
    public CriteriaQuery<T> limit(Integer size) {
	    return (CriteriaQuery<T>)super.limit(size);
    }

	@Override
    public CriteriaQuery<T> limit(Limit limit) {
	    return (CriteriaQuery<T>)super.limit(limit);
    }

	@Override
    public CriteriaQuery<T> orderBy(String expression) {
	    return (CriteriaQuery<T>)super.orderBy(expression);
    }

	@Override
    public CriteriaQuery<T> orderByIdAsc() {
		orderById("asc");
		return this;
    }

	@Override
    public CriteriaQuery<T> orderByIdDesc() {
		orderById("desc");
	    return this;
    }
	
	protected void orderById(String order) {
		StringBuilder s = new StringBuilder();

		for(int i=0;i<em.getKeyColumnNames().length;i++){
			if(i > 0){
				s.append(",");
			}
			
			s.append(builder.alias).append('.')
			 .append(em.getKeyColumnNames()[i])
			 .append(' ')
			 .append(order);
		}
		
		orderBy(s.toString());
	}

	@Override
    public long count() {
		String sql = builder.buildCountSql();
		SqlStatement statement = createQueryStatement(this, sql, true);
	    return statement.executeQuery(ResultSetReaders.forScalarValue(Long.class, false));
    }
	
	@Override
    public int delete() {
        if(em.hasSecondaryTable()) {
            String secondarySql = builder.buildSecondaryDeleteSql(true);
            String primarySql   = builder.buildSecondaryDeleteSql(false);

            return dao.doTransaction((s) -> {
                createUpdateStatement(this, secondarySql).executeUpdate();
                return createUpdateStatement(this, primarySql).executeUpdate();
            });
        }else{
            return createUpdateStatement(this,builder.buildDeleteSql()).executeUpdate();
        }
    }

	@Override
    public int update(Map<String, Object> fields) {
		Args.notEmpty(fields,"update fields");

        if(em.hasSecondaryTable()) {
            Map<String, Object> params = paramsMap();

            String secondarySql = builder.buildSecondaryUpdateSql(fields, params, true);
            String primarySql   = builder.buildSecondaryUpdateSql(fields, params, false);

            if(null == secondarySql && null == primarySql) {
                throw new IllegalStateException("No update fields");
            }

            if(null == secondarySql) {
                return createUpdateStatement(this,primarySql).executeUpdate();
            }

            if(null == primarySql) {
                return createUpdateStatement(this,secondarySql).executeUpdate();
            }

            //The updates may change the result of where condition, so can't update both primary & secondary table.
            throw new IllegalStateException("The updated fields can not contains both primary & secondary fields in an update query");
//            return dao.doTransaction((s) -> {
//                int result = createUpdateStatement(this, primarySql).executeUpdate();
//
//                createUpdateStatement(this,secondarySql).executeUpdate();
//
//                return result;
//            });
        }else{
            String sql = builder.buildUpdateSql(fields, paramsMap());
            if(null == sql) {
                throw new IllegalStateException("No update fields");
            }
            return createUpdateStatement(this,sql).executeUpdate();
        }
    }

	@Override
    protected QueryResult<T> executeQuery(QueryContext qc) {
		String sql = builder.buildSelectSql();
		SqlStatement statement = createQueryStatement(qc,sql);
		
		ResultSetReader<List<T>> reader = ResultSetReaders.forListEntity(dao.getOrmContext(), qc,em, targetType, targetType);
		
		return new DefaultQueryResult<T>(sql,statement.executeQuery(reader));
    }
	
	@Override
    protected Scalar executeQueryForScalar(QueryContext context) throws TooManyRecordsException {
	    return buildQueryStatement(context).executeQuery(SimpleScalarReader.DEFAULT_INSTANCE);
    }

	@Override
    protected Scalars executeQueryForScalars(QueryContext context) throws TooManyRecordsException {
	    return buildQueryStatement(context).executeQuery(SimpleScalarsReader.DEFAULT_INSTANCE);
    }

    protected Object[] args() {
        if(null == whereParameters && null == joinByIdWhere) {
            return null;
        }

        if(null != whereParameters && null == joinByIdArgs) {
            return whereParameters.array();
        }

        if(null == whereParameters && null != joinByIdArgs) {
            return joinByIdArgs.toArray(Arrays2.EMPTY_OBJECT_ARRAY);
        }

        throw new IllegalStateException("Cannot combine with joinById and where expr in one query");
    }
	
	protected SqlStatement buildQueryStatement(QueryContext qc) {
		return createQueryStatement(qc,builder.buildSelectSql(), false);
	}

    protected SqlStatement createCountStatement(QueryContext qc, String sql) {
       return createQueryStatement(qc, sql, true);
    }

    protected SqlStatement createQueryStatement(QueryContext qc, String sql) {
        return createQueryStatement(qc, sql, false);
    }

	protected SqlStatement createQueryStatement(QueryContext qc, String sql, boolean count) {
		SqlClause clause = context.getQueryFactory().createQueryClause(dao, sql);

        Object queryParams;
        Object[] args = args();
        if(null == args) {
            queryParams = params();
        }else {
            queryParams = new MapArrayParams(paramsMap(), args);
        }

        if(count) {
            //Count query don't add the order by.
            String tmpOrderBy = this.orderBy;
            this.orderBy = null;
            SqlStatement statement =  clause.createQueryStatement(qc, queryParams);
            this.orderBy = tmpOrderBy;
            return statement;
        }else{
            return clause.createQueryStatement(qc, queryParams);
        }

	}
	
	protected SqlStatement createUpdateStatement(QueryContext qc, String sql) {
		SqlClause clause = context.getQueryFactory().createQueryClause(dao, sql);
		
		Object updateParams;
        Object[] args = args();
		if(null == args) {
			updateParams = params();
		}else {
			updateParams = new MapArrayParams(paramsMap(), args);
		}
		
		return clause.createUpdateStatement(qc, updateParams);
	}
	
	protected String[] columns(String[] fields){
		String[] columns = new String[fields.length];
		
		for(int i=0;i<fields.length;i++){
			columns[i] = column(fields[i]);
		}
		
		return columns;
	}
	
	protected String column(String field){
		FieldMapping fm = em.tryGetFieldMapping(field);
		return null == fm ? field : fm.getColumnName();
	}

    protected enum JoinType {
        INNER,
        LEFT
    }

    protected static class RelationJoin implements JoinBuilder {

        final RelationMapping relation;
        final EntityMapping   target;
        final String          alias;
        final JoinType        type;

        protected RelationJoin(EntityMapping target, String alias, JoinType type, RelationMapping relation) {
            this.target      = target;
            this.alias       = alias;
            this.type        = type;
            this.relation    = relation;
        }

        @Override
        public void build(StringBuilder sqlBuilder, JoinContext context) {
            RelationMapping relation = this.relation;

            if(relation.isManyToOne() || relation.isOneToMany()) {

                if(this.type == JoinType.LEFT) {
                    sqlBuilder.append(" left");
                }

                sqlBuilder.append(" join ")
                        .append(this.target.getTableName())
                        .append(" ")
                        .append(this.alias)
                        .append(" on ");

                //many-to-one
                if(relation.isManyToOne()) {
                    int i=0;
                    for(JoinFieldMapping jf : relation.getJoinFields()) {
                        if(i>0) {
                            sqlBuilder.append(" and ");
                        }
                        sqlBuilder.append(context.getSourceAlias()).append('.').append(jf.getLocalFieldName())
                                .append("=")
                                .append(this.alias).append('.').append(jf.getReferencedFieldName());
                        i++;
                    }
                    return;
                }

                //one-to-many, find the inverse many-to-one relation.
                if(relation.isOneToMany()) {
                    RelationMapping inverse =
                            this.target.getRelationMapping(relation.getInverseRelationName());

                    if(null == inverse || !inverse.isManyToOne()) {
                        throw new IllegalStateException("A inverse many-to-one relation must be exists in entity '" +
                                this.target.getEntityName() + "'");
                    }

                    int i=0;
                    for(JoinFieldMapping jf : inverse.getJoinFields()) {
                        if(i>0) {
                            sqlBuilder.append(" and ");
                        }
                        sqlBuilder.append(context.getSourceAlias()).append('.').append(jf.getReferencedFieldName())
                                .append("=")
                                .append(this.alias).append('.').append(jf.getLocalFieldName());
                        i++;
                    }

                    return;
                }

            }

            //many-to-many, find the join entity.
            if(relation.isManyToMany()) {
                EntityMapping joinEntity = context.getOrm().getMetadata().getEntityMapping(relation.getJoinEntityName());

                RelationMapping rjoin   = joinEntity.tryGetKeyRelationMappingOfTargetEntity(context.getSource().getEntityName());
                RelationMapping rtarget = joinEntity.tryGetKeyRelationMappingOfTargetEntity(relation.getTargetEntityName());

                if(null == rjoin) {
                    rjoin = joinEntity.tryGetRefRelationMappingOfTargetEntity(context.getSource().getEntityName());
                }

                if(null == rtarget) {
                    rtarget = joinEntity.tryGetRefRelationMappingOfTargetEntity(relation.getTargetEntityName());
                }

                String joinEntityAlias = context.getSourceAlias() + "_" + this.alias;

                if(this.type == JoinType.LEFT) {
                    sqlBuilder.append(" left");
                }

                sqlBuilder.append(" join ")
                        .append(joinEntity.getEntityName())
                        .append(" ")
                        .append(joinEntityAlias)
                        .append(" on ");

                int i = 0;
                for(JoinFieldMapping jf : rjoin.getJoinFields()) {
                    if(i > 0) {
                        sqlBuilder.append(" and ");
                    }
                    sqlBuilder.append(context.getSourceAlias()).append('.').append(jf.getReferencedFieldName())
                            .append('=')
                            .append(joinEntityAlias).append('.').append(jf.getLocalFieldName());
                    i++;
                }

                if(this.type == JoinType.LEFT) {
                    sqlBuilder.append(" left");
                }

                sqlBuilder.append(" join ")
                        .append(this.target.getEntityName())
                        .append(" ")
                        .append(this.alias)
                        .append(" on ");

                i=0;
                for(JoinFieldMapping jf : rtarget.getJoinFields()) {
                    if(i > 0) {
                        sqlBuilder.append(" and ");
                    }
                    sqlBuilder.append(this.alias).append('.').append(jf.getReferencedFieldName())
                            .append('=')
                            .append(joinEntityAlias).append('.').append(jf.getLocalFieldName());
                    i++;
                }

                return;
            }

            throw new IllegalStateException("Cannot join entity '" + this.target.getEntityName() + "' by relation type '" +
                    relation.getType());

        }
    }

	protected class SqlBuilder {
		protected String   alias = "t";
		protected String[] selects;

		private StringBuilder sql;
		
		protected SqlBuilder(){
		}
		
		public String buildDeleteSql() {
			sql = new StringBuilder();
			
			delete().from().join().where();
			
			return sql.toString();
		}

        public String buildSecondaryDeleteSql(boolean secondary) {
            if(!joins.isEmpty()) {
                throw new IllegalStateException("Delete by query with secondary table does not not support joins");
            }

            sql = new StringBuilder();

            checkAndResolveSecondaryWhere(true);

            if(db().isMySql()) {
                buildMySqlSecondaryDeleteSql(secondary);
            }else {
                //delete from primary_table | secondary_table where id in ( select id from (
                //  select t1.*,t2.col1,t2.col2,... from primary_table t1 left join secondary_table t2 on t1.id = t2.id
                //) t where ...
                delete().from(secondary).idInWhere();
            }

            return sql.toString();
        }

        private void checkAndResolveSecondaryWhere(boolean check) {
            if(!Strings.isEmpty(where)) {
                SqlWhereExpr expr = SqlParser.parseWhereExpr(where);

                expr.traverse((node) -> {

                    if(node instanceof SqlObjectName) {
                        String lastName = ((SqlObjectName) node).getLastName();

                        FieldMapping fm = em.tryGetFieldMapping(lastName);

                        if(null != fm) {
                            if(check && fm.isSecondary()) {
                                throw new IllegalStateException("Can't use secondary field '" + lastName + "' at where expression");
                            }

                            ((SqlObjectName) node).setLastName(fm.getColumnName());
                        }
                    }

                    return true;
                });

                where = expr.toString();
            }
        }

        private void buildMySqlSecondaryDeleteSql(boolean secondary) {
            sql.append("delete t1 from ");

            if(secondary) {
                sql.append(em.getSecondaryTableName()).append(" t1 join ").append(em.getTableName()).append(" t2");
            }else{
                sql.append(em.getTableName()).append(" t1 left join ").append(em.getSecondaryTableName()).append(" t2");
            }

            sql.append(" on t1.").append(em.idColumnName()).append("=t2.").append(em.idColumnName());

            where();
        }

        public String buildSecondaryUpdateSql(Map<String, Object> fields, Map<String,Object> params, boolean secondary) {
            if(!joins.isEmpty()) {
                throw new IllegalStateException("Update by query with secondary table does not not support joins");
            }

            sql = new StringBuilder();

            checkAndResolveSecondaryWhere(false);

            if(db().isMySql()) {
                if(!buildMySqlSecondaryUpdateSql(fields, params, secondary)){
                    return null;
                }
            }else{
                //update primary_table | secondary_table set ... where id in ( select id from (
                //  select t1.*,t2.col1,t2.col2,... from primary_table t1 left join secondary_table t2 on t1.id = t2.id
                //) t where ...
                if(!updateSetColumns(fields, params, secondary)){
                    return null;
                }else{
                    idInWhere();
                }
            }

            return sql.toString();
        }

        private boolean buildMySqlSecondaryUpdateSql(Map<String, Object> fields, Map<String,Object> params, boolean secondary) {
            sql.append("update ");

            if(secondary) {
                sql.append(em.getSecondaryTableName()).append(" t1 join ").append(em.getTableName()).append(" t2");
            }else{
                sql.append(em.getTableName()).append(" t1 left join ").append(em.getSecondaryTableName()).append(" t2");
            }

            sql.append(" on t1.").append(em.idColumnName()).append("=t2.").append(em.idColumnName());

            if(!setColumns(fields, params, false, secondary)) {
                return false;
            }

            where();
            return true;
        }

        protected SqlBuilder idInWhere() {
            String idColumn = em.idColumnName();

            sql.append(" where ").append(idColumn).append(" in (select ").append(idColumn).append(" from ");

            sql.append("(select t1.*");

            for(FieldMapping fm : em.getFieldMappings()) {
                if(fm.isSecondary()) {
                    sql.append(",t2.").append(fm.getColumnName());
                }
            }

            sql.append(" from ").append(em.getTableName()).append(" t1 left join ")
                .append(em.getSecondaryTableName()).append(" t2 on t1.").append(idColumn).append(" = ").append("t2.").append(idColumn)
                .append(") ").append(alias);

            where();

            sql.append(")");
            return this;
        }
		
		public String buildSelectSql() {
			sql = new StringBuilder();
			
			select().columns().from().join().where().groupBy().orderBy();
			
			return sql.toString();
		}
		
		public String buildCountSql() {
			sql = new StringBuilder();

            if(distinct || hasGroupBy()) {
                sql.append("select count(*) from ( ");
                select().columns().from().join().where().groupBy();
                sql.append(" ) cnt");
            }else{
                select().count().from().join().where().groupBy();
            }
			
			return sql.toString();
		}
		
		public String buildUpdateSql(Map<String, Object> fields, Map<String,Object> params) {
			sql = new StringBuilder();
			
			if(updateSetColumns(fields, params, false)){
                where();
            }else{
                return null;
            }

			return sql.toString();
		}
		
		protected SqlBuilder delete() {
			sql.append("delete");
			
			//MySQL:
			/*
				If you declare an alias for a table, you must use the alias when referring to the table:
				DELETE t1 FROM test AS t1, test2 WHERE ...
			 */
            if(context.getDb().getDialect().useTableAliasAfterDelete()) {
                sql.append(" ").append(alias);
            }

			return this;
		}
		
		protected SqlBuilder select() {
			sql.append("select");
            if(distinct) {
                sql.append(" distinct");
            }
			return this;
		}
		
		protected SqlBuilder count() {
			sql.append(" count(*)");
			return this;
		}
		
		protected boolean updateSetColumns(Map<String, Object> fields, Map<String,Object> params, boolean secondary) {
            DbDialect dialect = context.getDb().getDialect();

            sql.append("update ");

            if(dialect.useTableAliasAfterUpdate()) {
                //update alias set ... from table alias
                sql.append(alias);
            }else{
                sql.append(secondary ? em.getSecondaryTableName() : em.getEntityName()).append(" ").append(alias);
            }

            if(!setColumns(fields, params, true, secondary)) {
                return false;
            }

            if(dialect.useTableAliasAfterUpdate()) {
                sql.append(" from ").append(secondary ? em.getSecondaryTableName() : em.getEntityName()).append(" ").append(alias);
            }

			return true;
		}

        private boolean setColumns(Map<String, Object> fields, Map<String,Object> params, boolean useAlias, boolean secondary) {
            DbDialect dialect = context.getDb().getDialect();

            sql.append(" set ");

            int index = 0;
            for(Entry<String, Object> entry : fields.entrySet()){
                String field = entry.getKey();
                Object value = entry.getValue();

                FieldMapping fm = em.getFieldMapping(field);
                if(!fm.matchSecondary(secondary)) {
                    continue;
                }

                String param = "new_" + field;

                if(index > 0){
                    sql.append(",");
                }

                if(useAlias && !dialect.useTableAliasAfterUpdate()) {
                    sql.append(alias).append('.');
                }

                sql.append(fm.getColumnName()).append("=").append(':').append(param);

                params.put(param, value);

                index++;
            }

            return index > 0;
        }
		
		protected SqlBuilder columns() {
			sql.append(' ');
			
            if(null == selects || selects.length == 0){
            	if(null == selectFilter) {
                    SqlFactory sf = dao.getOrmContext().getSqlFactory();
                    sql.append(sf.createSelectColumns(dao.getOrmContext(), em, alias));
            	}else{
            		int index = 0;
            		for(FieldMapping fm : em.getFieldMappings()) {
            			if(selectFilter.test(fm)) {
                			if(index > 0) {
                				sql.append(',');
                			}
                			
                			sql.append(alias).append('.').append(fm.getColumnName());
                			index++;
            			}
            		}
            	}
                
            }else{
                int index = 0;
                for(String column : selects){
                    if(index > 0){
                        sql.append(",");
                    }
                    
                    if(column.contains(".") || column.contains(" ") || column.contains("(")){
                        sql.append(column);
                    }else {
                        sql.append(alias).append(".").append(column);
                    }
                    
                    index++;
                }
            } 
            
            return this;
		}
		
		protected SqlBuilder from() {
	        sql.append(" from ");
            if(!Strings.isEmpty(sqlView)) {
                sql.append("(").append(sqlView).append(")");
            }else {
                sql.append(em.getEntityName());
            }
            sql.append(" ").append(alias);
	        return this;
		}

        protected SqlBuilder from(boolean secondary) {
            sql.append(" from ").append(secondary ? em.getSecondaryTableName() : em.getTableName()).append(" ").append(alias);
            return this;
        }

        protected SqlBuilder join() {
            for(JoinBuilder join : joins) {
                JoinContext joinContext = new JoinContext() {
                    @Override
                    public EntityMapping getSource() {
                        return em;
                    }

                    @Override
                    public OrmContext getOrm() {
                        return context;
                    }

                    @Override
                    public String getSourceAlias() {
                        return alias;
                    }

                    @Override
                    public List<JoinBuilder> getAllJoins() {
                        return joins;
                    }
                };
                join.build(sql,joinContext);
            }
            return this;
        }
		
		protected SqlBuilder where() {
	        if(!Strings.isEmpty(where)){
	        	where = where.trim();
	        	if(!Strings.startsWithIgnoreCase(where,"where")){
	        		sql.append(" where ").append(where);
	        	}else{
	        		sql.append(" ").append(where);
	        	}
	        }

            if(null != joinByIdWhere) {

                if(Strings.isEmpty(where)) {
                    sql.append(" where ").append(joinByIdWhere);
                }else{
                    sql.append(" and ( ");
                    sql.append(joinByIdWhere);
                    sql.append(" )");
                }

            }

	        return this;
		}

        protected boolean hasGroupBy() {
            return !Strings.isEmpty(groupBy);
        }
		
		protected SqlBuilder groupBy() {
	        if(!Strings.isEmpty(groupBy)){
	        	sql.append(" group by ").append(groupBy);
	        }
	        
	        if(!Strings.isEmpty(having)){
	        	sql.append(" having ").append(having);
	        }
	        
	        return this;
		}
		
		protected SqlBuilder orderBy() {
	        if(!Strings.isEmpty(orderBy)){
	        	sql.append(" order by ").append(orderBy);
	        }
	        return this;
		}
	}
}