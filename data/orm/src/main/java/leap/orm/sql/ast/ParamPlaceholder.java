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

import java.io.IOException;

import leap.lang.Args;
import leap.lang.jdbc.JDBC;
import leap.lang.params.Params;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.sql.NamedSqlParameter;
import leap.orm.sql.PreparedBatchSqlStatementBuilder;
import leap.orm.sql.Sql;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlStatementBuilder;
import leap.orm.sql.parser.Token;

public class ParamPlaceholder extends NamedParamNode {
	
	private final Token  token;
	
	public ParamPlaceholder(Token token,String name) {
		super(name);
		Args.notNull(token);
	    this.token = token;
    }
	
	protected Object param(SqlStatementBuilder stm, Params params){
		if(params.isIndexed()) {
			if(params.contains(name)) {
				return params.get(name);
			}else{
				return params.get(stm.currentParameterIndex());
			}
		}else{
			return getParameterValue(stm, params);
		}
	}
	
	@Override
    protected void prepareBatchStatement_(SqlContext context, PreparedBatchSqlStatementBuilder stm) throws IOException {
		stm.append(JDBC.PARAMETER_PLACEHOLDER_CHAR);
		
		int index = stm.increaseAndGetParameterIndex();

		Sql sql = stm.sql();
		
		if(sql.isInsert() || sql.isUpdate()) {
			if(null != context.getPrimaryEntityMapping()) {
				EntityMapping em = context.getPrimaryEntityMapping();
				FieldMapping  fm = em.tryGetFieldMapping(name);
				
				if(null != fm){
					if(sql.isInsert() && null != fm.getInsertValue()) {
						stm.addBatchParameter(new NamedSqlParameter(index, name, fm.getInsertValue()));
						return;
					}
					
					if(sql.isUpdate() && null != fm.getUpdateValue()) {
						stm.addBatchParameter(new NamedSqlParameter(index, name, fm.getUpdateValue()));
						return;
					}
					
					if(sql.isInsert() && null != fm.getDefaultValue()) {
						stm.addBatchParameter(new NamedSqlParameter(index, name, fm.getDefaultValue()));
						return;
					}
				}
			}
		}
		
		stm.addBatchParameter(new NamedSqlParameter(index,name));
    }

	@Override
	protected void toString_(Appendable buf) throws IOException {
		if(token == Token.COLON_PLACEHOLDER){
			buf.append(':').append(name);
		}else if(token == Token.SHARP_PLACEHOLDER){
			buf.append('#').append(name).append('#');
		}else{
			throw new IllegalStateException("Unsupported placeholder token '" + token.name() + "'");	
		}
    }
	
	@Override
    protected Object getParameterValue(SqlStatementBuilder stm, Params params) {
	    return params.get(name);
    }
}