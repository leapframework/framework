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

import leap.orm.sql.ast.*;

import java.util.concurrent.atomic.AtomicInteger;


abstract class SqlQueryParser extends SqlParser {

	public SqlQueryParser(SqlParser parent) {
		super(parent);
	}

    protected boolean parseFrom(SqlQuery query){
        if(lexer.token() == Token.FROM){
            accept();
            parseFromItem(query);
            return true;
        }
        return false;
    }

    protected void parseFromItem(SqlQuery query) {
        parseTableSource(query);
    }

    protected void parseTableSource(SqlQuery query) {
        if(lexer.token() == Token.LPAREN){
            accept();

            if(lexer.token() == Token.SELECT){
                SqlSelect fromSelect = new SqlSelectParser(this).parseSelectBody();

                parseUnion();

                accept(Token.RPAREN);
                fromSelect.setAlias(parseTableAlias());
                query.addTableSource(fromSelect);
            }else{
                parseFromItem(query);

                parseUnion();

                accept(Token.RPAREN);
            }
            return;
        }

        parseTableName();

        if(node instanceof SqlTableName){
            SqlTableName table = (SqlTableName)node;
            table.setAlias(parseTableAlias());
            query.addTableSource(table);
        }
    }
	
	protected boolean parseWhere(SqlQuery query){
		if(lexer.token() == Token.WHERE){
            parseWhereExpression(query, new SqlWhere());
			return true;
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
            accept();
            return true;
        }

        if(token == Token.RPAREN){
            if(lparens.decrementAndGet() < 0){
                //sub-select end
                return false;
            }
            accept();
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

        //Sub-Query
        if(token == Token.SELECT && Token.LPAREN == lexer.prevToken()) {
            new SqlSelectParser(this).parseSelectBody();
            return true;
        }

        parseToken();
        return true;
    }

	protected void parseIn(SqlQuery query, AtomicInteger lparens){
		accept();
		
		if(lexer.token() == Token.LPAREN){
			lparens.incrementAndGet();
			accept();

			if(lexer.token() == Token.SELECT){
				parseSelect();
				accept(Token.RPAREN);
				lparens.decrementAndGet();
			}
		}
	}
	
	protected void parseExists(SqlQuery select,AtomicInteger lparens){
		accept();
		
		if(lexer.token() == Token.LPAREN){
			lparens.incrementAndGet();
			accept();

			if(lexer.token() == Token.SELECT){
				parseSelect();
				accept(Token.RPAREN);
				lparens.decrementAndGet();
			}
		}
	}
	
	protected void parseUnion() {
        if (lexer.token() == Token.UNION) {
            accept();

            if (lexer.token() == Token.ALL || lexer.token() == Token.DISTINCT) {
            	accept();
            } 
            
            new SqlSelectParser(this).parseSelectBody();
            return;
        }

        if (lexer.token() == Token.MINUS) {
        	accept();
        	
        	new SqlSelectParser(this).parseSelectBody();
        	return;
        }
	}
	
	protected String parseTableAlias(){
		if(lexer.token() == Token.AS){
			accept();
			
			expect(Token.IDENTIFIER);
			String alias = lexer.tokenText();
			accept();
			return alias;
		}

		final Token token = lexer.token();
		
		if(token.isKeywordOrIdentifier() && !isEndFromItem() && token != Token.ON){
			String alias = lexer.tokenText();
			accept();
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
