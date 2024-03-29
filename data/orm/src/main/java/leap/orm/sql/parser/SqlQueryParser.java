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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


abstract class SqlQueryParser extends SqlParser {

    protected static final Set<Token> WHERE_KEYWORDS = new HashSet<>();
    static {
        WHERE_KEYWORDS.add(Token.WHEN);
        WHERE_KEYWORDS.add(Token.TOP);
        WHERE_KEYWORDS.add(Token.COUNT);
        WHERE_KEYWORDS.add(Token.DELETE);
        WHERE_KEYWORDS.add(Token.END);
    }

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
        query.setFrom(parseTableSource(query));
    }

    protected SqlTableSource parseTableSource(SqlQuery query) {
        if(lexer.token() == Token.LPAREN) {
            acceptText();

            if (lexer.token() == Token.SELECT) {
                SqlSelect fromSelect = new SqlSelectParser(this).parseSelectBody();

                parseUnion();

                expect(Token.RPAREN).acceptText();
                fromSelect.setAlias(parseTableAlias());
                query.addTableSource(fromSelect);

                return fromSelect;
            } else {
                parseFromItem(query);

                if (!parseUnion()) {
                    new SqlSelectParser(this).parseJoins(query);
                    expect(Token.RPAREN).acceptText();
                    return query.getFrom();
                }

                expect(Token.RPAREN).acceptText();

                if (null == query.getFrom() && query.getTableSources().size() == 1) {
                    SqlTableSource ts = query.getTableSources().get(0);
                    if (ts instanceof SqlSelect) {
                        SqlSelect ss = (SqlSelect) ts;
                        ss.setAlias(parseTableAlias());
                    }
                } else if (query.getFrom() instanceof SqlSelect) {
                    SqlSelect ss = (SqlSelect) query.getFrom();
                    ss.setAlias(parseTableAlias());
                }

                return null;
            }
        }else if(lexer.token() == Token.QUOTED_TEXT) {
            QuotedText node = new QuotedText(lexer.literal());
            acceptNode(node);
            UnkownTableSource ts = new UnkownTableSource(node);
            ts.setAlias(parseTableAlias());
            query.addTableSource(ts);
            return ts;
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

        if(token == Token.AND) {
            acceptNode();
            return true;
        }

        if(token == Token.EQ) {
            acceptNode();
            return true;
        }

        if(token == Token.IDENTIFIER){
            parseSqlObjectName();
            return true;
        }

        if(token.isKeyword() && WHERE_KEYWORDS.contains(token)) {
            parseSqlObjectName();
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
        parseSubSelect(query, lparens);
	}
	
	protected void parseExists(SqlQuery select,AtomicInteger lparens){
        parseSubSelect(select, lparens);
	}

    protected void parseSubSelect(SqlQuery select, AtomicInteger lparens) {
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
	
	protected boolean parseUnion() {
        if (lexer.token() == Token.UNION) {
            acceptText();

            if (lexer.token() == Token.ALL || lexer.token() == Token.DISTINCT) {
            	acceptText();
            } 
            
            new SqlSelectParser(this).parseSelectBody();
            parseUnion();
            return true;
        }

        if (lexer.token() == Token.MINUS) {
        	acceptText();
        	
        	new SqlSelectParser(this).parseSelectBody();
        	return true;
        }

        return false;
	}
	
	protected String parseTableAlias(){
        if(lexer.token() == Token.RPAREN) {
            return null;
        }

		if(lexer.token() == Token.AS){
            return acceptAlias();
		}

		final Token token = lexer.token();
		
		if(token.isKeywordOrIdentifier() && !isEndFromItem() && token != Token.ON){
			String alias = lexer.tokenText();
			acceptText();
			return alias;
		}
		
		return null;
	}

    protected String acceptAlias() {
        acceptText();
        expect(Token.IDENTIFIER);
        String alias = lexer.tokenText();
        acceptText();
        return alias;
    }

    protected void parseNameExpr() {
        if(lexer.peekCharSkipWhitespaces() == '('){
            acceptText();
            parseParenExpr();
        }else{
            parseSqlObjectNameOrExpr();
        }
    }

    protected void parseSqlObjectNameOrExpr(){
        if (!parseSqlObjectName()) {
            acceptText(lexer.token());
        }

        if (lexer.token().isOperator()) {
            acceptText(lexer.token());

            if (lexer.token() == Token.LPAREN) {
                parseParenExpr();
            } else if (!parseSpecialToken()) {
                parseSqlObjectNameOrExpr();
            }
        }
    }

    protected void parseParenExpr() {
        expect(Token.LPAREN).acceptText();
        parseRestForClosingParen();
        expect(Token.RPAREN).acceptText();

        if(lexer.token() == Token.FROM) {
            return;
        }

        if (lexer.peekCharSkipWhitespaces() == '(') {
            if (lexer.token().isKeywordOrIdentifier()) {
                acceptText();
                expect(Token.LPAREN).acceptText();
                parseRestForClosingParen();
                expect(Token.RPAREN).acceptText();
            } else if (lexer.token().isOperator()) {
                acceptText();
                expect(Token.LPAREN).acceptText();
                parseNameExpr();
                expect(Token.RPAREN).acceptText();
            }
        } else if(lexer.token().isOperator()) {
            acceptText();
            parseNameExpr();
        }
    }

	protected boolean isEndFromItem(){
//		if(lexer.isEOF()) {
//			return true;
//		}
		
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
