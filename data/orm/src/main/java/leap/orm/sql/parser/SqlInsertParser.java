/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.orm.sql.parser;

import leap.orm.sql.ast.AstNode;
import leap.orm.sql.ast.SqlInsert;
import leap.orm.sql.ast.SqlObjectName;
import leap.orm.sql.ast.SqlTableName;

public class SqlInsertParser extends SqlParser {

    public SqlInsertParser(SqlParser parent) {
        super(parent);
    }

    public void parseInsertBody() {
        SqlInsert insert = new SqlInsert();

        suspendNodes();

        expect(Token.INSERT).acceptText();

        if(lexer.token() == Token.INTO) {
            acceptText();

            //parse table name
            if(parseTableName(insert) ){

                //parse insert columns
                if(parseInsertColumns(insert)) {

                    //parse insert values.
                    parseInsertValues(insert);
                }

            }
        }

        parseRest();

        insert.setNodes(nodes());
        restoreNodes().addNode(insert);
    }

    protected boolean parseTableName(SqlInsert insert) {
        SqlTableName tn = parseTableName();

        if(null != tn) {
            insert.setTableName(tn);
            return true;
        }

        return false;
    }

    protected boolean parseInsertColumns(SqlInsert insert) {
        if(acceptText(Token.LPAREN)) {

            if(parseColumnName(insert)) {

                if(lexer.token() == Token.COMMA){
                    do{
                        acceptText();

                        parseColumnName(insert);

                    }while(lexer.token() == Token.COMMA);
                }

            }

            expect(Token.RPAREN).acceptText();

            return true;
        }

        return false;
    }

    protected boolean parseInsertValues(SqlInsert insert) {
        if(lexer.isIdentifier("values")) {

            acceptText();

            if(acceptText(Token.LPAREN)) {

                if(parseInsertValue(insert)) {

                    if(lexer.token() == Token.COMMA){
                        do{
                            acceptText();

                            parseInsertValue(insert);

                        }while(lexer.token() == Token.COMMA);
                    }

                }

                expect(Token.RPAREN).acceptText();
            }

        }

        return false;
    }

    protected boolean parseColumnName(SqlInsert insert) {
        if(lexer.token().isKeywordOrIdentifier()) {

            SqlObjectName columnName = new SqlObjectName();

            columnName.setQuoted(lexer.token().isQuotedIdentifier());
            columnName.setLastName(lexer.tokenText());

            acceptNode(columnName);

            insert.addColumn(columnName);
            return true;
        }
        return false;
    }

    protected boolean parseInsertValue(SqlInsert insert) {
        AstNode node = parseExprNode();
        if(null != node) {
            addNode(node);
            insert.addValue(node);
            return true;
        }

        return false;
    }

}
