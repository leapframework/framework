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
package leap.orm.sql.ast;

import leap.core.el.EL;
import leap.db.Db;
import leap.db.DbDialect;
import leap.lang.params.Params;
import leap.orm.OrmContext;
import leap.orm.dmo.Dmo;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.sql.Sql;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlStatementBuilder;

import java.io.IOException;

public class SqlTableName extends SqlObjectNameBase implements SqlTableSource {

	private String        alias;
    private boolean       join;
	private EntityMapping em;

	public SqlTableName() {
		
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

    @Override
    public boolean isJoin() {
        return join;
    }

    public void setJoin(boolean join) {
        this.join = join;
    }

    public boolean hasEntityMapping(){
        return null != em;
    }

    public EntityMapping getEntityMapping() {
		return em;
	}

	public void setEntityMapping(EntityMapping em) {
		this.em = em;
	}

    public boolean isEntityName() {
        return null != em && em.getEntityName().equalsIgnoreCase(lastName);
    }

    @Override
    protected void buildStatement_(SqlContext context, Sql sql, SqlStatementBuilder stm, Params params) throws IOException {
        if(null != em){
            if(sql.isSelect() && em.hasSecondaryTable()) {
                if(isEntityName()) {
                    buildSecondaryTableStatement(context, sql, stm, params);
                    return;
                }
            }

            if(null != em.getDynamicTableName()) {
                toSql_(stm, em.getDynamicTableName(), params, context.getOrmContext());
                return;
            }
        }

        super.buildStatement_(context, sql, stm, params);
    }

    @Override
    protected void toSql_(Appendable out) throws IOException {
        toSql_(out, null, null, null);
    }

    protected void toSql_(Appendable out, String dynamicLastName, Params params, OrmContext context) throws IOException{
        if(null != firstName){
            out.append(firstName).append('.');
        }

        if(null != secondaryName){
            out.append(secondaryName).append('.');
        }

        if(null != dynamicLastName) {
            dynamicLastName = EL.createCompositeExpression(dynamicLastName).getValue(params.map()).toString();
            out.append(dynamicLastName);

            Db db = context.getDb();

            if(!db.checkTableExists(dynamicLastName)) {
                Dmo dmo = Dmo.get(context.getName());
                dmo.cmdCreateTable(em).changeTableName(dynamicLastName).execute();
            }
        }else if(null != em){
            out.append(em.getTableName());
        }else{
            out.append(lastName);
        }
    }

    @Override
    protected void appendLastName(Appendable buf, DbDialect dialect) throws IOException {
        if(null != em) {
            buf.append(dialect.quoteIdentifier(em.getTableName(), true));
        }else{
            buf.append(dialect.quoteIdentifier(lastName, true));    
        }
    }

    protected void buildSecondaryTableStatement(SqlContext context, Sql sql, SqlStatementBuilder stm, Params params) throws IOException {
        DbDialect dialect = context.getOrmContext().getDb().getDialect();

        StringBuilder s = new StringBuilder();

        s.append("( select ");

        int index=0;
        for(String col : em.getKeyColumnNames()) {
            if(index > 0) {
                s.append(',');
            }
            s.append("t1.").append(dialect.quoteIdentifier(col));
            index++;
        }

        for(FieldMapping fm : em.getFieldMappings()) {
            if(!fm.isPrimaryKey()) {
                s.append(',').append(dialect.quoteIdentifier(fm.getColumnName()));
            }
        }

        s.append(" from ")
                .append(em.getTableName()).append(" t1")
                .append(" left join ")
                .append(em.getSecondaryTableName()).append(" t2")
                .append(" on ");


        index=0;
        for(String col : em.getKeyColumnNames()) {
            String quotedColumn = dialect.quoteIdentifier(col);

            if(index > 0) {
                s.append(',');
            }
            s.append("t1.").append(quotedColumn).append("=").append("t2.").append(quotedColumn);
            index++;
        }

        s.append(" )");

        stm.append(s);
    }

}