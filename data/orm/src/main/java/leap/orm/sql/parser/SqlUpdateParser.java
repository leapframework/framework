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
import leap.orm.sql.ast.SqlTableSource;
import leap.orm.sql.ast.SqlUpdate;


public class SqlUpdateParser extends SqlQueryParser {

	public SqlUpdateParser(SqlParser parent) {
		super(parent);
	}
	
	public void parseUpdateBody() {
        SqlUpdate update = new SqlUpdate();

        //suspendNodes();
        createSavePoint();

        expect(Token.UPDATE).acceptText();

        //parse table source
        parseTableSource(update);

        //Not a standard update sql.
        if(lexer.token() != Token.SET) {
            restoreSavePoint();
            parseAny();
            return;
        }

        //parse set
        expect(Token.SET).acceptText();

        //parse update columns
        parseUpdateColumns(update);

        //parse where
        if(parseWhere(update)) {
            parseQueryBodyRest(update);
        }else{
            parseRest();
        }

        update.setNodes(removeSavePoint());
        addNode(update);
	}

	protected SqlTableSource parseTableSource(SqlQuery query) {
        return parseTableNameSource(query);
	}
	
	protected String parseTableAlias(){
		if(lexer.token() == Token.AS){
            return acceptAlias();
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
			expect(Token.EQ).acceptText();
			parseUpdateValue(update);
		}
	}
	
	protected void parseUpdateValue(SqlUpdate update) {
		if(lexer.token() == Token.LPAREN){
			acceptText();
			
			//select item : subquery
			if (lexer.token() == Token.SELECT) {
				parseSelect();
			} else if (lexer.token() == Token.CASE) {
				String literal = lexer.peekLiteral();
				if (Token.WHEN.literal().equalsIgnoreCase(literal)) {
					new SqlExprParser(this).parseExpr();
				} else {
					parseUpdateValue(update);
				}
			} else {
				parseUpdateValue(update);
			}
			expect(Token.RPAREN).acceptText();
		}else if(lexer.isIdentifier() || lexer.isKeyword()){
            parseNameExpr();
		}else{
			new SqlExprParser(this).parseExpr();
		}
	}
}