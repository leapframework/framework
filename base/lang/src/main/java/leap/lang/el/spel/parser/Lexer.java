package leap.lang.el.spel.parser;

import static leap.lang.el.spel.parser.CharTypes.isFirstIdentifierChar;
import static leap.lang.el.spel.parser.CharTypes.isIdentifierChar;
import static leap.lang.el.spel.parser.Token.COLON;
import static leap.lang.el.spel.parser.Token.COLONEQ;
import static leap.lang.el.spel.parser.Token.COMMA;
import static leap.lang.el.spel.parser.Token.EOF;
import static leap.lang.el.spel.parser.Token.ERROR;
import static leap.lang.el.spel.parser.Token.LBRACE;
import static leap.lang.el.spel.parser.Token.LBRACKET;
import static leap.lang.el.spel.parser.Token.LPAREN;
import static leap.lang.el.spel.parser.Token.RBRACE;
import static leap.lang.el.spel.parser.Token.RBRACKET;
import static leap.lang.el.spel.parser.Token.RPAREN;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import leap.lang.el.ElParseException;
import leap.lang.el.spel.parser.CharTypes;
import leap.lang.el.spel.parser.SymbolTable;


/**
 * 
 * @author shaojin.wensj
 * 
 */
//take from alibaba simple el project
class Lexer {
    public final static byte EOI = 0x1A;
    
    protected final char[] buf;
    protected int bp;
    protected int buflen;
    protected int eofPos;

    /**
     * The current character.
     */
    protected char ch;

    /**
     * The token's position, 0-based offset from beginning of text.
     */
    protected int pos;

    /**
     * A character buffer for literals.
     */
    protected char[] sbuf;
    protected int sp;

    protected int np;

    protected SymbolTable symbolTable = new SymbolTable();

    /**
     * The token, set by nextToken().
     */
    private Token token;

    protected Keywords keywods = Keywords.DEFAULT;

    protected final static ThreadLocal<char[]> sbufRef = new ThreadLocal<char[]>();

    protected String stringVal;

    public Lexer(String input) {
        this(input.toCharArray(), input.length());
    }

    public Lexer(char[] input, int inputLength) {
        sbuf = sbufRef.get(); // new char[1024];
        if (sbuf == null) {
            sbuf = new char[1024];
            sbufRef.set(sbuf);
        }

        eofPos = inputLength;

        if (inputLength == input.length) {
            if (input.length > 0 && isWhitespace(input[input.length - 1])) {
                inputLength--;
            } else {
                char[] newInput = new char[inputLength + 1];
                System.arraycopy(input, 0, newInput, 0, input.length);
                input = newInput;
            }
        }
        buf = input;
        buflen = inputLength;
        buf[buflen] = EOI;
        bp = -1;

        scanChar();
    }
    
    public boolean eof() {
        return pos >= eofPos - 1;
    }
    
    public String rest() {
        if(eof()) {
            return null;
        }else{
            return new String(Arrays.copyOfRange(buf, pos, buf.length));
        }
    }

    protected final void scanChar() {
        ch = buf[++bp];
    }

    /**
     * Report an error at the given position using the provided arguments.
     */
    protected void lexError(int pos, String key, Object... args) {
        token = ERROR;
    }

    /**
     * Report an error at the current token position using the provided
     * arguments.
     */
    private void lexError(String key, Object... args) {
        lexError(pos, key, args);
    }

    /**
     * Return the current token, set by nextToken().
     */
    public final Token token() {
        return token;
    }
    
    public final void nextText() {
    	
    }

    public final void nextToken() {
        sp = 0;

        for (;;) {
            pos = bp;

            if (isWhitespace(ch)) {
                scanChar();
                continue;
            }

            if (isFirstIdentifierChar(ch)) {
                scanIdent();
                return;
            }

            if (ch == ',') {
                scanChar();
                token = COMMA;
                return;
            }

            switch (ch) {
            case '0':
                if (buf[bp + 1] == 'x') {
                    scanChar();
                    scanChar();
                    scanHexNumber();
                } else {
                    scanNumber();
                }
                return;
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                scanNumber();
                return;
            case '(':
                scanChar();
                token = LPAREN;
                return;
            case ')':
                scanChar();
                token = RPAREN;
                return;
            case '[':
                scanChar();
                token = LBRACKET;
                return;
            case ']':
                scanChar();
                token = RBRACKET;
                return;
            case '{':
                scanChar();
                token = LBRACE;
                return;
            case '}':
                scanChar();
                token = RBRACE;
                return;
            case ':':
                scanChar();
                if (ch == '=') {
                    scanChar();
                    token = COLONEQ;
                } else {
                    token = COLON;
                }
                return;
            case '.':
                scanChar();
                token = Token.DOT;
                return;
            case '\'':
                scanString();
                return;
            case '\"':
                scanAlias();
                return;
            case '*':
                scanChar();
                token = Token.STAR;
                return;
            case '?':
                scanChar();
                token = Token.QUES;
                return;
            case ';':
                scanChar();
                token = Token.SEMI;
                return;
            default:
                if (Character.isLetter(ch)) {
                    scanIdent();
                    return;
                }

                if (isOperator(ch)) {
                    scanOperator();
                    return;
                }

                if (bp == buflen || ch == EOI && bp + 1 == buflen) { // JLS
                    token = EOF;
                    pos = bp = eofPos;
                } else {
                    lexError("illegal.char", String.valueOf((int) ch));
                    scanChar();
                }

                return;
            }
        }

    }
    
    public static final boolean isWhitespace(char ch) {
        // 专门调整了判断顺序
        return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '\f' || ch == '\b';
    }

    private final void scanOperator() {
        switch (ch) {
        case '+':
            scanChar();
            if (ch == '=') {
                scanChar();
                token = Token.PLUSEQ;
            } else if (ch == '+') {
                    scanChar();
                    token = Token.PLUSPLUS;
            } else {
                token = Token.PLUS;
            }
            break;
        case '-':
            scanChar();
            if (ch == '=') {
                scanChar();
                token = Token.SUBEQ;
            } else if (ch == '-') {
                scanChar();
                token = Token.SUBSUB;
            } else {
                token = Token.SUB;
            }
            break;
        case '*':
            scanChar();
            token = Token.STAR;
            break;
        case '/':
            scanChar();
            token = Token.SLASH;
            break;
        case '&':
            scanChar();
            if (ch == '&') {
                scanChar();
                token = Token.AMPAMP;
            } else {
                token = Token.AMP;
            }
            break;
        case '|':
            scanChar();
            if (ch == '|') {
                scanChar();
                token = Token.BARBAR;
            } else {
                token = Token.BAR;
            }
            break;
        case '^':
            scanChar();
            token = Token.CARET;
            break;
        case '%':
            scanChar();
            token = Token.PERCENT;
            break;
        case '=':
            scanChar();
            if (ch == '=') {
                scanChar();
                token = Token.EQEQ;
            } else {
                token = Token.EQ;
            }
            break;
        case '>':
            scanChar();
            if (ch == '=') {
                scanChar();
                token = Token.GTEQ;
            } else if (ch == '>') {
                scanChar();
                token = Token.GTGT;
            } else {
                token = Token.GT;
            }
            break;
        case '<':
            scanChar();
            if (ch == '=') {
                scanChar();
                if (ch == '>') {
                    token = Token.LTEQGT;
                    scanChar();
                } else {
                    token = Token.LTEQ;
                }
            } else if (ch == '>') {
                scanChar();
                token = Token.LTGT;
            } else if (ch == '<') {
                scanChar();
                token = Token.LTLT;
            } else {
                token = Token.LT;
            }
            break;
        case '!':
            scanChar();
            if (ch == '=') {
                scanChar();
                token = Token.BANGEQ;
            } else if (ch == '>') {
                scanChar();
                token = Token.BANGGT;
            } else if (ch == '<') {
                scanChar();
                token = Token.BANGLT;
            } else {
                token = Token.BANG;
            }
            break;
        case '?':
            scanChar();
            token = Token.QUES;
            break;
        case '~':
            scanChar();
            token = Token.TILDE;
            break;
        default:
            throw new ElParseException("TODO");
        }
    }

    protected void scanString() {
        np = bp;
        boolean hasSpecial = false;
        char ch;
        for (;;) {
            ch = buf[++bp];

            if (ch == '\'') {
                break;
            }

            if (ch == EOI) {
                throw new ElParseException("unclosed single-quote string");
            }

            //found escape char '\'
            if (ch == '\\') {
                if (!hasSpecial) {
                    hasSpecial = true;
                    
                    if (sp > sbuf.length) {
                        char[] newsbuf = new char[sp * 2];
                        System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
                        sbuf = newsbuf;
                    }
                    
                    System.arraycopy(buf, np + 1, sbuf, 0, sp);
                }

                ch = buf[++bp];
                
                //see http://docs.oracle.com/javase/specs/jls/se5.0/html/lexical.html#101089
                switch (ch) {
                case '"':
                    putChar('"');
                    break;
                case '\\':
                    putChar('\\');
                    break;
                case '/':
                    putChar('/');
                    break;
                case '\'':
                    putChar('\'');
                    break;
                case 'b':
                    putChar('\b');
                    break;
                case 'f':
                case 'F':
                    putChar('\f');
                    break;
                case 'n':
                    putChar('\n');
                    break;
                case 'r':
                    putChar('\r');
                    break;
                case 't':
                    putChar('\t');
                    break;
                case 'u':
                    char c1 = ch = buf[++bp];
                    char c2 = ch = buf[++bp];
                    char c3 = ch = buf[++bp];
                    char c4 = ch = buf[++bp];
                    int val = Integer.parseInt(new String(new char[] { c1, c2, c3, c4 }), 16);
                    putChar((char) val);
                    break;
                default:
                	/*
						OctalEscape:
						        \ OctalDigit
						        \ OctalDigit OctalDigit
						        \ ZeroToThree OctalDigit OctalDigit
						
						OctalDigit: one of
						        0 1 2 3 4 5 6 7
						
						ZeroToThree: one of
						        0 1 2 3
                	 */
                	if(CharTypes.isZeroToThree(ch)){
            			if(buf.length > bp + 2){
            				// \ ZeroToThree OctalDigit OctalDigit
            				char oc1 = buf[++bp];
            				char oc2 = buf[++bp];
            				int oval = Integer.parseInt(new String(new char[]{ch, oc1, oc2}), 8);
            				putChar((char)oval);
            				ch = oc2;
            				break;
            			}
                	}else if(CharTypes.isOctalDigit(ch)){
                		if(CharTypes.isOctalDigit(buf[bp+1])){
            				// \ OctalDigit OctalDigit
                			char oc1 = buf[++bp];
            				int oval = Integer.parseInt(new String(new char[]{ch, oc1}), 8);
            				putChar((char)oval);
            				ch = oc1;
            				break;
                		}else{
            				// \ OctalDigit
            				int oval = Integer.parseInt(new String(new char[]{ch}), 8);
            				putChar((char)oval);
            				break;
            			}
                	}
                    this.ch = ch;
                    throw new ElParseException("Unclosed single-quote string, incorrect octal escape char '" + ch + "'");
                }
                continue;
            }

            if (!hasSpecial) {
                sp++;
                continue;
            }

            if (sp == sbuf.length) {
                putChar(ch);
            } else {
                sbuf[sp++] = ch;
            }
        }

        if (!hasSpecial) {
            stringVal = new String(buf, np + 1, sp);
        } else {
            stringVal = new String(sbuf, 0, sp);
        }
        
        token = Token.LITERAL_STRING;
        this.ch = buf[++bp];
        
    }

    private final void scanAlias() {
        np = bp;
        boolean hasSpecial = false;
        char ch;
        for (;;) {
            ch = buf[++bp];

            if (ch == '\"') {
                break;
            }

            if (ch == '\\') {
                if (!hasSpecial) {
                    hasSpecial = true;
                    
                    if (sp >= sbuf.length) {
                        int newCapcity = sbuf.length * 2;
                        if (sp > newCapcity) {
                            newCapcity = sp;
                        }
                        char[] newsbuf = new char[newCapcity];
                        System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
                        sbuf = newsbuf;
                    }
                    
                    System.arraycopy(buf, np + 1, sbuf, 0, sp);
                }

                ch = buf[++bp];

                switch (ch) {
                case '"':
                    putChar('"');
                    break;
                case '\\':
                    putChar('\\');
                    break;
                case '/':
                    putChar('/');
                    break;
                case 'b':
                    putChar('\b');
                    break;
                case 'f':
                case 'F':
                    putChar('\f');
                    break;
                case 'n':
                    putChar('\n');
                    break;
                case 'r':
                    putChar('\r');
                    break;
                case 't':
                    putChar('\t');
                    break;
                case 'x':
                    char x1 = ch = buf[++bp];
                    char x2 = ch = buf[++bp];
                    
                    int x_val = digits[x1] * 16 + digits[x2];
                    char x_char = (char) x_val;
                    putChar(x_char);
                    break;
                case 'u':
                    char u1 = ch = buf[++bp];
                    char u2 = ch = buf[++bp];
                    char u3 = ch = buf[++bp];
                    char u4 = ch = buf[++bp];
                    int val = Integer.parseInt(new String(new char[] { u1, u2, u3, u4 }), 16);
                    putChar((char) val);
                    break;
                default:
                    this.ch = ch;
                    throw new ElParseException("unclosed string");
                }
                continue;
            }

            if (!hasSpecial) {
                sp++;
                continue;
            }

            if (sp == sbuf.length) {
                putChar(ch);
            } else {
                sbuf[sp++] = ch;
            }
        }
        
        if (!hasSpecial) {
            stringVal = new String(buf, np + 1, sp);
        } else {
            stringVal = new String(sbuf, 0, sp);
        }

        token = Token.LITERAL_STRING;
        this.ch = buf[++bp];
    }

    public void scanVariable() {
        final char first = ch;

        if (ch != '@' && ch != ':') {
            throw new ElParseException("illegal variable");
        }

        int hash = first;

        np = bp;
        sp = 1;
        char ch;
        if (buf[bp + 1] == '@') {
            hash = 31 * hash + '@';
            bp++;
            sp++;
        }
        for (;;) {
            ch = buf[++bp];

            if (!isIdentifierChar(ch)) {
                break;
            }

            hash = 31 * hash + ch;

            sp++;
            continue;
        }

        this.ch = buf[bp];

        stringVal = symbolTable.addSymbol(buf, np, sp, hash);
        Token tok = keywods.getToken(stringVal);
        if (tok != null) {
            token = tok;
        } else {
            token = Token.IDENTIFIER;
        }
    }

    public void scanIdent() {
        final char first = ch;

        final boolean firstFlag = isFirstIdentifierChar(first);
        if (!firstFlag) {
            throw new ElParseException("illegal identifier");
        }

        int hash = first;

        np = bp;
        sp = 1;
        char ch;
        for (;;) {
            ch = buf[++bp];

            if (!isIdentifierChar(ch)) {
                break;
            }

            hash = 31 * hash + ch;

            sp++;
            continue;
        }

        this.ch = buf[bp];

        stringVal = symbolTable.addSymbol(buf, np, sp, hash);
        Token tok = keywods.getToken(stringVal);
        if (tok != null) {
            token = tok;
        } else {
            token = Token.IDENTIFIER;
        }
    }

    public void scanNumber() {
        np = bp;

        if (ch == '-') {
            sp++;
            ch = buf[++bp];
        }

        for (;;) {
            if (ch >= '0' && ch <= '9') {
                sp++;
            } else {
                break;
            }
            ch = buf[++bp];
        }

        boolean isDouble = false;
        boolean isFloat  = false;

        if (ch == '.') {
            sp++;
            ch = buf[++bp];
            isDouble = true;

            for (;;) {
                if (ch >= '0' && ch <= '9') {
                    sp++;
                } else {
                    break;
                }
                ch = buf[++bp];
            }
            
            if(ch == 'f'){
            	isFloat = true;
            }
        }

        if (ch == 'e' || ch == 'E') {
            sp++;
            ch = buf[++bp];

            if (ch == '+' || ch == '-') {
                sp++;
                ch = buf[++bp];
            }

            for (;;) {
                if (ch >= '0' && ch <= '9') {
                    sp++;
                } else {
                    break;
                }
                ch = buf[++bp];
            }

            isDouble = true;
            
            if(ch == 'f'){
            	isFloat = true;
            }
        }

        if (isDouble) {
        	if(isFloat){
        		token = Token.LITERAL_FLOAT;	
        	}else{
        		token = Token.LITERAL_DOUBLE;
        	}
        } else {
            token = Token.LITERAL_INT;
        }
    }

    public void scanHexNumber() {
        np = bp;

        if (ch == '-') {
            sp++;
            ch = buf[++bp];
        }

        for (;;) {
            if (CharTypes.isHex(ch)) {
                sp++;
            } else {
                break;
            }
            ch = buf[++bp];
        }
        
        if(ch == 'L' || ch == 'l'){
        	sp++;
        	ch = buf[++bp];
        	token = Token.LITERAL_HEX_LONG;
        }else{
        	token = Token.LITERAL_HEX;	
        }
    }

    public String hexStringWithPrefix() {
    	if(token == Token.LITERAL_HEX_LONG){
    		return new String(buf, np - 2, sp + 1);	
    	}else{
    		return new String(buf, np - 2, sp + 2);
    	}
    }

    public final boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * Append a character to sbuf.
     */
    protected final void putChar(char ch) {
        if (sp == sbuf.length) {
            char[] newsbuf = new char[sbuf.length * 2];
            System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
            sbuf = newsbuf;
        }
        sbuf[sp++] = ch;
    }

    /**
     * Return the current token's position: a 0-based offset from beginning of
     * the raw input stream (before unicode translation)
     */
    public final int pos() {
        return pos;
    }

    /**
     * The value of a literal token, recorded as a string. For integers, leading
     * 0x and 'l' suffixes are suppressed.
     */
    public final String stringVal() {
        return stringVal;
    }

    private boolean isOperator(char ch) {
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
        case '/':
        case ';':
            return true;
        default:
            return false;
        }
    }

    private static final long MULTMIN_RADIX_TEN = Long.MIN_VALUE / 10;
    private static final long N_MULTMAX_RADIX_TEN = -Long.MAX_VALUE / 10;

    private final static int[] digits = new int[(int) '9' + 1];

    static {
        for (int i = '0'; i <= '9'; ++i) {
            digits[i] = i - '0';
        }
    }

    public Number integerValue() throws NumberFormatException {
        long result = 0;
        boolean negative = false;
        int i = np, max = np + sp;
        long limit;
        long multmin;
        int digit;

        if (buf[np] == '-') {
            negative = true;
            limit = Long.MIN_VALUE;
            i++;
        } else {
            limit = -Long.MAX_VALUE;
        }
        multmin = negative ? MULTMIN_RADIX_TEN : N_MULTMAX_RADIX_TEN;
        if (i < max) {
            digit = digits[buf[i++]];
            result = -digit;
        }
        while (i < max) {
            // Accumulating negatively avoids surprises near MAX_VALUE
            digit = digits[buf[i++]];
            if (result < multmin) {
                return new BigInteger(numberString());
            }
            result *= 10;
            if (result < limit + digit) {
                return new BigInteger(numberString());
            }
            result -= digit;
        }

        if (negative) {
            if (i > np + 1) {
                if (result >= Integer.MIN_VALUE) {
                    return (int) result;
                }
                return result;
            } else { /* Only got "-" */
                throw new NumberFormatException(numberString());
            }
        } else {
            result = -result;
            if (result <= Integer.MAX_VALUE) {
                return (int) result;
            }
            return result;
        }
    }

    public final String numberString() {
        return new String(buf, np, sp);
    }

    public BigDecimal decimalValue() {
        return new BigDecimal(buf, np, sp);
    }
}
