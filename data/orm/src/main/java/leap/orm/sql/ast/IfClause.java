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
import java.util.Objects;

import leap.lang.Args;
import leap.lang.Objects2;
import leap.lang.params.Params;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlStatementBuilder;
import leap.orm.sql.parser.Token;

public class IfClause extends DynamicNode {
	
	private static final String END_IF = Token.AT_ENDIF.literal().toLowerCase();
	
	private final IfStatement[] ifStatements;
	private final ElseStatement elseStatement;
	
	public IfClause(IfStatement[] ifStatements, ElseStatement elseStatement){
		Args.notEmpty(ifStatements);
		this.ifStatements  = ifStatements;
		this.elseStatement = elseStatement;
	}

	public IfStatement[] getIfStatements() {
		return ifStatements;
	}

	public ElseStatement getElseStatement() {
		return elseStatement;
	}

	@Override
	protected void toString_(Appendable buf) throws IOException {
		for(int i=0;i<ifStatements.length;i++){
			ifStatements[i].toString(buf);
		}
		
		if(null != elseStatement){
			elseStatement.toString(buf);
		}
		
		buf.append(END_IF);	    
    }

	@Override
    protected void buildStatement_(SqlContext context, SqlStatementBuilder stm, Params params) throws IOException {
		boolean condition = false;
		for(IfStatement ifStatement : this.getIfStatements()){
			Object obj = ifStatement.getCondition().eval(stm,params);
			if(Objects.equals(Boolean.TRUE,obj)){
				ifStatement.buildStatement(context, stm, params);
				condition = true;
			}
		}
		if(!condition){
			if(this.getElseStatement() != null){
				this.getElseStatement().buildStatement(context, stm, params);
			}
		}
    }

}