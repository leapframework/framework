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

import leap.core.el.ExpressionLanguage;
import leap.lang.Strings;
import leap.lang.exception.ParseException;
import leap.lang.expression.Expression;
import leap.orm.sql.Sql;
import leap.orm.sql.Sql.ParseLevel;
import leap.orm.sql.Sql.Scope;
import leap.orm.sql.ast.AstNode;
import leap.orm.sql.ast.SqlToken;
import leap.orm.sql.ast.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class SqlParserBase {
	
	protected final Lexer 			   lexer;
	protected final ExpressionLanguage el;
	protected final Sql.ParseLevel	   level;
	protected final boolean			   parseMore;

	protected Sql.Type      type;
	protected AstNode		node;
	protected List<AstNode> nodes;
	
	private Stack<List<AstNode>> savedNodes = new Stack<>();
	private Stack<Sql.Scope>     scope      = new Stack<>();
	
	protected SqlParserBase(Lexer lexer,ExpressionLanguage el){
		this.lexer     = lexer;
		this.el        = el;
		this.level     = lexer.level;
		this.parseMore = this.level == ParseLevel.MORE;
	}
	
	protected final SqlParserBase nextToken(){
		lexer.nextToken();
		appendText();
		return this;
	}

	protected final void acceptText(){
		appendText(lexer.tokenText());
		nextToken();
	}

    protected final boolean acceptText(Token token) {
        if(lexer.token() == token) {
            acceptText();
            return true;
        }
        return false;
    }

    protected final void acceptNode() {
        acceptNode(new SqlToken(lexer.token(), lexer.tokenText()));
    }
	
	/**
	 * Accepts the given node and move to next token.
	 */
	protected final void acceptNode(AstNode node){
		this.node = node;
		nodes.add(node);
		nextToken();
	}

	protected final void addNode(AstNode node){
		this.node = node;
		nodes.add(node);
	}
	
    protected final SqlParserBase expect(Token token) {
        if (lexer.token() != token) {
            throw new SqlParserException("Syntax error, expect " + token + ", actual " + lexer.token() + " : " + lexer.describePosition());
        }
        return this;
    }
    
    protected final SqlParserBase expects(Token... tokens) {
    	boolean in = false;
    	for(Token token : tokens){
            if (lexer.token() == token) {
                in = true;
                break;
            }
    	}
    	
    	if(!in){
    		throw new SqlParserException("Syntax error, expect [" +  Strings.join(tokens,",")  + "], actual " + lexer.token() + " : " + lexer.describePosition());
    	}
    	
        return this;
    }
    
    protected final SqlParserBase expectIdentifier() {
        if (!lexer.token().isIdentifier()) {
            throw new SqlParserException("Syntax error, expect IDENTIFIER , actual " + lexer.token() + " : " + lexer.describePosition());
        }
        return this;
    }
    
    protected final SqlParserBase expectNextToken(Token token) {
    	lexer.nextToken();
    	return expect(token);
    }
    
    protected final AstNode[] nodes(){
    	return nodes.toArray(new AstNode[nodes.size()]);
    }

    /**
     * Suspends current nodes. (will not save the lexer's state)
     */
    protected final void suspendNodes(){
		this.savedNodes.add(nodes);
		this.nodes = new ArrayList<>();
	}

    /**
     * Restores the previous suspended nodes. (will not restore the lexer's state)
     */
	protected final SqlParserBase restoreNodes(){
		this.nodes = this.savedNodes.pop();
		return this;
	}

    /**
     * Creates a save point for saving all the state of parser, includes lexer's state and the parsed nodes.
     */
    protected final void createSavePoint(){
        lexer.createSavePoint();
        this.suspendNodes();
    }

    /**
     * Restores all the state of parser to the previous created save point.
     */
    protected final void restoreSavePoint(){
        lexer.restoreSavePoint();
        this.restoreNodes();
    }

    protected final void acceptSavePoint(){
        lexer.deleteSavePoint();
        List<AstNode> popNodes = this.savedNodes.pop();
        popNodes.addAll(this.nodes);
        this.nodes = popNodes;
    }

    protected final AstNode[] removeSavePoint(){
        lexer.deleteSavePoint();

        List<AstNode> popNodes = this.savedNodes.pop();

        AstNode[] removedNodes = this.nodes();

        this.nodes = popNodes;

        return removedNodes;
    }


    protected final Scope scope(){
		return scope.isEmpty() ? null : scope.peek();
	}
	
	protected final void setScope(Scope scope){
		this.scope.add(scope);
	}
	
	protected final void removeScope(){
		this.scope.pop();
	}
	
    protected final boolean lookahead(Token... tokens) {
        if(lexer.isEOS()){
            return false;
        }

        createSavePoint();

        try{
            for(Token token : tokens) {

                if(lexer.token() != token) {
                    return false;
                }

                nextToken();

                if(lexer.isEOS()){
                    return false;
                }
            }
        }finally{
            restoreSavePoint();
        }

        return true;
    }
	
	protected final void appendText(){
		appendText(lexer.acceptText());
	}
    
	protected void appendText(String text){
		if(null == text || text.length() == 0){
			return;
		}
		
		if(nodes.size() > 0){
			AstNode lastNode = nodes.get(nodes.size() - 1);
			if(lastNode instanceof Text){
				((Text) lastNode).append(text);
			}else{
				nodes.add(new Text(text));
			}
		}else{
			nodes.add(new Text(text));
		}
	}
	
	protected final Expression compileExpression(String text){
		return compileExpression(lexer.tokenStart(),text);
	}
	
	protected final Expression compileExpression(int pos,String text){
		try {
			String expr = text.trim();
	        return el.createExpression(expr);
        } catch (Exception e) {
        	throw new ParseException("Error compiling expression '" + text + "' at " + lexer.describePosition(pos));
        }
	}
	
	protected void error(Exception e){
		if(e instanceof IllegalStateException){
			throw (IllegalStateException)e;
		}
		if(e instanceof SqlParserException){
			throw (SqlParserException)e;
		}
		throw new SqlParserException("Parsing sql error : " + e.getMessage(),e);
	}
}
