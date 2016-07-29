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

import leap.orm.sql.Sql;
import leap.orm.sql.ast.*;

import java.util.concurrent.atomic.AtomicInteger;


abstract class SqlQueryParser extends SqlParser {

	public SqlQueryParser(SqlParser parent) {
		super(parent);
	}

    protected boolean parseFrom(SqlQuery query){
        if(lexer.token() == Token.FROM){
            acceptText();
            parseFromItem(query);
            return true;
        }
        return false;
    }

    protected void parseFromItem(SqlQuery query) {
        parseTableSource(query);
    }

    protected SqlTableSource parseTableSource(SqlQuery query) {
        if(lexer.token() == Token.LPAREN){
            acceptText();

            if(lexer.token() == Token.SELECT){
                SqlSelect fromSelect = new SqlSelectParser(this).parseSelectBody();

                parseUnion();

                expect(Token.RPAREN).acceptText();
                fromSelect.setAlias(parseTableAlias());
                query.addTableSource(fromSelect);

                return fromSelect;
            }else{
                parseFromItem(query);

                parseUnion();

                expect(Token.RPAREN).acceptText();

                return null;
            }
        }else {
            return parseTableNameSource(query);
        }

    }

    protected SqlTableSource parseTableNameSource(SqlQuery query) {
        parseTableName();

        if(node instanceof SqlTableName){
            SqlTableName table = (SqlTableName)node;
            table.setAlias(parseTableAlias());
            query.addTableSource(table);

            return table;
        }

        return null;
    }

	protected boolean parseWhere(SqlQuery query){
		if(lexer.token() == Token.WHERE){
            pushScope(Sql.Scope.WHERE);
            parseWhereExpression(query, new SqlWhere());
            popScope();
			return true;
		}else{
            addNode(new SqlWhere(query));
        }
		return false;
	}

    protected void parseWhereExpression(SqlQuery query, SqlWhere where) {
        suspendNodes();

        acceptNode(new SqlToken(lexer.token(), lexer.tokenText()));

        AtomicInteger lparens = new AtomicInteger();
        for(;;) {
            if(lexer.isEOS()){
                appendText();
                break;
            }

            if(!parseWhereBodyToken(query, lparens, lexer.token())) {
                break;
            }

            //order by
            if(lookahead(Token.ORDER, Token.BY)) {
                break;
            }

            //group by
            if(lookahead(Token.GROUP, Token.BY)) {
                break;
            }

            Token token = lexer.token();

            //union, limit (offset)
            if(token == Token.UNION || token == Token.MINUS || token == Token.LIMIT) {
                break;
            }

            //todo : for update, etc...
        }

        where.setQuery(query);
        where.setNodes(nodes());
        restoreNodes().addNode(where);
    }

	protected void parseQueryBodyRest(SqlQuery query){
		AtomicInteger lparens = new AtomicInteger();
		
		for(;;){
			if(lexer.isEOS()){
				appendText();
				break;
			}

            Token token = lexer.token();

            //Union
            if(token == Token.UNION || token == Token.MINUS) {
                //parseUnionQuery(query);
                return;
            }
			
			if(!parseWhereBodyToken(query, lparens, lexer.token())){
                break;
            }
		}
	}

    protected boolean parseWhereBodyToken(SqlQuery query, AtomicInteger lparens, Token token) {
        if(token == Token.LPAREN){
            lparens.incrementAndGet();
            acceptText();
            return true;
        }

        if(token == Token.RPAREN){
            if(lparens.decrementAndGet() < 0){
                //sub-select end
                return false;
            }
            acceptText();
            return true;
        }

        if(token == Token.IN){
            parseIn(query,lparens);
            return true;
        }

        if(token == Token.EXISTS){
            parseExists(query,lparens);
            return true;
        }

        if(token == Token.IDENTIFIER){
            parseSqlObjectName();
            return true;
        }

        if(token == Token.AND) {
            acceptNode();
            return true;
        }

        if(token == Token.EQ) {
            acceptNode();
            return true;
        }

        if(token == Token.LITERAL_CHARS || token == Token.LITERAL_INT) {
            acceptNode(new SqlLiteral(lexer.token(),lexer.text(),lexer.literal()));
            return true;
        }

        //Sub-Query
        if(token == Token.SELECT && Token.LPAREN == lexer.prevToken()) {
            new SqlSelectParser(this).parseSelectBody();
            return true;
        }

        parseToken();
        return true;
    }

	protected void parseIn(SqlQuery query, AtomicInteger lparens){
		acceptText();
		
		if(lexer.token() == Token.LPAREN){
			lparens.incrementAndGet();
			acceptText();

			if(lexer.token() == Token.SELECT){
				parseSelect();
				expect(Token.RPAREN).acceptText();
				lparens.decrementAndGet();
			}
		}
	}
	
	protected void parseExists(SqlQuery select,AtomicInteger lparens){
		acceptText();
		
		if(lexer.token() == Token.LPAREN){
			lparens.incrementAndGet();
			acceptText();

			if(lexer.token() == Token.SELECT){
				parseSelect();
				expect(Token.RPAREN).acceptText();
				lparens.decrementAndGet();
			}
		}
	}
	
	protected void parseUnion() {
        if (lexer.token() == Token.UNION) {
            acceptText();

            if (lexer.token() == Token.ALL || lexer.token() == Token.DISTINCT) {
            	acceptText();
            } 
            
            new SqlSelectParser(this).parseSelectBody();
            parseUnion();
            return;
        }

        if (lexer.token() == Token.MINUS) {
        	acceptText();
        	
        	new SqlSelectParser(this).parseSelectBody();
        	return;
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
		
		if(token.isKeywordOrIdentifier() && !isEndFromItem() && token != Token.ON){
			String alias = lexer.tokenText();
			acceptText();
			return alias;
		}
		
		return null;
	}

	protected boolean isEndFromItem(){
		if(lexer.isEOF()) {
			return true;
		}
		
		final Token token = lexer.token();
		switch (token) {
			case COMMA:
			case RPAREN:
			case LEFT:
			case RIGHT:
			case INNER:
			case FULL:
			case JOIN:
			case WHERE:
			case ORDER:
			case GROUP:
			case UNION:
			case MINUS:
				return true;
			default:
				return false;
		}
	}
}
