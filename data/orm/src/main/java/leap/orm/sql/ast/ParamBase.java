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

import leap.lang.Enumerable;
import leap.lang.Enumerables;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.jdbc.JDBC;
import leap.lang.params.Params;
import leap.orm.sql.Sql.Scope;
import leap.orm.sql.SqlStatementBuilder;

import java.io.IOException;

public abstract class ParamBase extends AstNode {
	
	@Override
	protected void buildStatement_(SqlStatementBuilder stm, Params params) throws IOException {
		stm.increaseAndGetParameterIndex();
		
		Object p = eval(stm,params);
		
		if(isReplace()){
			addReplacedText(stm, params, p);
		}else{
			addPlaceholderParameter(stm, params, p);
		}
    }

    /**
     * Evaluates the param value.
     */
	public Object eval(SqlStatementBuilder stm, Params params){
		if(params.isIndexed()){
			return params.get(stm.currentParameterIndex());
		}else{
			return getParameterValue(stm, params);
		}
	}
	
	public boolean isReplace(){
		return false;
	}
	
	public Scope getScope(){
		throw new IllegalStateException("No scope");
	}
	
	protected void addReplacedText(SqlStatementBuilder stm,Params params, Object p) throws IOException {
		Object value  = eval(stm,params);
		String string = Converts.toString(value);
		if(!Strings.isEmpty(string)){
			Scope scope = getScope();
		
			if(scope == Scope.STRING){
				string = Strings.replace(string, "'", "''");
			}
			
			stm.append(string);
		}	
	}
	
	protected void addPlaceholderParameter(SqlStatementBuilder stm,Params params, Object value) throws IOException {
		/*
		if(!stm.isQuery()){
			stm.append(JDBC.PARAMETER_PLACEHOLDER_CHAR);
			stm.addParameter(value);
			return;
		}
		*/
		
		//TODO : optimize performande
		if(stm.isLastInOperator()){
			stm.append('(');
			
			if(null == value){
				stm.append(JDBC.PARAMETER_PLACEHOLDER_CHAR);
				stm.addParameter(null);
			}else{
				Enumerable<Object> c = Enumerables.of(value);
				if(c.isEmpty()) {
					stm.append(JDBC.PARAMETER_PLACEHOLDER_CHAR);
					stm.addParameter(null);
				}else{
					int i=0;
					for(Object item : c){
						if(null != item && !"".equals(item)){
	    					if(i > 0){
	    						stm.append(',');
	    					}
	    					stm.append(JDBC.PARAMETER_PLACEHOLDER_CHAR);
	    					stm.addParameter(item);
	    					
	    					i++;
						}
					}
				}
			}
			
			stm.append(')');
		}else if(null == value && stm.removeLastEqualsOperator()){
			stm.append(" is null");
		}else{
			stm.append(JDBC.PARAMETER_PLACEHOLDER_CHAR);
			stm.addParameter(value);
		}
	}
	
	protected abstract Object getParameterValue(SqlStatementBuilder stm,Params params);
}