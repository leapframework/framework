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

import static leap.orm.sql.parser.CharTypes.isWhitespace;

/**
 * Used for parsing sql string literal.
 */
class SqlStringLexer extends Lexer {

	public SqlStringLexer(String string) {
	    super(string);
    }
	
	@Override
    protected void scanToken() {
		for(;;){
			if(isEOF()){
				return;
			}
			
			//skip whitespace
			if(isWhitespace(ch)){
				nextChar();
				continue;
			}
			
			if(ch == '$'){
				nextChar();
				
				if(ch == '$'){
					for(;;){
						nextChar();
						if(ch != '$'){
							break;
						}
					}
				}
				
				int start = pos-1;
				
				if(ch == '{'){
					nextChar(); //skip '{'
					if(tryScanValueExpression()){
						startToken(start);
						_token = Token.EXPR_REPLACEMENT;
						nextChar();
						return;
					}
				}else{
					if(tryScanParameterWithSuffix('$')){
						startToken(start);
						_token = Token.DOLLAR_REPLACEMENT;
						nextChar();
						return;
					}
				}
			}
			
			nextChar();
		}
    }
}
