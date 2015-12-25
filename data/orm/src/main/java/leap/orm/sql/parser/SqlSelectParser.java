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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import leap.orm.sql.Sql.Scope;
import leap.orm.sql.ast.SqlAllColumns;
import leap.orm.sql.ast.SqlQuery;
import leap.orm.sql.ast.SqlSelect;
import leap.orm.sql.ast.SqlSelectList;
import leap.orm.sql.ast.SqlTop;


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
			accept();
			SqlSelect select = parseSelectBody();
			accept(Token.RPAREN);
			return select;
		}
		
		SqlSelect select = new SqlSelect();

		suspendNodes();
		
		accept(Token.SELECT);
		
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
			accept();
			select.setDistinct(true);
			return;
		}
		
		if(lexer.token() == Token.ALL){
			accept();
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
		
		setScope(Scope.SELECT_LIST);
		
		parseSelectItem(select);
		
		if(lexer.token() == Token.COMMA){
			do{
				accept();
				parseSelectItem(select);
			}while(lexer.token() == Token.COMMA);
		}
		
		removeScope();
		
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
					accept();
					break;
				case LEFT:
				case RIGHT:
					accept();
					if(lexer.token() == Token.OUTER){
						accept();
					}
					accept(Token.JOIN);
					break;
				case FULL:
					accept();
					if(lexer.token() == Token.OUTER){
						accept();
					}
					accept(Token.JOIN);
					break;
				case INNER:
					accept();
					accept(Token.JOIN);
					break;
				default:
					if(lexer.isIdentifier("STRAIGHT_JOIN") || lexer.isIdentifier("CROSS")){
						accept();
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
			//Accets 'ON' 
			accept();
			
			parseJoinOnExpr(query);
		}
	}
	
	protected void parseJoinOnExpr(SqlQuery query){
		new SqlExprParser(this).parseExpr();
		
		if(lexer.token() == Token.AND || lexer.token() == Token.OR) {
			accept();
			
			if(lexer.token() == Token.EXISTS) {
				parseExists(query, new AtomicInteger());
			}else{
				parseJoinOnExpr(query);
			}
		}
	}
	
	protected void parseSelectItem(SqlSelect select){
		if(lexer.token() == Token.STAR){
			acceptNode(new SqlAllColumns());
			return;
		}
		
		if(parseSpecialToken()){
			return;
		}
		
		if(lexer.token() == Token.LPAREN){
			accept();
			
			//select item : subquery
			if(lexer.token() == Token.SELECT){
				parseSelect();
			}else{
				parseSelectItem(select);	
			}
			
			accept(Token.RPAREN);
		}else if(lexer.isIdentifier() || (lexer.isKeyword() && SELECT_ITEM_KEYWORDS.contains(lexer.token()))){
			if(lexer.peekCharSkipWhitespaces() == '('){
				accept();
				accept(Token.LPAREN);
				parseRestForClosingParen();
				accept(Token.RPAREN);
				
				if(lexer.token().isKeywordOrIdentifier() && lexer.peekCharSkipWhitespaces() == '(') {
					accept();
					accept(Token.LPAREN);
					parseRestForClosingParen();
					accept(Token.RPAREN);
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
			accept();
			
			expects(Token.IDENTIFIER,Token.QUOTED_IDENTIFIER,Token.LITERAL_CHARS);
			
			String alias = lexer.tokenText();
			
			select.addSelectItemAlias(alias);
			
			accept();
			
			return alias;
		}
		
		if((lexer.token().isKeywordOrIdentifier() && lexer.token() != Token.FROM) || 
		   (lexer.token() == Token.LITERAL_CHARS)){
			
			String alias = lexer.tokenText();
			
			select.addSelectItemAlias(alias);
			
			accept();
			
			return alias;
		}
		
		return null;
	}
	
}