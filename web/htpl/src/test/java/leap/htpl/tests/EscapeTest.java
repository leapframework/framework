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
package leap.htpl.tests;

import leap.htpl.HtplTestCase;

import org.junit.Test;


public class EscapeTest extends HtplTestCase {

	@Test
	public void testBaseEntities(){
		assertRender("<p>&nbsp;</p>");
		assertRender("<p>&lt;100&gt;&amp;&quot;&copy;</p>");
		assertRender("<input value=\"a>\"/>");
		assertRender("<input value=\"a<\"/>");
	}
	
	@Test
	public void testAttributeSpecialChars() {
		assertRender("<a href=\"javascript:alert('');\">a</a>");
		assertRender("<a href=\"javascript:alert('&');\">a</a>");
		assertRender("<a href=\"javascript:alert('<');\">a</a>");
	}
	
	@Test
	public void testHtmlAutoEscape() {
		context.setLocalVariable("a", "<");
		assertRender("&lt;" ,"${a}");
		assertRender("&nbsp;&lt;" ,"&nbsp;${a}");
		assertRender("&nbsp;<" ,"&nbsp;${a;escape:off}");
		assertRender("&nbsp;<" ,"&nbsp;$!{a}");
		assertRender("&nbsp;<" ,"&nbsp;${a ; escape : off}");
		assertRender("&nbsp;&lt;" ,"&nbsp;${a;}");
		assertRender("&nbsp;&lt; ; " ,"&nbsp;${a + ' ; '}");
	}
	
	@Test
	public void testAttrAutoEscape() {
		context.setLocalVariable("sq", "\'");
		context.setLocalVariable("dq", "\"");
		context.setLocalVariable("amp","&");
		
		assertRender("<div a=\"'\">1</div>");
		assertRender("<div a=\"'\">1</div>","<div a=\"${sq}\">1</div>");
		assertRender("<div a=\"&quot;\">1</div>","<div a=\"${dq}\">1</div>");
	}
	
	@Test
	public void testJavascriptAutoEscape() {
		context.setLocalVariable("sq", "\'");
		context.setLocalVariable("dq", "\"");

		assertRender("<script>var a = '';</script>");
		assertRender("<script>var a = '\\'';</script>","<script>var a = '${sq}';</script>");
		assertRender("<script>var a = '';</script>","<script>var a = '${sq;escape:off};</script>");
	}

	@Test
	public void testExpressionSpecialChars(){
		context.setLocalVariable("i", 1);
		context.setLocalVariable("j", 10);
		context.setLocalVariable("s", "str");
		
		assertRender("<p>str</p>","<p>${i > 0 && j > 0 ? s : 'nothing'}</p>");
		assertRender("<p>str</p>","<p>${i &gt; 0 &amp;&amp; j &gt; 0 ? s : 'nothing'}</p>");
		
		assertRender("<input value=\"str\"/>","<input value=\"${i > 0 && j > 0 ? s : 'nothing'}\"/>");
		assertRender("<input value=\"str\"/>","<input value=\"${i &gt; 0 &amp;&amp; j &gt; 0 ? s : 'nothing'}\"/>");
	}

}