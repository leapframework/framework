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
import leap.orm.sql.Sql;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlStatementBuilder;

import java.io.IOException;

public class SqlTableName extends SqlObjectNameBase implements SqlTableSource {

	private String 		  alias;
    private boolean       join;
	private EntityMapping entityMapping;
    private boolean       secondary;
	
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

    public EntityMapping getEntityMapping() {
		return entityMapping;
	}

	public void setEntityMapping(EntityMapping em) {
		this.entityMapping = em;
        if(null != em && em.hasSecondaryTable()) {
            if(em.getSecondaryTableName().equalsIgnoreCase(this.lastName)) {
                this.secondary = true;
            }
        }
	}

    public boolean isEntity(){
		return null != entityMapping;
	}

    public boolean isSecondary() {
        return secondary;
    }

    @Override
    protected void buildStatement_(SqlContext context, Sql sql, SqlStatementBuilder stm, Params params) throws IOException {
        if(null != entityMapping && null != entityMapping.getDynamicTableName()) {
            toSql_(stm, entityMapping.getDynamicTableName(), params, context.getOrmContext());
        }else{
            super.buildStatement_(context, sql, stm, params);
        }
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
                dmo.cmdCreateTable(entityMapping).changeTableName(dynamicLastName).execute();
            }
        }else if(null != entityMapping){
            out.append(entityMapping.getTableName());
        }else{
            out.append(lastName);
        }
    }

    @Override
    protected void appendLastName(Appendable buf, DbDialect dialect) throws IOException {
        if(null != entityMapping) {
            buf.append(dialect.quoteIdentifier(entityMapping.getTableName(), true));
        }else{
            buf.append(dialect.quoteIdentifier(lastName, true));    
        }
    }
}