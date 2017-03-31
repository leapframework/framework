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
package tests;

import leap.lang.New;

import leap.lang.Strings;
import org.junit.Test;

import java.io.File;

public class RenderTest extends HtplTestCase {

	@Test
	public void testAnyAttr(){
		context.setContextPath("/t");
		context.setLocalVariable("value", "1");
		
		assertRender("<a href=\"/1.jpg\"></a>","<a ht:href=\"@{^/1.jpg}\" href=\"1.jpg\"/>");
		assertRender("<a href=\"/t/1.jpg\"></a>","<a ht:href=\"@{/1.jpg}\" href=\"1.jpg\"/>");
		assertRender("<a href=\"/t/1.jpg\"></a>","<a ht:href=\"@{~/1.jpg}\" href=\"1.jpg\"/>");
		assertRender("<a href=\"/t\"></a>","<a ht-href=\"@{/}\" href=\"/\"/>");
		assertRender("<a href=\"/\"></a>","<a ht-href=\"@{^}\" href=\"/\"/>");
		assertRender("<a href=\"/t/test\">test</a>","<a ht-href=\"@{/test}\">test</a>");
		assertRender("<a href=\"/t/test\">test</a>","<a ht-attr-href=\"@{/test}\">test</a>");
		assertRender("<input type=\"text\" value=\"1\"/>","<input type=\"text\" ht-value=\"${value}\"/>");
		assertRender("<script>var url='/t/1.js';</script>","<script>var url='@!{/1.js}';</script>");
	}
	
	@Test
	public void testAnyAttrConditional() {
		context.setLocalVariable("i", 1);
		assertRender("<div class=\"a\">1</div>", "<div class=\"a\" ht-class-if=\"i > 0\">1</div>");
		
		context.setLocalVariable("i", 0);
		assertRender("<div>1</div>", "<div class=\"a\" ht-class-if=\"i > 0\">1</div>");
		
		context.setLocalVariable("i", 1);
		assertRender("<div class=\"\">1</div>", "<div ht-class-if=\"i > 0\">1</div>");
	}
	
	@Test
	public void testAnyAttrMinimized(){
		assertRender("<input disabled=\"disabled\"/>", "<input ht-disabled=\"\"/>");
		assertRender("<input disabled=\"disabled\"/>", "<input disabled=\"\"/>");
		assertRender("<input disabled=\"disabled\"/>", "<input ht-disabled=\"${true}\"/>");
		assertRender("<input disabled=\"disabled\"/>", "<input disabled=\"${true}\"/>");
		assertRender("<input/>", "<input ht-disabled=\"${false}\"/>");
		assertRender("<input disabled=\"x\"/>", "<input ht-disabled=\"x\"/>");
		assertRender("<option selected=\"selected\">1</option>", "<option ht-selected=\"\">1</option>");
		assertRender("<option selected=\"selected\">1</option>", "<option ht-selected-if=\"true\">1</option>");
	}
	
	@Test
	public void testEscapeAttributes() {
		assertRender("<input type=text/>","<input type=text />");
		assertRender("<div a='x\"x'>a</div>","<div a='x\"x'>a</div>");
	}
	
	@Test
	public void testHtmlWithExpression(){
		context.setContextPath("/t");
		context.setLocalVariable("name", "htpl");
		
		assertRender("<input value=\"htpl\"/>", "<input value=\"${name}\"/>");
		assertRender("<span>htpl</span>","<span>${name}</span>");
		assertRender("<a href=\"/1.jpg\">img</a>","<a href=\"@{^/1.jpg}\">img</a>");
		assertRender("<a href=\"/t/1.jpg\">img</a>","<a href=\"@{/1.jpg}\">img</a>");
		assertRender("<a href=\"/t/1.jpg\">img</a>","<a href=\"@{~/1.jpg}\">img</a>");
		assertRender("<a href=\"//example.com/1.jpg\">img</a>","<a href=\"@{//example.com/1.jpg}\">img</a>");
	}
	
	@Test
	public void testTextAndHtml(){
		context.setLocalVariable("msg", "Hello world");
		assertRender("<p>Hello world</p>","<p ht-text=\"${msg}\">text</p>");
		assertRender("<p>Hello world</p>","<p ht-html=\"${msg}\">text</p>");
	}
	
	@Test
	public void testInclude(){
		assertRender("<html><p>Include</p></html>","<html><!--#include includes/inc1--></html>");
		assertRender("<html><div><p>Include</p></div></html>","<html><div ht-include=\"includes/inc1\">a</div></html>");
		assertRender("<p><div>fragment1</div></p>","<p><!--#include includes/inc2#fragment1 --></p>");
		assertRender("<p><div>fragment2</div></p>","<p><!--#include includes/inc2#fragment2 --></p>");
		
		get("/test/include_fragment").assertOk();
	}
	
	@Test
	public void testParserLevelSingleComments() {
		assertRender("<p>1</p>","<!--//comments--><p>1</p>");
		assertRender("<p>1</p>","<!--// comments--><p>1</p>");
		assertRender("<p>1</p>","<!--//comments--><p>1</p><!--//comments-->");
		assertRender("<p>1</p>","<!--//comments--><p>1<!--//comments--></p><!--//comments-->");
	}
	
	@Test
	public void testBlockComments() {
		assertRender("<p>1</p>","<!--/*--><p>1</p><!--*/--><p>1</p>");
		assertRender("<ul></ul>","<ul><!--/*--><li><a href=\"/\">/</a></li><!--*/--></ul>");
	}
	
	@Test
	public void testHtmlComments() {
		assertRender("<!--c-->1<!--<>-->");
		assertRender("<div><!--c-->1<!--<>--></div>");
		assertRender("<div> <!--<div>1</div>--></div>");
		assertRender("<div><!--  <div>1</div>  --></div>","<!--#if(true)--><div><!--  <div>1</div>  --></div><!--#endif-->");
	}
	
	@Test
	public void testConditionalComment() {
	    assertRender("<!--[if lt IE 9]><script src=\"t.js\"></script><![endif]-->");
	    assertRender("<!--[if lt IE 9]>\n<script src=\"t.js\"></script>\n<![endif]-->");
	    
	    context.setContextPath("/t");
	    assertRender("<!--[if lt IE 9]>\n<script src=\"/t/t.js\"></script>\n<![endif]-->",
	    			 "<!--[if lt IE 9]>\n<script src=\"/t.js\"></script>\n<![endif]-->");
	    
	    assertRender("<div><div><!--[if lt IE 9]><script src=\"1.js\"></script><![endif]--></div><div>2</div></div>");
	    assertRender("<html><head><!--[if lt IE 9]><script src=\"1.js\"></script><![endif]--></head><body>2</body></html>");
	}
	
	@Test
	public void testFor() {
		context.setLocalVariable("c", New.arrayList("1","2"));
		assertRender("12", "<!--#for i : c-->${i}<!--#endfor-->");
		assertRender("<ul><li>1</li><li>2</li></ul>", "<ul><!--#for i : c--><li>${i}</li><!--#endfor--></ul>");
		
		assertRender("12", "<!--#for i : c-->${loop.index}<!--#endfor-->");
		assertRender("12", "<!--#for i : c-->${loop.item}<!--#endfor-->");
		
		context.setLocalVariable("c", New.arrayList("1","2","3"));
		assertRender("12", "<!--#for i : c--><!--#break(i > 2)-->${i}<!--#endfor-->");
		assertRender("12", "<!--#for i : c-->${i}<!--#break(i == 2)--><!--#endfor-->");
		
		assertRender("empty","<!--#for i : e-->${1}<!--#empty-->empty<!--#endfor-->");
		
		context.setLocalVariable("count", 2);
		assertRender("12","<!--#for i : count-->${i}<!--#endfor-->");
		assertRender("12","<!--#for i : 2-->${i}<!--#endfor-->");
	}

	@Test
	public void testSetVariables() {
		assertRender("1", "<!--#set n=1-->${n}");
		assertRender("1", "<!--#set n=1-->${n}<!--#endset-->");
		assertRender("12", "<!--#set a=1; b = 2-->${a}${b}");
		assertRender("12", "<!--#set a=1\r b = 2-->${a}${b}");
		assertRender("12", "<!--#set a=1\n b = 2-->${a}${b}");
		assertRender("12", "<!--#set a=1\r\n b = 2-->${a}${b}");
		assertRender("12", "<!--#set a=1; b = 2-->${a}${b}<!--#endset-->");
		assertRender("name","<!--#set n=\"name\"-->${n}");
		
		assertRender("<div><div>1</div></div>", "<div><div><!--#set n=1-->${n}</div></div>");
		assertRender("<div><div></div></div>", "<div><div><!--#set n=1--></div>${n}</div>");
	}
	
	@Test
	public void testIf() {
		context.setLocalVariable("i", 10);
		assertRender("1", "<!--#if i >= 10-->1<!--#endif-->");
		assertRender("1", "<!-- #if i >= 10-->1<!--#endif-->");
		assertRender("", "<!--#if i < 10-->1<!--#endif-->");
		
		context.setLocalVariable("i", 9);
		assertRender("1", "<!--#if i < 10-->1<!--#elseif i == 10-->2<!--#else-->3<!--#endif-->");
		
		context.setLocalVariable("i", 10);
		assertRender("2", "<!--#if i < 10-->1<!--#elseif i == 10-->2<!--#else-->3<!--#endif-->");
		
		context.setLocalVariable("i", 11);
		assertRender("3", "<!--#if i < 10-->1<!--#elseif i == 10-->2<!--#else-->3<!--#endif-->");
		
		context.setLocalVariable("i", null);
		assertRender("3", "<!--#if i < 10-->1<!--#elseif i == 10-->2<!--#else-->3<!--#endif-->");
	}
	
	@Test
	public void testRemoveBlankLines() {
		assertRender("\ts", "<!--#if true-->\ts<!--#endif-->");
		assertRender("\ts", "<!--#if true-->\n\ts<!--#endif-->");
		assertRender("<!---->\t<select>	<option>1</option></select>", "<!---->\t<select>	<option>1</option></select>");
		assertRender("\t<select><option>1</option></select>", "<!--#if true-->\n\t<select><option>1</option></select><!--#endif-->");
	}
	
	@Test
	public void testCommentsInText() {
		assertRender("<div>s</div>", "<div><!--#if(true)-->s<!--#endif--></div>");
	}
	
	@Test
	public void testCommentsExpression() {
		context.setLocalVariable("v", "100");
		assertRender("<!--100-->","<!--${v}-->");
	}
	
	@Test
	public void testScripts() {
		context.setLocalVariable("i", 10);
		assertRender("<script>\n if(i > 0){ var a = ''; } </script>", "<script>\n if(i > 0){ var a = ''; } </script>");
		assertRender("<script> if(i > 0){ var a = '';\n var b = ''; } </script>", "<script> if(i > 0){ var a = '';\n var b = ''; } </script>");
		assertRender("<script> if(i > 10){ var a = ''; } </script>", "<script> if(i > ${i}){ var a = ''; } </script>");
		
		/*
		assertRender("<script>var i=0;</script>", "<script><!--#if(true)-->var i=0;<!--#endif--></script>");
		assertRender("<script>\n var i=0;\n var j=0;</script>", "<script>\n <!--#if(true)-->var i=0;\n var j=0;<!--#endif--></script>");
		
		assertRender("<script>if(i > 0){var a = '';}</script>", "<script><!--#if(true)-->if(i > 0){var a = '';}<!--#endif--></script>");
		*/
	}
	
	@Test
	public void testScriptWithHtmlElements() {
		assertRender("<script>var s = '<div>';\n var s1='</p>';</script>");
	}
	
	@Test
	public void testLayout(){
		assertRender("<div><p>Layout1</p><div><p>Content</p></div></div>",
					 "<!--@ layout=layouts/layout1--><p ht-fragment=\"content\">Content</p>");
		
		assertRender("<div><p>Layout1</p><div><p>Content</p></div></div>",
					 "<!--@layout=\"layouts/layout1\"--><p ht-fragment=\"content\">Content</p>");
		
		assertRender("<div><p>Layout1</p><div><p>Content</p></div></div>",
					 "<!--@layout=\"layouts/layout2\"--><p ht-fragment=\"content\">Content</p>");
		
		assertRender("<div><p>Layout1</p><div>Content</div></div>",
				 "<!--@layout=\"layouts/layout2\"--><!--#fragment content-->Content<!--#endfragment-->");
	}
	
	@Test
	public void testHtml() {
		assertRender("<html><head></head><body></body></html>");
		assertRender("<!DOCTYPE html><html><head></head><body></body></html>");
	}

	@Test
	public void testHtml5DataPrefix() {
		assertRender("<a data-src=\"1.jpg\">1</a>");
	}

	@Test
	public void testBlankLineBeforeEndBody() {
		assertRender("<html><body>Hello</body></html>");
		assertRender("<html><body>\nHello\n</body></html>");
		assertEquals(get("/test/simple.html?$debug=false").getContent().replaceAll("\r","").replaceAll("\n",""),"<html><body>Hello</body></html>");
	}
	@Test
	public void testDynamicAttribute(){
		context.setVariable("attrName","test1");
		context.setVariable("attrValue","test2");
		context.setVariable("childValue","test3");
		assertRender("<div><span test1=\"test2\" attr=\"attr\">test3</span></div>","<div ht-inline-html=\"true\"><span ${attrName}=\"${attrValue}\" attr=\"attr\">${childValue}</span></div>");
		//assertRender("<div><span ${attrName}=\"test2\" attr=\"attr\">test3</span></div>","<div ht-inline-html=\"false\"><span ${attrName}=\"${attrValue}\" attr=\"attr\">${childValue}</span></div>");
	}
}