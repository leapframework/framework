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

import leap.orm.sql.ast.*;

public class SqlDynaParser extends SqlParser {

    public SqlDynaParser(SqlParser parent) {
        super(parent);
    }

    public void parseDynaSql() {
        parseAny();
    }

    @Override
    protected void parseToken() {
        Token token = lexer.token();
        if(token == Token.DYNAMIC){
            // {? .. }
            parseDynamicClause();
            return;
        }

        if(token == Token.COLON_PLACEHOLDER){
            // :name
            acceptNode(new ParamPlaceholder(scope(), token, lexer.literal()));
            return;
        }

        if(token == Token.SHARP_PLACEHOLDER){
            // #name#
            acceptNode(new ParamPlaceholder(scope(), token, lexer.literal()));
            return;
        }

        if(token == Token.DOLLAR_REPLACEMENT){
            // $name$
            acceptNode(new ParamReplacement(scope(), lexer.literal()));
            return;
        }

        if(token == Token.AT_IF){
            // @if(..) .. @elseif(..) .. @else .. @endif;
            parseIfClause();
            return;
        }

        if(token == Token.TAG){
            // @name{...}
            parseTag();
            return;
        }

        if(token == Token.LITERAL_CHARS){
            parseSqlString();
            return;
        }

        acceptText();
    }
}
