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
package leap.orm.sql.parser;

import leap.orm.sql.Sql.Scope;
import leap.orm.sql.ast.SqlOrderBy;

class SqlOrderByParser extends SqlExprParser {

	public SqlOrderByParser(SqlParser parent) {
		super(parent);
	}
	
	public SqlOrderBy orderBy(){
		if(lexer.token() == Token.ORDER){
			acceptText();
			
			if(lexer.token() == Token.BY){
				pushScope(Scope.ORDER_BY);
				
				acceptText();
				parseOrderByItem();
				
				while(lexer.token() == Token.COMMA){
					acceptText();
					parseOrderByItem();	
				}
				
				popScope();
				return new SqlOrderBy(nodes());
			}
		}
		
		return null;
	}
	
	protected void parseOrderByItem(){
		/* SQL:2003
		<sort specification list>   ::=   <sort specification> [ { <comma> <sort specification> }... ]
		<sort specification>    	::=   <sort key> [ <ordering specification> ] [ <null ordering> ]
		<sort key>    				::=   <value expression>
		<ordering specification>    ::=   ASC | DESC
		<null ordering>    			::=   NULLS FIRST | NULLS LAST				 
		 */	
		
		if(lexer.token() == Token.COMMA){
			return;
		}
		
		if(lexer.isEOS()){
			return;
		}
		
		if(!parseOrderByAscOrDesc()){
			
			if(lexer.isIdentifier()) {
				
				createSavePoint();
				
				parseSqlObjectName();
				
				//order by sub_str(a,b) -> sub_str is identifier
				if(lexer.token() == Token.LPAREN){
					restoreSavePoint();
					parseExpr();
				}else{
					acceptSavePoint();	
				}
			}else{
				parseExpr();	
			}
			
			parseOrderByAscOrDesc();
		}
	}
	
	protected boolean parseOrderByAscOrDesc(){
		if(lexer.token() == Token.ASC || lexer.token() == Token.DESC){
			acceptText();
			
			createSavePoint();
			
			if(lexer.isIdentifier("NULLS")){
				acceptText();
				
				if(lexer.isIdentifier("FIRST") || lexer.isIdentifier("LAST")){
					acceptText();
					acceptSavePoint();
					return true;
				}
			}
			
			restoreSavePoint();
			return true;
		}
		return false;
	}
}
