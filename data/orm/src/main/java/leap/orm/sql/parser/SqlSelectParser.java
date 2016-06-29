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
import leap.orm.sql.ast.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Most of codes inspire or copy from com.alibaba.druid.sql.parser.SqlSelectParser, see https://github.com/alibaba/druid
 */
class SqlSelectParser extends SqlQueryParser {
	
	protected static final Set<Token>  SELECT_ITEM_KEYWORDS = new HashSet<Token>();
	
	static {
		SELECT_ITEM_KEYWORDS.add(Token.ORDER);
		SELECT_ITEM_KEYWORDS.add(Token.GROUP);
		SELECT_ITEM_KEYWORDS.add(Token.LIMIT);
	}

	public SqlSelectParser(SqlParser parent) {
	    super(parent);
    }

	protected SqlSelect parseSelectBody(){
		if(lexer.token() == Token.LPAREN){
			acceptText();
			SqlSelect select = parseSelectBody();
			expect(Token.RPAREN).acceptText();
			return select;
		}
		
		SqlSelect select = new SqlSelect();

		suspendNodes();
		
		expect(Token.SELECT).acceptText();
		
		//SELECT (ALL | DISTINCT)? (TOP Integer (PERCENT))? selectList
		parseDistinct(select);
		parseTop(select);
		parseSelectList(select);
		
		if(parseFrom(select)){
			parseJoins(select);
			parseWhere(select);
		}

		parseQueryBodyRest(select);
		
		select.setNodes(nodes());
		restoreNodes().addNode(select);
		return select;
	}
	
	protected void parseDistinct(SqlSelect select){
		if(lexer.token() == Token.DISTINCT){
			acceptText();
			select.setDistinct(true);
			return;
		}
		
		if(lexer.token() == Token.ALL){
			acceptText();
			return;
		}
	}
	
	protected void parseTop(SqlSelect select){
		if(lexer.token() == Token.TOP){
			SqlTop top = new SqlTop(lexer.tokenText());
			
			expectNextToken(Token.LITERAL_INT);
			top.setNumber(lexer.intValue());
			
			select.setTop(top);
			acceptNode(top);
			
			if(lexer.token() == Token.PERCENT){
				top.setPercent(true);
				nextToken();
			}
		}
	}
	
	protected void parseSelectList(SqlSelect select){
		createSavePoint();
		
		pushScope(Scope.SELECT_LIST);
		
		parseSelectItem(select);
		
		if(lexer.token() == Token.COMMA){
			do{
				acceptText();
				parseSelectItem(select);
			}while(lexer.token() == Token.COMMA);
		}
		
		popScope();
		
		SqlSelectList list = new SqlSelectList(removeSavePoint());
		select.setSelectList(list);
		addNode(list);
	}
	
	protected void parseJoins(SqlQuery query) {
		for(;;) {
			if(lexer.isEOS()) {
				return;
			}
			
			if(lexer.token() == Token.WHERE) {
				return;
			}
			
			boolean join = true;
			
			switch (lexer.token()) {
				case COMMA:
				case JOIN:
					acceptText();
					break;
				case LEFT:
				case RIGHT:
					acceptText();
					if(lexer.token() == Token.OUTER){
						acceptText();
					}
					expect(Token.JOIN).acceptText();
					break;
				case FULL:
					acceptText();
					if(lexer.token() == Token.OUTER){
						acceptText();
					}
					expect(Token.JOIN).acceptText();
					break;
				case INNER:
					acceptText();
					expect(Token.JOIN).acceptText();
					break;
				default:
					if(lexer.isIdentifier("STRAIGHT_JOIN") || lexer.isIdentifier("CROSS")){
						acceptText();
						break;
					}
					join = false;
					break;
			}
			
			if(join){
				parseJoin(query);
			}else{
				return;
			}
		}
	}
	
	protected void parseJoin(SqlQuery query) {
		parseTableSource(query);
		
		if(lexer.token() == Token.ON) {
			//Accepts 'ON'
			acceptText();
			
			parseJoinOnExpr(query);
		}
	}
	
	protected void parseJoinOnExpr(SqlQuery query){
		new SqlExprParser(this).parseExpr();
		
		if(lexer.token() == Token.AND || lexer.token() == Token.OR) {
			acceptText();
			
			if(lexer.token() == Token.EXISTS) {
				parseExists(query, new AtomicInteger());
			}else{
				parseJoinOnExpr(query);
			}
		}
	}
	
	protected void parseSelectItem(SqlSelect select){

		if(lexer.token() == Token.RPAREN){
			return;
		}

		if(lexer.token() == Token.STAR){
			acceptNode(new SqlAllColumns());
			return;
		}
		
		if(parseSpecialToken()){
			return;
		}
		
		if(lexer.token() == Token.LPAREN){
			acceptText();
 			while(lexer.token() != Token.RPAREN && lexer.token() != Token.FROM){
				//select item : subquery
				if(lexer.token() == Token.SELECT){
					parseSelect();
				}else{
					parseSelectItem(select);
				}
			}
			expect(Token.RPAREN).acceptText();
		}else if(lexer.isIdentifier() || (lexer.isKeyword() && SELECT_ITEM_KEYWORDS.contains(lexer.token()))){
			if(lexer.peekCharSkipWhitespaces() == '('){
				acceptText();
				expect(Token.LPAREN).acceptText();
				parseRestForClosingParen();
				expect(Token.RPAREN).acceptText();
				
				if(lexer.token().isKeywordOrIdentifier() && lexer.peekCharSkipWhitespaces() == '(') {
					acceptText();
					expect(Token.LPAREN).acceptText();
					parseRestForClosingParen();
					expect(Token.RPAREN).acceptText();
				}
			}else{
				parseSqlObjectName();
			}
		}else{
			new SqlExprParser(this).parseExpr();
		}
		
		parseSelectItemAlias(select);
	}
	
	protected String parseSelectItemAlias(SqlSelect select){
		if(lexer.token() == Token.AS){
			acceptText();
			
			expects(Token.IDENTIFIER,Token.QUOTED_IDENTIFIER,Token.LITERAL_CHARS);
			
			String alias = lexer.tokenText();
			
			select.addSelectItemAlias(alias);
			
			acceptText();
			
			return alias;
		}
		
		if((lexer.token().isKeywordOrIdentifier() && lexer.token() != Token.FROM) || 
		   (lexer.token() == Token.LITERAL_CHARS)){
			
			String alias = lexer.tokenText();
			
			select.addSelectItemAlias(alias);
			
			acceptText();
			
			return alias;
		}
		
		return null;
	}
	
}