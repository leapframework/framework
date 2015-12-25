package leap.lang.el.spel.parser;

import static leap.lang.el.spel.ast.AstBinary.ADD_ASSIGN;
import static leap.lang.el.spel.ast.AstBinary.AND;
import static leap.lang.el.spel.ast.AstBinary.ASSIGN;
import static leap.lang.el.spel.ast.AstBinary.BIT_AND;
import static leap.lang.el.spel.ast.AstBinary.BIT_OR;
import static leap.lang.el.spel.ast.AstBinary.BIT_XOR;
import static leap.lang.el.spel.ast.AstBinary.DIV;
import static leap.lang.el.spel.ast.AstBinary.EQ;
import static leap.lang.el.spel.ast.AstBinary.GE;
import static leap.lang.el.spel.ast.AstBinary.GT;
import static leap.lang.el.spel.ast.AstBinary.INSTANCE_OF;
import static leap.lang.el.spel.ast.AstBinary.LE;
import static leap.lang.el.spel.ast.AstBinary.LSHIFT;
import static leap.lang.el.spel.ast.AstBinary.LT;
import static leap.lang.el.spel.ast.AstBinary.MOD;
import static leap.lang.el.spel.ast.AstBinary.MUL;
import static leap.lang.el.spel.ast.AstBinary.NE;
import static leap.lang.el.spel.ast.AstBinary.OR;
import static leap.lang.el.spel.ast.AstBinary.RSHIFT;
import static leap.lang.el.spel.ast.AstBinary.SUB;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import leap.lang.el.DefaultElParseContext;
import leap.lang.el.ElParseContext;
import leap.lang.el.ElParseException;
import leap.lang.el.spel.ast.AstBinary;
import leap.lang.el.spel.ast.AstBoolean;
import leap.lang.el.spel.ast.AstChoice;
import leap.lang.el.spel.ast.AstExpr;
import leap.lang.el.spel.ast.AstFunction;
import leap.lang.el.spel.ast.AstIdentifier;
import leap.lang.el.spel.ast.AstItem;
import leap.lang.el.spel.ast.AstMethod;
import leap.lang.el.spel.ast.AstNull;
import leap.lang.el.spel.ast.AstNumber;
import leap.lang.el.spel.ast.AstProperty;
import leap.lang.el.spel.ast.AstString;
import leap.lang.el.spel.ast.AstType;
import leap.lang.el.spel.ast.AstUnary;

//take from alibaba simple el project
public class Parser {
	
	private static final ElParseContext EMPTY_CONTEXT = new DefaultElParseContext();
	
	private static final String T = "T";
	
	public static AstExpr parse(String text) {
		return new Parser(text).expr();
	}
	
	public static AstExpr parse(ElParseContext context, String text) {
		return new Parser(context,text).parse();
	}

	protected final ElParseContext context;
    protected final Lexer 	 	   lexer;
    
    private int quesCounter;
    
    public Parser(String input){
    	this(EMPTY_CONTEXT,input);
    }
    
    public Parser(ElParseContext context, String input){
    	this.context = context;
    	this.lexer = new Lexer(input);
    	this.lexer.nextToken();
    }

    public AstExpr parse() {
        return parse(false);
    }
    
    public AstExpr parse(boolean allowRest) {
        AstExpr expr = expr();
        
        if(!eof() && !allowRest) {
            throw new ElParseException("multi-expressions not allowed");
        }
        
        return expr;
    }
    
    protected AstExpr expr() {
        AstExpr expr = primary();

        if (lexer.token() == Token.COMMA) {
            return expr;
        }

        return exprRest(expr);
    }
    
    public boolean eof() {
        return lexer.eof();
    }
    
    public String rest() {
        return lexer.rest();
    }
    
    protected AstExpr exprRest(AstExpr expr) {
        expr = multiplicativeRest(expr);
        expr = additiveRest(expr);
        expr = shiftRest(expr);
        expr = relationalRest(expr);
        expr = equalityRest(expr);
        expr = bitAndRest(expr);
        expr = bitXorRest(expr);
        expr = bitOrRest(expr);
        expr = andRest(expr);
        expr = orRest(expr);
        expr = choiceRest(expr);
        expr = assignRest(expr);
        return expr;
    }
    
    protected Lexer lexer() {
        return lexer;
    }

    protected void accept(Token token) {
        if (lexer.token() == token) {
            lexer.nextToken();
        } else {
            throw new ElParseException("syntax error, expect " + token + ", actual " + lexer.token());
        }
    }

    // ^
    protected final AstExpr bitXor() {
        AstExpr expr = bitAnd();
        return bitXorRest(expr);
    }

    // ^
    protected AstExpr bitXorRest(AstExpr expr) {
        if (lexer.token() == Token.CARET) {
            lexer.nextToken();
            AstExpr rightExp = bitAnd();
            expr = new AstBinary(expr,BIT_XOR, rightExp);
            expr = bitXorRest(expr);
        }

        return expr;
    }

    protected final AstExpr multiplicative() {
        AstExpr expr = primary();
        return multiplicativeRest(expr);
    }

    protected AstExpr multiplicativeRest(AstExpr expr) {
        if (lexer.token() == Token.STAR) {
            lexer.nextToken();
            AstExpr rightExp = primary();
            expr = new AstBinary(expr,MUL, rightExp);
            expr = multiplicativeRest(expr);
        } else if (lexer.token() == Token.SLASH) {
            lexer.nextToken();
            AstExpr rightExp = primary();
            expr = new AstBinary(expr,DIV, rightExp);
            expr = multiplicativeRest(expr);
        } else if (lexer.token() == Token.PERCENT) {
            lexer.nextToken();
            AstExpr rightExp = primary();
            expr = new AstBinary(expr,MOD, rightExp);
            expr = multiplicativeRest(expr);
        }
        return expr;
    }

    // &
    protected final AstExpr bitAnd() {
        AstExpr expr = equality();
        return orRest(expr);
    }

    protected final AstExpr bitAndRest(AstExpr expr) {
        while (lexer.token() == Token.AMP) {
            lexer.nextToken();
            AstExpr rightExp = equality();
            expr = new AstBinary(expr,BIT_AND, rightExp);
        }
        return expr;
    }

    // |
    protected final AstExpr bitOr() {
        AstExpr expr = bitXor();
        return orRest(expr);
    }

    // |
    protected final AstExpr bitOrRest(AstExpr expr) {
        while (lexer.token() == Token.BAR) {
            lexer.nextToken();
            AstExpr rightExp = bitXor();
            expr = new AstBinary(expr,BIT_OR, rightExp);
        }
        return expr;
    }

    protected final AstExpr equality() {
        AstExpr expr = relational();
        return equalityRest(expr);
    }

    protected final AstExpr equalityRest(AstExpr expr) {
        AstExpr rightExp;
        if (lexer.token() == Token.EQEQ) {
            lexer.nextToken();
            rightExp = relational();

            expr = new AstBinary(expr,EQ, rightExp);
        } else if (lexer.token() == Token.BANGEQ) {
            lexer.nextToken();
            rightExp = relational();

            expr = new AstBinary(expr,NE, rightExp);
        }

        return expr;
    }

    protected final AstExpr additive() {
        AstExpr expr = multiplicative();
        return additiveRest(expr);
    }

    // + -
    protected AstExpr additiveRest(AstExpr expr) {
        if (lexer.token() == Token.PLUS) {
            lexer.nextToken();
            AstExpr rightExp = multiplicative();

            expr = new AstBinary(expr, AstBinary.ADD, rightExp);
            expr = additiveRest(expr);
        } else if (lexer.token() == Token.SUB) {
            lexer.nextToken();
            AstExpr rightExp = multiplicative();

            expr = new AstBinary(expr,SUB, rightExp);
            expr = additiveRest(expr);
        }

        return expr;
    }

    protected final AstExpr shift() {
        AstExpr expr = additive();
        return shiftRest(expr);
    }

    protected AstExpr shiftRest(AstExpr expr) {
        if (lexer.token() == Token.LTLT) {
            lexer.nextToken();
            AstExpr rightExp = additive();

            expr = new AstBinary(expr, LSHIFT, rightExp);
            expr = shiftRest(expr);
        } else if (lexer.token() == Token.GTGT) {
            lexer.nextToken();
            AstExpr rightExp = additive();

            expr = new AstBinary(expr, RSHIFT, rightExp);
            expr = shiftRest(expr);
        }

        return expr;
    }

    protected final AstExpr and() {
        AstExpr expr = bitOr();
        return andRest(expr);
    }

    // &&
    protected final AstExpr andRest(AstExpr expr) {
        if (lexer.token() == Token.AMPAMP) {
            lexer.nextToken();
            AstExpr rightExp = bitOr();

            expr = new AstBinary(expr,AND, rightExp);
            expr = andRest(expr);
        }

        return expr;
    }

    protected final AstExpr or() {
        AstExpr expr = and();
        return orRest(expr);
    }

    // ||
    protected final AstExpr orRest(AstExpr expr) {
        if (lexer.token() == Token.BARBAR) {
            lexer.nextToken();
            AstExpr rightExp = and();

            expr = new AstBinary(expr,OR, rightExp);
            expr = orRest(expr);
        }

        return expr;
    }

    protected final AstExpr conditional() {
        AstExpr expr = or();
        return choiceRest(expr);
    }

    // ?:
    protected final AstExpr choiceRest(AstExpr expr) {
        if (lexer.token() == Token.QUES) {
        	quesCounter++;
            
        	lexer.nextToken();
            AstExpr trueExpr = expr();
            accept(Token.COLON);
            
            quesCounter--;
            
            AstExpr falseExpr = expr();
            expr = new AstChoice(expr, trueExpr, falseExpr);

            expr = choiceRest(expr);
        }
        return expr;
    }

    protected final AstExpr assign() {
        AstExpr expr = conditional();
        return assignRest(expr);
    }

    // = += -= *= /= %= &= |= ^= ~= <<= >>= >>>=
    protected final AstExpr assignRest(AstExpr expr) {
        if (lexer.token() == Token.EQ) {
            lexer.nextToken();
            AstExpr rightExp = conditional();
            expr = new AstBinary(expr,ASSIGN, rightExp);
            expr = assignRest(expr);
        }

        if (lexer.token() == Token.PLUSEQ) {
            lexer.nextToken();
            AstExpr rightExp = conditional();
            expr = new AstBinary(expr,ADD_ASSIGN, rightExp);

            expr = assignRest(expr);
        }

        return expr;
    }

    protected final AstExpr relational() {
        AstExpr expr = shift();

        return relationalRest(expr);
    }

    public AstExpr relationalRest(AstExpr expr) {
        AstExpr rightExp;
        if (lexer.token() == Token.LT) {
            lexer.nextToken();
            rightExp = shift();

            expr = new AstBinary(expr, LT, rightExp);
        } else if (lexer.token() == Token.LTEQ) {
            lexer.nextToken();
            rightExp = shift();

            expr = new AstBinary(expr, LE, rightExp);
        } else if (lexer.token() == Token.GT) {
            lexer.nextToken();
            rightExp = shift();

            expr = new AstBinary(expr,GT, rightExp);
        } else if (lexer.token() == Token.GTEQ) {
            lexer.nextToken();
            rightExp = shift();

            expr = new AstBinary(expr,GE, rightExp);
        } else if (lexer.token() == Token.INSTNACEOF) {
            lexer.nextToken();
            rightExp = shift();

            expr = new AstBinary(expr,INSTANCE_OF, rightExp);
        }

        return expr;
    }

    protected final AstExpr name() throws ElParseException {
        if (lexer.token() != Token.IDENTIFIER) {
            throw new ElParseException("error : " + lexer.token());
        }

        String identName = lexer.stringVal();

        lexer.nextToken();

        AstExpr name = new AstIdentifier(context,identName);

        while (lexer.token() == Token.DOT) {
            lexer.nextToken();

            if (lexer.token() != Token.IDENTIFIER) {
                throw new ElParseException("error : " + lexer.token());
            }

            name = new AstProperty(context, name, lexer.stringVal());
            lexer.nextToken();
        }

        return name;
    }

    public AstExpr primary() {
        AstExpr primaryExpr = null;

        final Token tok = lexer.token();

        switch (tok) {
            case LPAREN:
                lexer.nextToken();
                primaryExpr = expr();
                accept(Token.RPAREN);
                break;
            case IDENTIFIER:
                lexer.nextToken();
                primaryExpr = new AstIdentifier(context,lexer.stringVal());	
                break;
            case PLUSPLUS:
                lexer.nextToken();
                primaryExpr = expr();
                primaryExpr = new AstUnary(primaryExpr, AstUnary.PRE_PLUSPLUS);
                break;
            case SUBSUB:
                lexer.nextToken();
                primaryExpr = expr();
                primaryExpr = new AstUnary(primaryExpr, AstUnary.PRE_SUBSUB);
                break;
            case BANG:
                lexer.nextToken();
                primaryExpr = primary();
                primaryExpr = new AstUnary(primaryExpr, AstUnary.NOT);
                break;
            case TRUE:
                primaryExpr = new AstBoolean(true);
                lexer.nextToken();
                break;
            case FALSE:
                primaryExpr = new AstBoolean(false);
                lexer.nextToken();
                break;
            case LITERAL_INT:
                primaryExpr = new AstNumber(lexer.integerValue());
                lexer.nextToken();
                break;
            case LITERAL_FLOAT:
                primaryExpr = new AstNumber(lexer.decimalValue().floatValue());
                lexer.nextToken();
                break;
            case LITERAL_DOUBLE:
                primaryExpr = new AstNumber(lexer.decimalValue().doubleValue());
                lexer.nextToken();
                break;
            case LITERAL_STRING:
                primaryExpr = new AstString(lexer.stringVal());
                lexer.nextToken();
                break;
            case SUB:
                lexer.nextToken();
                switch (lexer.token()) {
                    case LITERAL_INT:
                        Number integerValue = lexer.integerValue();
                        if (integerValue instanceof Integer) {
                            int intVal = ((Integer) integerValue).intValue();
                            if (intVal == Integer.MIN_VALUE) {
                                integerValue = Long.valueOf(((long) intVal) * -1);
                            } else {
                                integerValue = Integer.valueOf(intVal * -1);
                            }
                        } else if (integerValue instanceof Long) {
                            long longVal = ((Long) integerValue).longValue();
                            if (longVal == 2147483648L) {
                                integerValue = Integer.valueOf((int) (((long) longVal) * -1));
                            } else {
                                integerValue = Long.valueOf(longVal * -1);
                            }
                        } else {
                            integerValue = ((BigInteger) integerValue).negate();
                        }
                        primaryExpr = new AstNumber(integerValue);
                        lexer.nextToken();
                        break;
                    case LITERAL_FLOAT:
                        primaryExpr = new AstNumber(lexer.decimalValue().negate().floatValue());
                        lexer.nextToken();
                        break;
                    case LITERAL_DOUBLE:
                        primaryExpr = new AstNumber(lexer.decimalValue().negate().doubleValue());
                        lexer.nextToken();
                        break;
                    default:
                        primaryExpr = expr();
                        primaryExpr = new AstUnary(primaryExpr, AstUnary.MINUS);
                        break;
                }
                break;
            case PLUS:
                lexer.nextToken();
                switch (lexer.token()) {
                    case LITERAL_INT:
                        primaryExpr = new AstNumber(lexer.integerValue());
                        lexer.nextToken();
                        break;
                    case LITERAL_FLOAT:
                        primaryExpr = new AstNumber(lexer.decimalValue().floatValue());
                        lexer.nextToken();
                        break;
                    case LITERAL_DOUBLE:
                    	primaryExpr = new AstNumber(lexer.decimalValue().doubleValue());
                    	lexer.nextToken();
                    	break;
                    default:
                        primaryExpr = expr();
                        primaryExpr = new AstUnary(primaryExpr, AstUnary.PLUS);
                        break;
                }
                break;
            case NULL:
                primaryExpr = new AstNull();
                lexer.nextToken();
                break;
            case LITERAL_HEX:
            	primaryExpr = new AstNumber(Integer.decode(lexer.hexStringWithPrefix()));
            	lexer.nextToken();
            	break;
            case LITERAL_HEX_LONG:
            	primaryExpr = new AstNumber(Long.decode(lexer.hexStringWithPrefix()));
            	lexer.nextToken();
            	break;
            case ERROR:
            	throw new ElParseException("Error parse expression at pos " + lexer.pos() + ", character '" + lexer.buf[lexer.pos()] + "'");
            default:
                throw new ElParseException("Error. unsupported token : " + tok);
        }

        return primaryRest(primaryExpr);
    }

    public AstExpr primaryRest(AstExpr expr) throws ElParseException {
        if (expr == null) {
            throw new IllegalArgumentException("expr");
        }

        if (lexer.token() == Token.DOT) {
            lexer.nextToken();

            if (lexer.token() == Token.STAR) {
                lexer.nextToken();
                expr = new AstProperty(context, expr, "*");
            } else {
                if (lexer.token() != Token.IDENTIFIER) {
                    throw new ElParseException("error");
                }

                String name = lexer.stringVal();
                lexer.nextToken();

                if (lexer.token() == Token.LPAREN) {
                    lexer.nextToken();
                    
                    List<AstExpr> params = new ArrayList<>();

                    if (lexer.token() == Token.RPAREN) {
                        lexer.nextToken();
                    } else {
                        exprList(params);
                        accept(Token.RPAREN);
                    }
                    
                    expr = new AstMethod(expr,name, params.toArray(new AstExpr[params.size()]));
                } else {
                    expr = new AstProperty(context, expr, name);
                }
            }

            expr = primaryRest(expr);
        } else if (lexer.token() == Token.PLUSPLUS) {
            lexer.nextToken();
            expr = new AstUnary(expr, AstUnary.POST_PLUSPLUS);
            expr = primaryRest(expr);
        } else if (lexer.token() == Token.SUBSUB) {
            lexer.nextToken();
            expr = new AstUnary(expr, AstUnary.POST_SUBSUB);
            expr = primaryRest(expr);
        } else if (lexer.token() == Token.LBRACKET) {
            lexer.nextToken();
            AstExpr indexExpr = expr();
            accept(Token.RBRACKET);
            expr = new AstItem(expr, indexExpr);
            expr = primaryRest(expr);
        } else if (lexer.token() == Token.COLONEQ) {
            lexer.nextToken();
            AstExpr rightExp = primary();
            expr = new AstBinary(expr, ASSIGN, rightExp);
        } else {
        	if(lexer.token() == Token.COLON && quesCounter == 0){
            	//funcion with prefix
            	String prefix = lexer.stringVal();
            	
            	lexer.nextToken();
            	accept(Token.IDENTIFIER);
            	
            	String name = lexer.stringVal();
            	accept(Token.LPAREN);
            	
                List<AstExpr> params = new ArrayList<>();
                
                if (lexer.token() != Token.RPAREN) {
                    exprList(params);
                }

                accept(Token.RPAREN);
                return primaryRest(new AstFunction(context, prefix,name,params.toArray(new AstExpr[params.size()])));
        	}else if (lexer.token() == Token.LPAREN) {
                if (expr instanceof AstIdentifier) {
                    AstIdentifier identExpr = (AstIdentifier) expr;
                    String name = identExpr.getName();
                    lexer.nextToken();

                    List<AstExpr> params = new ArrayList<>();
                    
                    if (lexer.token() != Token.RPAREN) {
                        exprList(params);
                    }

                    accept(Token.RPAREN);
                    
                    return primaryRest(createFunction(context, name, params));
                }

                throw new ElParseException("not support token:");
            }
        }

        return expr;
    }

    protected final void exprList(Collection<AstExpr> c) throws ElParseException {
        if (lexer.token() == Token.RPAREN) {
            return;
        }

        if (lexer.token() == Token.EOF) {
            return;
        }

        AstExpr expr = expr();
        c.add(expr);

        while (lexer.token() == Token.COMMA) {
            lexer.nextToken();
            expr = expr();
            c.add(expr);
        }
    }

    protected AstExpr createFunction(ElParseContext context, String name, List<AstExpr> params) {
    	if(name.equals(T) & params.size() == 1){
    		AstExpr e = params.get(0);
    		if(e instanceof AstProperty || e instanceof AstIdentifier) {
    			return new AstType(context, e.toString());
    		}
    	}
		return new AstFunction(context,name,params.toArray(new AstExpr[params.size()]));
    }
}
