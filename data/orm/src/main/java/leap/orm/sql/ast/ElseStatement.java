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
import leap.lang.params.Params;
import leap.orm.sql.Sql;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlStatementBuilder;

public class ElseStatement extends DynamicNode {
	
	private final AstNode[] bodyNodes;
	
	public ElseStatement(AstNode[] bodyNodes){
		Args.notEmpty(bodyNodes);
		this.bodyNodes = bodyNodes;
	}
	
	public AstNode[] getBodyNodes() {
		return bodyNodes;
	}
	
	@Override
    protected void toString_(Appendable buf) throws IOException {
		buf.append("@else");
		for(AstNode bodyNode : bodyNodes){
			bodyNode.toString(buf);
		}	    	    
    }

	@Override
    protected void buildStatement_(SqlContext context, Sql sql, SqlStatementBuilder stm, Params params) throws IOException {
		for(AstNode node : this.getBodyNodes()){
			node.buildStatement(context, sql, stm,params);
		}
    }
}
