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
package leap.htpl.tests;

import java.util.HashMap;
import java.util.Map;

import leap.htpl.HtplDocument;
import leap.htpl.HtplTestCase;
import leap.lang.expression.Expression;

import org.junit.Test;

public class ParseTest extends HtplTestCase {
	
	protected void assertParse(String text){
		assertEquals(text,parseDocument(text).toString());
	}
	
	protected void assertParse(String text,String result){
		assertEquals(result,parseDocument(text).toString());
	}

	@Test
	public void testHeaderProperties(){
		HtplDocument doc = parseDocument("<!--@ a=b c=d e=\"f g\" f='x'--><a/>");
		assertEquals("b",doc.getProperty("a"));
		assertEquals("d",doc.getProperty("c"));
		assertEquals("f g",doc.getProperty("e"));
		assertEquals("x",doc.getProperty("f"));
	}
	
	@Test
	public void testDocType(){
		String html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html><body></body></html>";
		assertParse(html);
		
		html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html><body></body></html>";
		assertParse(html);
		
		html = "<!DOCTYPE HTML><html><body></body></html>";
		assertParse(html);
		
		html = "<!DOCTYPE html><html><body></body></html>";
		assertParse(html);
		
		html = "<!doctype html><html><body></body></html>";
		assertParse(html);		
		
		html = "<!--@ a=b --><!doctype html><html><body></body></html>";
		HtplDocument doc = parseDocument(html);
		assertEquals("b",doc.getProperty("a"));
		assertEquals("<!doctype html><html><body></body></html>", doc.toString());
	}
	
	@Test
	public void testNamespace(){
		String html = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:t=\"http://sample.com\"></html>";
		assertEquals("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:t=\"http://sample.com\"></html>",parseDocument(html).toString());
	}
	
	@Test
	public void testProcessors(){
		String html = "<a t:href=\"~/a.html\" b=\"x\" a=\"a.html\">hello</a>";
		HtplDocument doc = parseDocument(html);
		assertEquals(html, doc.toString());
	}
	
	@Test
	public void testInclude(){
		String html = "<html><!--#include \"test\"--></html>";
		assertParse(html);
		
		String html1 = "<html><!--#include test --></html>";
		assertEquals(html,parseDocument(html1).toString());
	}
	
	@Test
	public void testBaseEntities(){
		assertParse("<p>&nbsp;</p>");
		assertParse("<p>&lt;100&gt;&amp;&quot;&copy;</p>");
		assertParse("<input value=\"a>\"/>");
		assertParse("<input value=\"a&lt;\"/>","<input value=\"a<\"/>");
	}
	
	@Test
	public void testQuoteCharacters() {
		assertParse("<input type=text name='a'/>","<input type=text name='a'/>");
		assertParse("<div a='x\"x'>a</div>");
	}
	
	@Test
	public void testCompositeExpression(){
		Expression expr = engine.getExpressionManager().parseCompositeExpression(engine,"${owner.id}/edit");
		
		Map<String,Object> vars  = new HashMap<String, Object>();
		Map<String,Object> owner = new HashMap<String, Object>();
		
		vars.put("owner",owner);
		owner.put("id", 1);
		
		assertEquals("1/edit",expr.getValue(vars));
	}
	
	@Test
	public void testForInTableTbody() {
	     assertParse("<table><!--#for(item : items)--><tr><td>${item.name}</td></tr><!--#endfor--></table>"); 
	}
}