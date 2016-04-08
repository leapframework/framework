/*
 * Copyright 2014 the original author or authors.
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

import leap.orm.sql.ast.SqlQuery;
import leap.orm.sql.ast.SqlTableName;
import leap.orm.sql.ast.SqlUpdate;


public class SqlUpdateParser extends SqlQueryParser {

	public SqlUpdateParser(SqlParser parent) {
		super(parent);
	}
	
	public void parseUpdateBody() {
		SqlUpdate update = new SqlUpdate();

		suspendNodes();
		
		expectAndAcceptText(Token.UPDATE);
		
		//parse table source
		parseTableSource(update);
		
		//parse set
		expectAndAcceptText(Token.SET);
		
		//parse update columns
		parseUpdateColumns(update);
		
		//parse where
		if(parseWhere(update)) {
			parseQueryBodyRest(update);
		}else{
			parseRest();	
		}
		
		update.setNodes(nodes());
		restoreNodes().addNode(update);
	}
	
	protected void parseTableSource(SqlQuery query) {
		parseTableName();
		
		if(node instanceof SqlTableName){
			SqlTableName table = (SqlTableName)node;
			table.setAlias(parseTableAlias());
			query.addTableSource(table);
		}
	}
	
	protected String parseTableAlias(){
		if(lexer.token() == Token.AS){
			acceptText();
			
			expect(Token.IDENTIFIER);
			String alias = lexer.tokenText();
			acceptText();
			return alias;
		}

		final Token token = lexer.token();
		
		if(token.isKeywordOrIdentifier() && token != Token.SET){
			String alias = lexer.tokenText();
			acceptText();
			return alias;
		}
		
		return null;
	}
	
	protected void parseUpdateColumns(SqlUpdate update) {
		parseUpdateColumn(update);
		
		if(lexer.token() == Token.COMMA){
			do{
				acceptText();
				parseUpdateColumn(update);
			}while(lexer.token() == Token.COMMA);
		}
	}
	
	protected void parseUpdateColumn(SqlUpdate update){
		if(parseSpecialToken()){
			return;
		}

		if(parseSqlObjectName()) {
			expectAndAcceptText(Token.EQ);
			parseUpdateValue(update);
		}
	}
	
	protected void parseUpdateValue(SqlUpdate update) {
		if(lexer.token() == Token.LPAREN){
			acceptText();
			
			//select item : subquery
			if(lexer.token() == Token.SELECT){
				parseSelect();
			}else{
				parseUpdateValue(update);
			}
			expectAndAcceptText(Token.RPAREN);
		}else if(lexer.isIdentifier() || lexer.isKeyword()){
			if(lexer.peekCharSkipWhitespaces() == '('){
				acceptText();
				expectAndAcceptText(Token.LPAREN);
				parseRestForClosingParen();
				expectAndAcceptText(Token.RPAREN);
				
				if(lexer.token().isKeywordOrIdentifier() && lexer.peekCharSkipWhitespaces() == '(') {
					acceptText();
					expectAndAcceptText(Token.LPAREN);
					parseRestForClosingParen();
					expectAndAcceptText(Token.RPAREN);
				}
			}else{
				parseSqlObjectName();
			}
		}else{
			new SqlExprParser(this).parseExpr();
		}
	}
}