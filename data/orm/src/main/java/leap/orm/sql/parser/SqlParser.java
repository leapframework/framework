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

import leap.core.el.EL;
import leap.core.el.ExpressionLanguage;
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.annotation.Internal;
import leap.lang.exception.ParseException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.text.KeyValueParser;
import leap.orm.sql.Sql;
import leap.orm.sql.Sql.Scope;
import leap.orm.sql.Sql.Type;
import leap.orm.sql.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Internal
public class SqlParser extends SqlParserBase {
	protected static final Log log = LogFactory.get(SqlParser.class);
	
	private static final Token[] IF_STOP_TOKENS = new Token[]{Token.AT_ELSEIF,Token.AT_ELSE,Token.AT_ENDIF};
	
	public static Sql parse(String text) {
		Args.notEmpty(text,"sql text");
		return new SqlParser(new Lexer(Strings.trim(text))).sql();
	}
	
	public static SqlOrderBy parseOrderBy(String orderByExpression) {
		Args.notEmpty(orderByExpression,"orderByExpression");
		return new SqlParser(new Lexer(Strings.trim(orderByExpression)),null).orderBy();
	}
	
	public static List<String> split(String text){
		return new SqlParser(new Lexer(Strings.trim(text))).split();
	}
	
	public static List<Sql> parseAll(String text) {
		Args.notEmpty(text,"sql text");
		return new SqlParser(new Lexer(Strings.trim(text))).sqls();
	}
	
	public SqlParser(Lexer lexer){
		this(lexer,EL.getAppDefaultExpressionLanguage());
	}
	
	public SqlParser(Lexer lexer,ExpressionLanguage el){
		super(lexer,el);
	}
	
	protected SqlParser(SqlParser parent){
	    this(parent.lexer,parent.el);
		this.type  = parent.type;
		this.nodes = parent.nodes;
	}
	
	public List<String> split(){
		List<String> sqls = new ArrayList<String>();
		
		for(;;){
			StringBuilder sql = new StringBuilder();
			
			lexer.nextToken();
			while(lexer.token() == Token.SEMI){
				lexer.nextToken();
			}
			
			String text = lexer.text();
			if(null != text){
				sql.append(text);	
			}
			
			if(!lexer.isEOF()){
				for(;;){
					lexer.nextToken();
					
					text = lexer.text();
					if(null != text){
						sql.append(text);	
					}else if(lexer.token() == Token.DYNAMIC) {
					    lexer.nextChars(2);
					    sql.append("{?");
					    
					    for(;;) {
					        lexer.nextToken();
					        text = lexer.text();
					        if(null != text) {
					            sql.append(text);
					        }
					        if(lexer.isEOF() || lexer.isToken(Token.RBRACE)) {
					            break;
					        }
					    }
					}
					
					if(lexer.isEOS()){
						String sqlString = Strings.trim(sql.toString());
						if(!Strings.isEmpty(sqlString)){
							log.trace("Found sql statement : {}",sqlString);
							sqls.add(sqlString);
						}
						break;
					}
				}
			}
			
			if(lexer.isEOF()){
				break;
			}
		}
		
		return sqls;
	}
	
	public List<Sql> sqls(){
		List<Sql> sqls = new ArrayList<Sql>();
		
		for(;;){
			lexer.skipWhitespaces();
			
			lexer.nextToken();
			while(lexer.token() == Token.SEMI){ //skip ;;;;...
				lexer.nextToken();
			}
			
			if(!lexer.isEOF()){
				Sql sql = parseSql();
				if(!sql.isEmpty()){
					sqls.add(sql);
				}
			}
			
			if(lexer.isEOF()){
				break;
			}
		}
		
		return sqls;
	}
	
	public Sql sql(){
		lexer.nextToken();
		while(lexer.token() == Token.SEMI){ //skip ;;;;...
			lexer.nextToken();
		}
		
		Sql sql = parseSql();
		
		if(!lexer.isEOF()){
			throw new ParseException("Only one sql statement is allowed, error at " + lexer.describePosition());
		}
		
		if(sql.isEmpty()){
			throw new ParseException("The given sql contains comments only");
		}
		
		return sql;
	}
	
	public SqlOrderBy orderBy() {
		lexer.nextToken();
		expect(Token.ORDER);
		
		nodes = new ArrayList<>();
		
		parseOrderBy();
		
		return (SqlOrderBy)this.nodes.get(0);
	}
	
	protected Sql parseSql(){
		type  = null;
		nodes = new ArrayList<>();
		
		try {
			Token token = lexer.token();
			
			switch (token) {
				case SELECT:
					type = Type.SELECT;
					parseSelect();
					break;
				case INSERT:
					type = Type.INSERT;
					parseInsert();
					break;
				case UPDATE:
					type = Type.UPDATE;
					parseUpdate();
					break;
				case DELETE:
					type = Type.DELETE;
					parseDelete();
					break;
				default:
					type = Type.UNRESOLVED;
					parseOther();
					break;
			}
        } catch (Exception e) {
	        error(e);
        }
		
		return new Sql(type, nodes.toArray(new AstNode[nodes.size()]));
	}

	protected final void parseSelect(){
		if(parseMore) {
			new SqlSelectParser(this).parseSelectBody();
			while(!lexer.isEOF()) {
				if(lexer.token() == Token.UNION || lexer.token() == Token.MINUS) {
					new SqlSelectParser(this).parseUnion();
				}else{
					return;
				}
			}
		}else{
			parseAny();
		}
	}
	
	protected void parseInsert(){
        if(parseMore) {
            new SqlInsertParser(this).parseInsertBody();
        }else{
            parseAny();
        }
	}
	
	protected void parseUpdate(){
		if(parseMore) {
			new SqlUpdateParser(this).parseUpdateBody();
		}else{
			parseAny();	
		}
	}
	
	protected void parseDelete(){
		if(parseMore) {
			new SqlDeleteParser(this).parseDeleteBody();
		}else{
			parseAny();
		}
	}
	
	protected void parseOther(){
		parseAny();
	}
	
	protected void parseAny(){
		parseRest();
	}
	
	//parse the remaining sql text.
	protected void parseRest(){
		for(;;){
			if(lexer.isEOS()){
				appendText();
				break;
			}
			parseToken();
		}
	}
	
	protected void parseExpr() {
		new SqlExprParser(this).parseExpr();
	}

    protected AstNode parseExprNode() {

        createSavePoint();

        parseExpr();

        AstNode[] nodes = removeSavePoint();

        if(nodes.length == 0) {
            return null;
        }

        if(nodes.length == 1) {
            return nodes[0];
        }

        return new SqlNodeContainer(nodes);
    }
	
	/**
	 * Returns <code>true</code> if stop at the given stop tokens.
	 * 
	 * <p>
	 * Returns <code>false</code> if EOF.
	 */
	protected boolean parseRestStopAt(Token... stops){
		for(;;){
			if(null != stops && stops.length > 0){
				for(int i=0;i<stops.length;i++){
					if(lexer.token() == stops[i]){
						//appendTextWithoutToken();
						return true;
					}
				}
			}
			
			if(lexer.isEOS()){
				appendText();
				break;
			}
			
			parseToken();
		}
		
		return false;
	}
	
	protected boolean parseRestForClosingParen(){
		int count = 1;
		for(;;){
			
			if(lexer.token() == Token.LPAREN) {
				count++;
				acceptText();
				continue;
			}
			
			if(lexer.token() == Token.RPAREN) {
				count--;
				if(count == 0) {
					return true;
				}
				acceptText();
				continue;
			}
			
			if(lexer.isEOS()){
				appendText();
				break;
			}
			
			parseToken();
		}
		return false;
	}
	
	/**
	 * Parses current token.
	 */
	protected void parseToken(){
		node = null;
		
    	if(parseSpecialToken()){
    		return;
    	}
    	
    	parseSqlToken();
	}
	
	//parse non sql tokens
	protected boolean parseSpecialToken(){
		Token token = lexer.token();
		if(token == Token.DYNAMIC){
			// {? .. }
			parseDynamicClause();
			return true;
		}else if(token == Token.COLON_PLACEHOLDER){
			// :name
			acceptNode(new ParamPlaceholder(token, lexer.literal()));
			return true;
		}else if(token == Token.JDBC_PLACEHOLDER){
			// ?
			acceptNode(new JdbcPlaceholder());
			return true;
		}else if(token == Token.SHARP_PLACEHOLDER){
			// #name#
			acceptNode(new ParamPlaceholder(token, lexer.literal()));
			return true;
		}else if(token == Token.EXPR_PLACEHOLDER){
			// #{..}
			String expr = lexer.literal();
			acceptNode(new ExprParamPlaceholder(expr, compileExpression(expr)));
			return true;
		}else if(token == Token.DOLLAR_REPLACEMENT){
			// $name$
			acceptNode(new ParamReplacement(lexer.literal(), Scope.UNKNOWN));
			return true;
		}else if(token == Token.EXPR_REPLACEMENT){
			// ${..}
			String expr = lexer.literal();
			acceptNode(new ExprParamReplacement(expr, compileExpression(expr),Scope.UNKNOWN));
			return true;
		}else if(token == Token.AT_IF){
			// @if(..) .. @elseif(..) .. @else .. @endif;
			parseIfClause();
			return true;
		}else if(token == Token.TAG){
			// @name{...}
			parseTag();
			return true;
		}
		
		return false;
	}
	
	//parse other tokens
	protected void parseSqlToken(){
		Token token = lexer.token();
		
		if(token == Token.LITERAL_CHARS){
			parseSqlString();
			return;
		}
		
		if(type == Type.SELECT){
			if(token == Token.ORDER){
				if(parseOrderBy()){
					return;	
				}
			}
		}
		
		if(parseMore){
			if(lexer.token().isKeywordOrIdentifier() && parseSqlDotName()){
				return;
			}
		}
		
		acceptText();
	}
	
	protected final boolean parseOrderBy(){
		createSavePoint();
		
		SqlOrderBy orderBy = new SqlOrderByParser(this).orderBy();
		
		if(null == orderBy){
			restoreSavePoint();
			return false;
		}else{
			restoreNodes().addNode(orderBy);
			return true;
		}
	}
	
	protected final void parseSqlString(){
		//$..$ and ${..} can used in string literal
		//i.e. '$name$' , '${expr}' , 'a${name}$'
		appendText("'");
		
		Lexer lexer = new SqlStringLexer(this.lexer.literal());
		
		for(;;){
			lexer.nextToken();
			appendText(lexer.acceptText());
			
			if(lexer.token() == Token.DOLLAR_REPLACEMENT){
				nodes.add(new ParamReplacement(lexer.literal(),Scope.STRING));
				continue;
			}
			
			if(lexer.token() == Token.EXPR_REPLACEMENT){
				String expr = lexer.literal();
				nodes.add(new ExprParamReplacement(expr, compileExpression(expr),Scope.STRING));
				continue;
			}
			
			appendText(lexer.acceptText());
			
			if(lexer.isEOF()){
				break;
			}
		}
		
		appendText("'");
		nextToken();
	}
	
	protected boolean parseSqlObjectName(){
		if(null != lexer.token() && lexer.token().isKeywordOrIdentifier()){
			if(!parseSqlDotName()){
				acceptNode(new SqlObjectName(scope(),lexer.tokenText()));
			}
			return true;
		}
		return false;
	}
	
	protected boolean parseSqlDotName(){
		if(lexer.ch == '.'){
			String firstName = lexer.tokenText();
			
			lexer.nextChar();
			
			if(lexer.ch == '*'){
				lexer.nextChar();//skip '*'
				acceptNode(new SqlAllColumns(firstName));
			}else{
				SqlObjectName name = new SqlObjectName();
				name.setQuoted(lexer.token().isQuotedIdentifier());
				name.setScope(scope());
				name.setFirstName(firstName);

				nextToken().expectIdentifier();
				
				if(lexer.ch == '.'){
					name.setSecondaryName(lexer.tokenText());
					
					lexer.nextChar();
					nextToken().expect(Token.IDENTIFIER);
					name.setLastName(lexer.tokenText());
				}else{
					name.setLastName(lexer.tokenText());
				}
				
				acceptNode(name);
			}
			
			return true;
		}
		
		return false;
	}
	
	protected void parseDynamicClause(){
		int mark = lexer.tokenStart();
		
		lexer.nextChars(2);//skip {?
		
		suspendNodes();
		nextToken();
		
		String paramsString = null;
		
        for (;;) {
            if(lexer.isToken(Token.RBRACE)) {
                break;
            }

            if(lexer.isToken(Token.SEMI)) {
                if(!lexer.nextToChar('}')) {
                    lexer.reportError("Unclosed dynamic clause at {0}", lexer.describePosition(mark));
                    return;
                }

                paramsString = lexer.text();
                lexer.nextToken();
                break;
            }
            
            if(lexer.isEOS()) {
                lexer.reportError("Unclosed dynamic clause at {0}", lexer.describePosition(mark));
                return;
            }

            parseToken();
        }
        
        DynamicClause dc;
        if(null != paramsString) {
            Map<String, String> params = new HashMap<String, String>();
            KeyValueParser.parseKeyValuePairs(params, paramsString, ':');
            dc = new DynamicClause(nodes(), params);
        }else{
            dc = new DynamicClause(nodes());    
        }
		
		restoreNodes();
		acceptNode(dc);
	}
	
	protected void parseIfClause(){
		int pos = lexer.tokenStart();
		
		//current char = '('
		
		Token			  ifToken       = Token.AT_IF;
		List<IfStatement> ifStatements  = new ArrayList<IfStatement>();
		ElseStatement     elseStatement = null;
		IfCondition       condition     = null;
		
		//create nodes save point
		suspendNodes();
		
		for(;;){
			//scan condition node
			lexer.nextChar();
			lexer.scanConditionalExpression();
			condition = new IfCondition(lexer.literal(), compileExpression(lexer.literal()));
			
			nodes.clear(); //clear for body nodes
			
			//scan next @elseif or @else or @end token
			nextToken();
			
			if(!parseRestStopAt(IF_STOP_TOKENS)){
				lexer.reportError("Unclosed @if statement, {0}", lexer.describePosition(pos));
				return;
			}
			
			//create previous if statement.
			ifStatements.add(createIfStatement(ifToken, condition, nodes));
			nodes.clear();
			
			//stop at @elseif
			if(lexer.token() == Token.AT_ELSEIF){
				ifToken = lexer.token();
				lexer.skipWhitespaces();
				continue;
			}
			
			//stop at @else, scan @end token
			if(lexer.token() == Token.AT_ELSE){
				nextToken();
				if(!parseRestStopAt(Token.AT_ENDIF)){
					lexer.reportError("Unclosed @if statement, {0}",lexer.describePosition(pos));
					return;
				}
				elseStatement = new ElseStatement(nodes());
				break;
			}
			
			//stop at @endif
			if(lexer.token() == Token.AT_ENDIF){
				break;
			}
			
			throw new IllegalStateException("Illegal state in @if statement, must stop at valid '@' token");
		}
		
		restoreNodes();
		acceptNode(new IfClause(ifStatements.toArray(new IfStatement[ifStatements.size()]),elseStatement));
	}
	
	protected final IfStatement createIfStatement(Token token, IfCondition condition,List<AstNode> bodyNodes){
		return new IfStatement(token, condition, bodyNodes.toArray(new AstNode[bodyNodes.size()]));
	}
	
	protected void parseTag(){
		throw new IllegalStateException("Not implemented");
	}
	
	protected SqlTableName parseTableName(){
        SqlTableName name = parseTableNameOnly();
        if(null != name) {
            acceptNode(name);
        }
        return name;
	}

    protected SqlTableName parseTableNameOnly(){
        if(lexer.token().isKeywordOrIdentifier()){
            SqlTableName name = new SqlTableName();
            name.setQuoted(lexer.token().isQuotedIdentifier());
            if(lexer.ch == '.'){
                name.setFirstName(lexer.tokenText());

                lexer.nextChar();
                nextToken().expectIdentifier();

                if(lexer.ch == '.'){
                    name.setSecondaryName(lexer.tokenText());

                    lexer.nextChar();
                    nextToken().expectIdentifier();

                    name.setLastName(lexer.tokenText());
                }else{
                    name.setLastName(lexer.tokenText());
                }
            }else{
                name.setLastName(lexer.tokenText());
            }

            return name;
        }

        return null;
    }
}