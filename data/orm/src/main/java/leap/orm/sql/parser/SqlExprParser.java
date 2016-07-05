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


/**
 * Most of codes inspire or copy from com.alibaba.druid.sql.parser.SqlExprParser, see https://github.com/alibaba/druid
 */
class SqlExprParser extends SqlParser {

	public SqlExprParser(SqlParser parent) {
		super(parent);
	}
	
	protected final void parseExpr(){
		parsePrimary();
		
		if(lexer.token() == Token.LPAREN){
			acceptText();
			parseExprLparenToken();
		}
		
		parseExprRest();
	}
	
	protected void parseExprRest(){
		if(isEndExpr()){
			return;
		}
		
		if(parseRelationalExprRest()) {
		    return;
		}
		
		parseToken();
		parsePrimary();
	}
	
	protected boolean parseRelationalExprRest() {
	    Token token = lexer.token();

	    if(token == Token.IS) {
	        acceptText();
	        
	        if(lexer.isToken(Token.NOT)) {
	            acceptText();
	        }
	        
	        parsePrimary();
	        
	        return true;
	    }
	    
	    return false;
	}
	
	protected boolean isEndExpr(){
		if (lexer.isEOS()) {
			return true;
		}
		
		Token token = lexer.token();
		
		if(token == Token.COMMA){
			return true;
		}
		
		if(token == Token.RPAREN){
			return true;
		}
		
		if(token == Token.DYNAMIC) {
		    return true;
		}
		
		if(token.isKeywordOrIdentifier() && !token.isOperator()){
			return true;
		}
		
		return false;
	}
	
	protected void parsePrimary(){
		final Token token = lexer.token();
		
		switch (token) {
			case RPAREN:
				return;
			case LPAREN:
				acceptText();
				parseExprLparenToken();
				break;
			case CASE:
			    acceptText();
			    
			    if(lexer.token() != Token.WHEN) {
			        parseExpr();
			    }
			    
			    expect(Token.WHEN).acceptText();
			    parseExpr();
			    expect(Token.THEN).acceptText();
			    parseExpr();
			    
			    while(lexer.token() == Token.WHEN) {
			        acceptText();
			        parseExpr();
			        expect(Token.THEN).acceptText();
			        parseExpr();
			    }
			    
			    if(lexer.token() == Token.ELSE) {
			        acceptText();
			        parseExpr();
			    }
			    
			    expect(Token.END).acceptText();
			    break;
			default:
			    parsePrimaryToken();
				break;
		}
	}
	
	protected void parsePrimaryToken(){
		parseToken();
	}
	
	protected void parseExprLparenToken(){
		parseExpr();
		
		if(lexer.token() != Token.RPAREN) {
			do{
				acceptText();
				
				if(lexer.token() == Token.RPAREN){
					break;
				}
				
				parseExpr();
			}while(lexer.token() != Token.RPAREN);
		}
		
		/*
		if(lexer.token() == Token.COMMA){
			do{
				accept();
				
				if(lexer.token() == Token.RPAREN){
					break;
				}
				
				parseExpr();
			}while(lexer.token() == Token.COMMA);
		}
		*/
		
		expect(Token.RPAREN).acceptText();
	}
	
}