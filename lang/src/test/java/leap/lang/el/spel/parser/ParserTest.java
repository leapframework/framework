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
package leap.lang.el.spel.parser;

import leap.lang.el.spel.SpelTestCase;
import leap.lang.el.spel.ast.AstBinary;
import leap.lang.el.spel.ast.AstChoice;
import leap.lang.el.spel.ast.AstExpr;
import leap.lang.el.spel.ast.AstFunction;
import leap.lang.el.spel.ast.AstIdentifier;
import leap.lang.el.spel.ast.AstItem;
import leap.lang.el.spel.ast.AstNumber;
import leap.lang.el.spel.ast.AstProperty;
import leap.lang.el.spel.ast.AstString;
import leap.lang.el.spel.parser.Parser;

import org.junit.Test;

public class ParserTest extends SpelTestCase {
    
    @Test
    public void testMultiExpr() {
        Parser parser = new Parser("1 2");
        AstExpr expr = parser.expr();
        System.out.println(expr);
    }
	
	@Test
    public void testAdd() throws Exception {
        String text = "a + b";
        Parser parser = new Parser(text);
        AstExpr expr = parser.expr();

        AstBinary binaryExpr = (AstBinary) expr;

        AstIdentifier left = (AstIdentifier) binaryExpr.getLeft();
        AstIdentifier right = (AstIdentifier) binaryExpr.getRight();

        assertEquals("a", left.getName());
        assertEquals("b", right.getName());
    }
    
	@Test
    public void testDiv() throws Exception {
        String text = "a / b";
        Parser parser = new Parser(text);
        AstExpr expr = parser.expr();

        AstBinary binaryExpr = (AstBinary) expr;

        AstIdentifier left = (AstIdentifier) binaryExpr.getLeft();
        AstIdentifier right = (AstIdentifier) binaryExpr.getRight();

        assertEquals("a", left.getName());
        assertEquals("b", right.getName());
        assertEquals(expr.toString(), text);
    }
    
    @Test
    public void testBitAnd() throws Exception {
        String text = "a & b";
        Parser parser = new Parser(text);
        AstExpr expr = parser.expr();

        AstBinary binaryExpr = (AstBinary) expr;

        AstIdentifier left = (AstIdentifier) binaryExpr.getLeft();
        AstIdentifier right = (AstIdentifier) binaryExpr.getRight();

        assertEquals("a", left.getName());
        assertEquals("b", right.getName());
        assertEquals(expr.toString(), text);
    }
    
    @Test
    public void testBitOr() throws Exception {
        String text = "a | b";
        Parser parser = new Parser(text);
        AstExpr expr = parser.expr();

        AstBinary binaryExpr = (AstBinary) expr;

        AstIdentifier left = (AstIdentifier) binaryExpr.getLeft();
        AstIdentifier right = (AstIdentifier) binaryExpr.getRight();

        assertEquals("a", left.getName());
        assertEquals("b", right.getName());
        assertEquals(expr.toString(), text);
    }
    
    @Test
    public void testBitXor() throws Exception {
        String text = "a ^ b";
        Parser parser = new Parser(text);
        AstExpr expr = parser.expr();
        
        AstBinary binaryExpr = (AstBinary) expr;
        
        AstIdentifier left = (AstIdentifier) binaryExpr.getLeft();
        AstIdentifier right = (AstIdentifier) binaryExpr.getRight();
        
        assertEquals("a", left.getName());
        assertEquals("b", right.getName());
        assertEquals(expr.toString(), text);
    }
    
    @Test
    public void testRightShift() throws Exception {
        String text = "a >> b";
        Parser parser = new Parser(text);
        AstExpr expr = parser.expr();
        
        AstBinary binaryExpr = (AstBinary) expr;
        
        AstIdentifier left = (AstIdentifier) binaryExpr.getLeft();
        AstIdentifier right = (AstIdentifier) binaryExpr.getRight();
        
        assertEquals("a", left.getName());
        assertEquals("b", right.getName());
        assertEquals(expr.toString(), text);
    }
    
    @Test
    public void testLeftShift() throws Exception {
        String text = "a << 3";
        Parser parser = new Parser(text);
        AstExpr expr = parser.expr();
        
        AstBinary binaryExpr = (AstBinary) expr;
        
        AstIdentifier left = (AstIdentifier) binaryExpr.getLeft();
        AstNumber right = (AstNumber) binaryExpr.getRight();
        
        assertEquals("a", left.getName());
        assertEquals(3, right.getValue());
        assertEquals(expr.toString(), text);
    }
    
    @Test
    public void testSub() throws Exception {
        String text = "+1 + +2.0 + -1 + -1.3";
        Parser parser = new Parser(text);
        AstExpr expr = parser.expr();

        assertEquals("1 + 2.0 + -1 + -1.3", expr.toString());
    }
    
    @Test
    public void testChoice() {
    	AstExpr expr = Parser.parse("a ? b : c");
    	
    	AstChoice cexpr = (AstChoice)expr;
    	
    	AstIdentifier condition  = (AstIdentifier)cexpr.getQuestion();
    	AstIdentifier trueValue  = (AstIdentifier)cexpr.getYes();
    	AstIdentifier falseValue = (AstIdentifier)cexpr.getNo();
    	
    	assertEquals("a",condition.getName());
    	assertEquals("b",trueValue.getName());
    	assertEquals("c",falseValue.getName());
    }
    
    @Test
    public void testFunction() {
    	AstBinary expr = parse("1 + a(1)");
    	AstFunction func = (AstFunction)expr.getRight();
    	assertNull(func.getPrefix());
    	assertEquals("a",func.getName());
    	
    	func = parse("p:a()");
    	assertEquals("p", func.getPrefix());
    	assertEquals("a", func.getName());
    	
    	expr = parse("1 + p:a (1,2,'3')");
    	func = (AstFunction)expr.getRight();
    	assertEquals("p", func.getPrefix());
    	assertEquals("a", func.getName());
    }
    
    @Test
    public void testItem() {
    	AstItem p = parse("a['p']");
    	assertTrue(p.getIndex() instanceof AstString);
    	
    	p = parse("a[p]");
    	assertTrue(p.getIndex() instanceof AstIdentifier);
    	
    	p = parse("a[o.p]");
    	assertTrue(p.getIndex() instanceof AstProperty);
    }
}
