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

import static leap.orm.sql.parser.CharTypes.isFirstIdentifierChar;
import static leap.orm.sql.parser.CharTypes.isIdentifierChar;
import static leap.orm.sql.parser.CharTypes.isParameterChar;
import static leap.orm.sql.parser.CharTypes.isWhitespace;
import leap.lang.Strings;
import leap.lang.annotation.Internal;
import leap.orm.sql.Sql;
import leap.orm.sql.Sql.ParseLevel;

/**
 * Syntax : 
 * 
 * <pre>
 *	 
 * Replacement    : $parameter$ ${expression}
 * 
 * Placeholder    : #parameter# #{expression} :parameter 
 * 
 * If Clause      : @if(..) .. @elseif(..) .. @else .. @end
 * 
 * Dynamic Clause : {? ... }
 * 
 * Line Comment   : --comment or //comment
 * 
 * MultiLine Comment : / * comment * /
 * 
 * </pre>
 */

/**
 * Most of codes inspire or copy from com.alibaba.druid.sql.parser.Lexer, see https://github.com/alibaba/druid
 */
@Internal
public class Lexer {
	
    private static final char EOI = 0x1A;
    
    private static boolean isOperator(char ch) {
        switch (ch) {
            case '!':
            case '%':
            case '&':
            case '*':
            case '+':
            case '-':
            case '<':
            case '=':
            case '>':
            case '^':
            case '|':
            case '~':
            case ';':
                return true;
            default:
                return false;
        }
    }
    
    protected final String        sql;
    protected final StringBuilder chars;
	protected final Keywords      keywords = Keywords.DEFAULT;
	protected final Keywords      atwords  = Keywords.ATWORDS;
	protected final boolean       scanMore;
	protected final ParseLevel    level;
	
	protected int   pos = -1;
    protected char  ch;
    protected int   markPos;
    protected int   textPos;
    protected Token _token;
    protected Token _prevToken;
    
    private boolean   eof;
    private int       tokenStart;
    private int       literalStart;
    private int       literalEnd;
    private String    literal;
    private SavePoint sp;
    
	public Lexer(String sql) {
		this(sql,null);
	}
	
	public Lexer(String sql,Sql.ParseLevel level) {
		this.sql      = sql;
		this.chars    = new StringBuilder(this.sql);
		this.level    = null == level ? ParseLevel.BASE : level;
		this.scanMore = true;
		nextChar();
	}
	
	protected final char charAt(int index) {
		return index < chars.length() ? chars.charAt(index) : EOI;
	}
	
	protected final String substring(int start,int end){
		return chars.substring(start,end);
	}
	
	protected final String substring(int start){
		return chars.substring(start);
	}
	
	protected final void delete(int start,int end){
		chars.delete(start, end);
		pos = (pos - (end - start));
	}
	
	protected final void delete(int index){
		chars.deleteCharAt(index);
		pos--;
	}
	
	public final void nextChar(){
		ch = charAt(++pos);
	}
	
	public final void nextChars(int number){
		pos+=number;
		ch = charAt(pos);
	}
	
	public final char peekChar(){
		return charAt(pos + 1);
	}

	public final char peekChar(int next){
		return charAt(pos + next);
	}
	
	public final char peekCharSkipWhitespaces(){
		int  i=pos;
		char c;
		for(;;){
			c = charAt(i++);
			if(c == EOI || !isWhitespace(c)){
				break;
			}
		}
		return c;
	}

	public final String peekLiteral() {
		int i = pos;
		char c;
		for(;;){
			c = charAt(i++);
			if (c == EOI) {
				return Strings.EMPTY;
			}
			if(!isWhitespace(c)){
				break;
			}
		}
		StringBuilder builder = new StringBuilder();
		builder.append(c);
		for(;;){
			c = charAt(i++);
			if(c == EOI || isWhitespace(c)){
				break;
			}
			builder.append(c);
		}
		return builder.toString();
	}
	
	/*
	public final Token peekToken(){
		createSavePoint();
		nextToken();
		Token tok = this._token;
		restoreSavePoint();
		return tok;
	}
	*/
	
	/**
     * Return the current index of character cursor in this lexer.
     * 
     * <p>
     * Position is start from 0.
     */
    public final int pos() {
        return pos;
    }
    
	/**
	 * Returns current token.
	 */
	public final Token token(){
		return _token;
	}

	/**
	 * Returns previous token.
	 */
	public final Token prevToken() {
		return _prevToken;
	}

    /**
     * Returns the position of current token.
     * 
     * <p>
     * Returns -1 if no current token.
     */
    public final int tokenStart(){
    	return tokenStart;
    }
    
    /**
     * The value of a literal token, recorded as a string. For integers, leading 0x and 'l' suffixes are suppressed.
     */
    public final String literal() {
    	if(null == literal){
    		literal = (literalStart >= 0 ? substring(literalStart,literalEnd) : null);
    	}
    	return literal;
    }
	
    public final int intValue(){
    	checkLiteral();
    	try {
	        return Integer.parseInt(literal());
        } catch (NumberFormatException e) {
        	reportError("Invalid int literal '{0}' : {1}",literal(),describePosition());
        	return 0;
        }
    }
	
	/**
	 * Is current position at the end of input.
	 */
	public final boolean isEOF(){
		return pos >= chars.length();
	}
	
	/**
	 * EOS means 'end of statement'.
	 */
	public final boolean isEOS(){
		return _token == Token.EOF || _token == Token.SEMI;
	}
	
	public final String tokenText(){
		return substring(tokenStart,pos);
	}
	
    protected final void checkLiteral(){
    	if(!hasLiteral()){
    		throw new IllegalStateException("No literal");
    	}
    }
	
    /**
     * Returns the current parsed text from {@link #textPos} to {@link #textEnd} or current pos.
     * 
     * <p>
     * 
     * Returns <code>null</code> if {@link #textPos} at current pos.
     * 
     */
	public final String text(){
		if(textPos < pos){
			return isEOF() ? substring(textPos) : substring(textPos,pos);
		}
		return null;
	}

	/**
	 * Returns the text from {@link #textPos} to {@link #tokenStart()}.
	 * 
	 * that {@link #acceptText()} will move the {@link #textPos} to {@link #tokenStart()}, 
	 */
	public final String acceptText(){
		if(textPos < tokenStart){
			String text = substring(textPos,tokenStart);
			textPos = tokenStart;
			return text;
		}else if(tokenStart < 0 && textPos < pos){
			String text = isEOF() ? substring(textPos) : substring(textPos,pos);
			textPos = pos;
			return text;
		}
		return null;
	}
	
	protected final void startToken(){
		tokenStart = pos;
	}
	
	protected final void startToken(int pos){
		tokenStart = pos;
	}
	
	protected final void startToken(Token tok){
		tokenStart = pos;
		this._token = tok;
	}
	
	protected final void startToken(int startPos,Token tok){
		tokenStart = startPos;
		this._token = tok;
	}
	
	protected final void startLiteral(){
		literalStart = pos;
		literal      = null;
	}

    protected final void startLiteral(int pos){
        literalStart = pos;
        literal      = null;
    }
	
	protected final void endLiteral(){
		literalEnd = pos;
	}
	
	protected final void endLiteral(int pos){
		literalEnd = pos;
	}
	
	protected final void resetLiteral(){
		this.literal      = null;
		this.literalStart = -1;
		this.literalEnd   = -1;
	}
	
	/*
	public final boolean lookahead(Token... nextTokens){
		if(null == nextTokens || nextTokens.length==0){
			return false;
		}
		
		createSavePoint();
		
		boolean match = true;
		
		for(int i=0;i<nextTokens.length;i++){
			nextToken();

			if(this._token != nextTokens[i]){
				match = false;
				break;
			}
		}
		
		restoreSavePoint();
		return match;
	}
	
	public final boolean lookahead(String... nextLiterals){
		if(null == nextLiterals || nextLiterals.length==0){
			return false;
		}
		
		createSavePoint();
		
		boolean match = true;
		
		for(int i=0;i<nextLiterals.length;i++){
			nextToken();

			if(!nextLiterals[i].equalsIgnoreCase(this.literal())){
				match = false;
				break;
			}
		}
		
		restoreSavePoint();
		return match;
	}
	*/
	
	public final void nextToken(){
        if(eof) {
            throw new IllegalStateException("Lexer EOF!");
        }
        
		reset();
		scanToken();	
	}

	public final boolean nextToChar(char c) {
        if(eof) {
            throw new IllegalStateException("Lexer EOF!");
        }
        
        reset();
        for(;;) {
            nextChar();
            
            if(ch == c) {
                return true;
            }
            
            if(ch == EOI) {
                _token = Token.EOF;
                eof = true;
                return false;
            }
        }
	}
	
	private void reset(){
		markPos      = -1;
		textPos      = pos;
		_prevToken   = _token;
		_token        = null;
		tokenStart   = -1;
		resetLiteral();		
	}
	
	protected void scanToken(){
		for(;;){
			if(ch == EOI){
				_token = Token.EOF;
				eof = true;
				return;
			}
			
			//skip whitespace
			if(isWhitespace(ch)){
				nextChar();
				continue;
			}
			
			int nextChar;
			
			switch (ch) {
				case '\'' :
					startToken(Token.LITERAL_CHARS);
					scanString();
					return;
				case '-' : 
					nextChar = peekChar();

					if(nextChar == '-'){
					    startToken(Token.COMMENTS);
						//markPos = pos;
						scanSingleLineComment();
						//delete(markPos, pos);
						//continue;
						return;
					}else if(scanMore){
						scanOperator();
						return;
					}else{
						nextChar();
					}
					
					continue;
				case '/' : 
					nextChar = peekChar();
					
					if(nextChar == '/'){
					    startToken(Token.COMMENTS);
						//markPos = pos;
						scanSingleLineComment();
						//delete(markPos, pos);
						//continue;
						return;
					}else if(nextChar == '*'){
						//endText();
					    startToken(Token.COMMENTS);
						//markPos = pos;
						scanMultiLineComment();
						//delete(markPos, pos);
						//continue;
						return;
					}else if(scanMore){
						startToken(Token.SLASH);
						nextChar();
						return;
					}
				case ';':
					delete(pos);
					startToken(Token.SEMI);
					nextChar();
					return;
				case ',' :
					startToken(Token.COMMA);
					nextChar();
					return;
				case '(' : 
					startToken(Token.LPAREN);
					nextChar();
					return;
				case ')' : 
					startToken(Token.RPAREN);
					nextChar();
					return;
				case '$':
					//Replacement : $parameter$ ${expression}
					
					startToken();
					nextChar();
					
					if(ch == '{'){
						nextChar(); //skip '{'
						scanValueExpression();
						_token = Token.EXPR_REPLACEMENT;
					}else{
						scanParameterWithSuffix('$');
						_token = Token.DOLLAR_REPLACEMENT;
					}
					return;
					
				case '?' : 
					//Index Placeholder
					startToken(Token.JDBC_PLACEHOLDER);
					nextChar();
					return;
					
				case '#' : 
					//Placeholder : #parameter# or #{expression}
					startToken();
					nextChar();
					
					if(ch == '{'){
						nextChar(); //skip '{'
						scanValueExpression();
						_token = Token.EXPR_PLACEHOLDER;
					}else{
						scanParameterWithSuffix('#');
						_token = Token.SHARP_PLACEHOLDER;
					}
					
					return;
					
				case '{' : 
					startToken();
					
					//Dynamic Clause : {? .. }
					nextChar = peekChar();
					
					if(nextChar == '?'){
						_token = Token.DYNAMIC;
					}else{
						_token = Token.LBRACE;
						nextChar();
					}
					return;
					
				case '}':
					startToken(Token.RBRACE);
					nextChar();
					return;
				case '@' :
					//@tagname , such as @if
					if(scanAtToken()){
						return;
					}
					continue;
				case '`':
					char next1Char = peekChar(1);
					if(next1Char == '`') {
						char next2Char = peekChar(2);
						if(next2Char == '`') {
							startToken(Token.QUOTED_TEXT);
							nextChars(3);
							scanQuotedText();
							return;
						}
					}
				case '"':
					startToken(Token.QUOTED_IDENTIFIER);
					scanQuotedIdentifier();
					return;
				case ':' :
					nextChar = peekChar();
					if(nextChar != '='){
						//Parameter Placeholder -> :parameter
						startToken();
				    	nextChar(); //skip ':'
						scanParameter();
						_token = Token.COLON_PLACEHOLDER;
						return;
					}					
				default:
                    if (isFirstIdentifierChar(ch)) {
                    	startToken();
                        scanIdentifier();
                        setIdentifierToken();
                        return;
                    }
                    
                    if(scanMoreTokens()){
                    	return;
                    }
                    
                    nextChar();
					break;
			}
		}
	}
	
	protected final boolean scanMoreTokens(){
		switch (ch) {
            case '0':
            	startToken();
                if (charAt(pos + 1) == 'x') {
                    nextChar();
                    nextChar();
                    scanHexaDecimal();
                } else {
                    scanNumber();
                }
                return true;
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            	startToken();
                scanNumber();
                return true;	
			default:
				if(isOperator(ch)){
					scanOperator();
					return true;
				}
				
				break;
		}
		
		return false;
	}
	
	public final void scanString(){
		//ch == '\''
		nextChar(); //skip '\''
		startLiteral();
		
        for (;;) {
            if (isEOF()) {
                reportError("Unclosed string : {0}",describePosition(markPos));
                return;
            }

            if (ch == '\'') {
                nextChar();
                if (ch != '\'') {
                    break;
                } 
            }
            
            nextChar();
        }
        
        endLiteral(pos - 1);
	}
	
    public void scanNumber() {
    	startLiteral();
    	
        if (ch == '-') {
        	nextChar();
        }

        for (;;) {
            if (ch < '0' || ch > '9') {
                break;
            }
            nextChar();
        }

        boolean isDouble = false;

        if (ch == '.') {
            if (peekChar() == '.') {
                _token = Token.LITERAL_INT;  //TODO : int value like 100.. ?
                endLiteral();
                return;
            }
            nextChar();
            isDouble = true;

            for (;;) {
                if (ch < '0' || ch > '9') {
                    break;
                }
                nextChar();
            }
        }

        if (ch == 'e' || ch == 'E') {
        	nextChar();

            if (ch == '+' || ch == '-') {
            	nextChar();
            }

            for (;;) {
                if (ch < '0' || ch > '9') {
                    break;
                }
                nextChar();
            }

            isDouble = true;
        }

        endLiteral();
        if (isDouble) {
            _token = Token.LITERAL_FLOAT;
        } else {
            _token = Token.LITERAL_INT;
        }
    }
    
    public void scanHexaDecimal() {
        startLiteral();

        if (ch == '-') {
            nextChar();
        }

        for (;;) {
            if (!CharTypes.isHex(ch)) {
                break;
            }
            nextChar();
        }

        endLiteral();
        _token = Token.LITERAL_HEX;
    }
	
	public final void scanSingleLineComment(){
		//ch == '-' || ch == '/'
		nextChar(); //skip next '-' or '/'
		
        for (;;) {
        	if(isEOF()){
        		return;
        	}
        	
            if (ch == '\r') {
                if (peekChar() == '\n') {
                    nextChar();
                    break;
                }
                break;
            }

            if (ch == '\n') {
                nextChar();
                break;
            }

            nextChar();
        }
        
        return;
	}
	
	public final void scanMultiLineComment(){
		//ch == '/'
        nextChar(); //skip '*'
        
        for (;;) {
        	if(isEOF()){
        		return;
        	}
        	
            if (ch == '*' && peekChar() == '/') {
                nextChar();
                nextChar();
                break;
            }

            nextChar();
        }

        return;
	}
	
	/**
	 * Condition Expression : ( ... ), i.e @if(..) @elseif(..)
	 * 
	 * <p>
	 * Pos must move to the first char after '('
	 */
	public final void scanConditionalExpression(){
        int start = pos;

		int lparens = 0;
		
		for(;;){
			if(ch == '('){
				lparens++;
			}else if(ch == ')'){
				if(lparens == 0){
					break;
				}else{
					lparens--;	
				}
			}else if(ch == '\''){
				scanString();
				continue;
			}
			nextChar();
		}

        startLiteral(start);
        endLiteral();

		if(!hasLiteral()){
			reportError("Expression can not be emtpy : {0}",describePosition(markPos));
		}
		
		nextChar();
	}
	
	public final void scanValueExpression(){
		if(tryScanValueExpression()){
			if(literal().length() == 0){
				reportError("Empty expression not allowed : {0}",describePosition(tokenStart));
			}
			nextChar();
		}else{
			reportError("Unclosed expression : {0}",describePosition(tokenStart));
		}
	}
	
	/**
	 * Value Expression : ${..} or #{ .. }
	 * 
	 * <p>
	 * Pos must move to the first char after '{'
	 */
	public final boolean tryScanValueExpression(){
		startLiteral();
		
		for(;;){
			nextChar();
			
            if (isEOF()) {
            	resetLiteral();
                return false;
            }
			
			if(ch == '}'){
				break;
			}
		}
		
		endLiteral();
		return true;
	}
	
	public final void scanParameterWithSuffix(char suffix){
		if(tryScanParameterWithSuffix(suffix)){
			if(literal().length() == 0){
				reportError("Empty parameter name not allowed : {0}",describePosition());
			}
			nextChar();
		}else{
			reportError("Unclosed parameter : {0}",describePosition());	
		}
	}
	
	public final boolean tryScanParameterWithSuffix(char suffix){
		startLiteral();
		
		for(;;){
            if (isEOF()) {
                return false;
            }
            
            if(ch == suffix){
            	break;
            }
            
            nextChar();
		}
		
		endLiteral();
		return true;
	}
	
	public final void scanParameter(){
    	
        if (!isIdentifierChar(ch)) {
            reportError("Illegal parameter character '{0}' at {1}",ch,describePosition());
        }

        startLiteral();
        
        for (;;) {
        	nextChar();

            if (!isParameterChar(ch)) {
                break;
            }
        }

        endLiteral();
        
		if(literal().length() == 0){
			reportError("Empty parameter name not allowed : {0}",describePosition());
		}
	}
	
	public final void skipWhitespaces(){
		for(;;){
			if(!Character.isWhitespace(ch)){
				break;
			}
			nextChar();
		}
	}
	
	public final boolean scanAtToken(){
		int startPos = pos;
		
		nextChar();
		
		if(ch == '@'){
			nextChar();
			scanIdentifier();
			return false;
		}
		
		scanIdentifier();
		
		Token tok = atwords.getToken(literal());
		
		if(null == tok){
			skipWhitespaces();
			
			//@tagname { ... }
			if(null == tok && ch == '('){
				tok = Token.TAG;
			}
		}

		if(null != tok){
			startToken(startPos,tok);
			return true;
		}
		
		return false;
	}

	protected final void scanQuotedText() {
		startLiteral();
		for(;;) {
			if(ch == '`' && peekChar(1) == '`' && peekChar(2) == '`') {
				endLiteral();
				nextChars(2);
				break;
			}
			if(ch == EOI) {
				reportError("Illegal raw text, must be closed by '```' at {0}", describePosition());
			}
			nextChar();
		}
		nextChar();
	}
	
	public final void scanQuotedIdentifier(){
		char quoteChar = ch;
		startLiteral();
		
        for (;;) {
        	nextChar();
        	if(ch == EOI || ch == quoteChar){
        		break;
        	}
        }

        endLiteral();
		if(ch != quoteChar){
			reportError("Illegal quoted identifier, expected '{0}' character at {1}",quoteChar,describePosition());
		}
		nextChar();
	}
	
    public final void scanIdentifier() {
        if (!isFirstIdentifierChar(ch)) {
            reportError("Illegal identifier character '{0}' at {1}",ch,describePosition());
        }

        startLiteral();
        
        for (;;) {
        	nextChar();

            if (!isIdentifierChar(ch)) {
                break;
            }

            continue;
        }

        endLiteral();
    }
    
    private final void scanOperator() {
    	startToken();
        switch (ch) {
            case '+':
                nextChar();
                _token = Token.PLUS;
                break;
            case '-':
                nextChar();
                if (ch == '>') {
                	nextChar();
                	if (ch == '>') {
                		nextChar();
                		_token = Token.SUBGTGT;
					} else {
						_token = Token.SUBGT;
					}
				} else {
					_token = Token.SUB;
				}
                break;
            case '*':
                nextChar();
                _token = Token.STAR;
                break;
            case '/':
                nextChar();
                _token = Token.SLASH;
                break;
            case '&':
                nextChar();
                if (ch == '&') {
                    nextChar();
                    _token = Token.AMPAMP;
                } else {
                    _token = Token.AMP;
                }
                break;
            case '|':
                nextChar();
                if (ch == '|') {
                    nextChar();
                    _token = Token.BARBAR;
                } else {
                    _token = Token.BAR;
                }
                break;
            case '^':
                nextChar();
                _token = Token.CARET;
                break;
            case '%':
                nextChar();
                _token = Token.PERCENT;
                break;
            case '=':
                nextChar();
                if (ch == '=') {
                    nextChar();
                    _token = Token.EQEQ;
                } else {
                    _token = Token.EQ;
                }
                break;
            case '>':
                nextChar();
                if (ch == '=') {
                    nextChar();
                    _token = Token.GTEQ;
                } else if (ch == '>') {
                    nextChar();
                    _token = Token.GTGT;
                } else {
                    _token = Token.GT;
                }
                break;
            case '<':
                nextChar();
                if (ch == '=') {
                    nextChar();
                    if (ch == '>') {
                        _token = Token.LTEQGT;
                        nextChar();
                    } else {
                        _token = Token.LTEQ;
                    }
                } else if (ch == '>') {
                    nextChar();
                    _token = Token.LTGT;
                } else if (ch == '<') {
                    nextChar();
                    _token = Token.LTLT;
                } else {
                    _token = Token.LT;
                }
                break;
            case '!':
                nextChar();
                if (ch == '=') {
                    nextChar();
                    _token = Token.BANGEQ;
                } else if (ch == '>') {
                    nextChar();
                    _token = Token.BANGGT;
                } else if (ch == '<') {
                    nextChar();
                    _token = Token.BANGLT;
                } else {
                    _token = Token.BANG;
                }
                break;
            case '?':
                nextChar();
                _token = Token.QUES;
                break;
            case '~':
                nextChar();
                _token = Token.TILDE;
                break;
            default:
                throw new SqlParserException("TODO");
        }
    }
    
    protected final void setIdentifierToken(){
        Token tok = keywords.getToken(literal());
        if (tok != null) {
            _token = tok;
        } else {
        	_token = Token.IDENTIFIER;
        }
    }
    
    public final boolean hasLiteral(){
    	return literalStart < literalEnd && literalStart >= 0;
    }
    
    public final boolean isToken(Token token) {
        return _token == token;
    }
    
    public final boolean isKeyword(){
    	return _token != null && _token.isKeyword();
    }
    
    public final boolean isIdentifier(){
    	return null != _token && _token.isIdentifier();
    }
    
    public final boolean isLiteral(){
    	return _token == Token.LITERAL_CHARS ||
    		   _token == Token.LITERAL_INT   ||
    		   _token == Token.LITERAL_FLOAT || 
    		   _token == Token.LITERAL_HEX;
    }
    
    /**
     * Returns <code>true</code> if current token is {@link Token#IDENTIFIER} 
     * 
     * and the identifier literal equals to the given string.
     */
    public final boolean isIdentifier(String string){
    	return _token == Token.IDENTIFIER && Strings.equalsIgnoreCase(this.literal(), string);
    }
    
    protected final void reportError(String message) {
    	throw new SqlParserException(message);
    }
	
    protected final void reportError(String message, Object... args) {
    	throw new SqlParserException(Strings.format(message, args));
    }
    
    /**
     * Returns a string describe current position.
     */
    public final String describePosition(){
    	return describePosition(pos);
    }
    
    /**
     * Returns a string describe the given position.
     */
    public final String describePosition(int pos){
    	int fromIndex;
    	int endIndex;
    	
    	if(pos > chars.length() - 5){
    		fromIndex = Math.max(pos - 15, 0);
    		endIndex  = chars.length()-1;
    	}else{
        	fromIndex = pos;
        	endIndex  = Math.min(pos + 20, chars.length() - 1);
    	}
    	
    	StringBuilder sb = new StringBuilder();

    	sb.append("position ").append(pos).append(", \" ");
    	sb.append(substring(fromIndex,endIndex));
    	if(endIndex < chars.length() - 1){
    		sb.append("...");
    	}
    	sb.append(" \"");
    	
    	return sb.toString();
    }
	
	@Override
    public String toString() {
		StringBuilder sb = new StringBuilder();

        int len = tokenText().length();

        sb.append("Token : ");
		sb.append(_token).append(" , Pos : ");


        int index = pos;
		if(index >=0 && index <= chars.length()){

            if(index == chars.length()) {
                index--;
            }
			
			if(index > 0){
				sb.append(substring(0,index - len ));
			}

            sb.append("--> ");

            sb.append(tokenText());

			sb.append(" <-- ");
			
			if(index < chars.length() - 1){
				sb.append(substring(index));
			}
		}

		return sb.toString();
	}
	
	/**
	 * Creates a save point to save current state of this lexer.
	 */
	public final void createSavePoint(){
		this.sp = new SavePoint(this);
	}
	
	/**
	 * Restores the state of this lexer from current save point. 
	 */
	public final void restoreSavePoint(){
		this.sp.restore();
		this.sp = null;
	}
	
	/**
	 * Deletes current save point.
	 */
	public final void deleteSavePoint(){
		this.sp = null;
	}
	
    private static class SavePoint {
        private final int     pos;
        private final char    ch;
        private final int     markPos;
        private final int     textPos;
        private final int     literalStart;
        private final int     literalEnd;
        private final String  literal;
        private final Token   prevToken;
        private final Token   token;
        private final int     tokenStart;
        private final Lexer   lexer;
        private final boolean eof;
        
        private SavePoint(Lexer lexer){
        	this.lexer = lexer;
            this.pos = lexer.pos;
            this.ch = lexer.ch;
            this.markPos = lexer.markPos;
            this.textPos = lexer.textPos;
            this.literalStart = lexer.literalStart;
            this.literalEnd   = lexer.literalEnd;
            this.literal = lexer.literal;
            this.token = lexer._token;
            this.prevToken = lexer._prevToken;
            this.tokenStart = lexer.tokenStart;
            this.eof = lexer.eof;
        }
        
        private void restore(){
            lexer.pos = this.pos;
            lexer.ch = this.ch;
            lexer.markPos = this.markPos;
            lexer.textPos = this.textPos;
            lexer.literalStart = this.literalStart;
            lexer.literalEnd   = this.literalEnd;
            lexer.literal = this.literal;
            lexer._token = this.token;
            lexer._prevToken = this.prevToken;
            lexer.tokenStart = this.tokenStart;
            lexer.eof = this.eof;
        }
    }
}