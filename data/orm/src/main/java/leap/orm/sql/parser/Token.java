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

import leap.lang.annotation.Internal;

/**
 * Most codes inspire or copy from com.alibaba.druid.sql.parser.Token in <a href="https://github.com/alibaba/druid">durid</a>. 
 */
@Internal
public enum Token {
	//standard sql token
    SELECT("SELECT"), 
    DELETE("DELETE"), 
    INSERT("INSERT"), 
    UPDATE("UPDATE"), 
    
    UNION("UNION"), 
    MINUS("MINUS"),
    
    DISTINCT("DISTINCT"),
    ALL("ALL"), 
    TOP("TOP"), 
    COUNT("COUNT"),
    
    FROM("FROM"), 
    LEFT("LEFT"),
    RIGHT("RIGHT"),
    FULL("FULL"),
    INNER("INNER"),
    OUTER("OUTER"),
    JOIN("JOIN"),
    ON("ON"),
    WHERE("WHERE"), 
    
    ORDER("ORDER"), 
    BY("BY"),
    GROUP("GROUP"), 
    HAVING("HAVING"), 
    ASC("ASC"),
    DESC("DESC"),
    
    LIMIT("LIMIT"),
    
    INTO("INTO"), 
    SET("SET"),
    AS("AS"), 
    
    IS("IS",true),
    NULL("NULL", true),
    TRUE("TRUE", true),
    FALSE("FALSE", true),
    
    AND("AND",true),
    OR("OR",true),
    XOR("XOR",true),
    
    CASE("CASE"),
    WHEN("WHEN"),
    THEN("THEN"),
    ELSE("ELSE"),
    END("END"),
    
    NOT("NOT", true),
    LIKE("LIKE"),
    IN("IN"),
    EXISTS("EXISTS"),
    BETWEEN("BETWEEN"),
    
	IDENTIFIER,
	QUOTED_IDENTIFIER,
    LITERAL_INT,
    LITERAL_FLOAT,
    LITERAL_HEX,	
	LITERAL_CHARS,
	
    SEMI(";"), 
    COMMA(","), 
    LPAREN("("), 
    RPAREN(")"), 
    LBRACE("{"), 
    RBRACE("}"),
    
    //operators
    QUES("?",true),
    EQ("=",true), 
    GT(">",true),
    SUBGT("->", true),
    SUBGTGT("->>", true),
    LT("<",true), 
    BANG("!",true),
    TILDE("~",true), 
    EQEQ("==",true), 
    LTEQ("<=",true), 
    LTEQGT("<=>"), 
    LTGT("<>",true), 
    GTEQ(">=",true), 
    BANGEQ("!=",true), 
    BANGGT("!>",true), 
    BANGLT("!<",true),    
    AMPAMP("&&",true), 
    BARBAR("||",true), 
    PLUS("+",true), 
    SUB("-",true), 
    STAR("*",true), 
    SLASH("/",true), 
    AMP("&",true), 
    BAR("|",true), 
    CARET("^",true), 
    PERCENT("%",true), 
    LTLT("<<",true), 
    GTGT(">>",true),
	
	//special token
	DOLLAR_REPLACEMENT, // $parameter$
	EXPR_REPLACEMENT,   // ${expr}
	SHARP_PLACEHOLDER,  // #parameter#
	COLON_PLACEHOLDER,  // :parameter
	JDBC_PLACEHOLDER,   // ?
	EXPR_PLACEHOLDER,   // #{expr}
	
	DYNAMIC, //{? .. }
    QUOTED_TEXT, //```...```

	AT_IF("@IF"),
	AT_ELSEIF("@ELSEIF"),
	AT_ELSE("@ELSE"),
	AT_ENDIF("@ENDIF"),

    AT_INCLUDE("@INCLUDE"),
	
	TAG, // @tagname{  .. }
	
	COMMENTS,
	
	EOF;
	
	private final String  literal;
	private final boolean keyword;
	private final boolean operator;
	
	private Token(){
		this.literal  = null;
		this.keyword  = false;
		this.operator = false;
	}
	
	private Token(String literal){
		this(literal,false);
	}
	
	private Token(String literal,boolean operator){
		this.literal  = literal;
		this.operator = operator;
		
		for(int i=0;i<literal.length();i++){
			if(!Character.isLetter(literal.charAt(i))){
				keyword = false;
				return;
			}
		}
		keyword = true;
	}
	
	public String literal(){
		return literal;
	}
	
	public boolean isKeyword(){
		return keyword;
	}
	
	public boolean isKeywordOrIdentifier(){
		return keyword || this == IDENTIFIER || this == QUOTED_IDENTIFIER; 
	}
	
	public boolean isIdentifier(){
		return this == IDENTIFIER || this == QUOTED_IDENTIFIER;
	}
	
	public boolean isQuotedIdentifier() {
	    return this == QUOTED_IDENTIFIER;
	}
	
	public boolean isOperator(){
		return operator;
	}
}