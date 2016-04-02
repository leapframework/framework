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
package tests;

import org.junit.Test;


public class ScriptTest extends HtplTestCase {

	@Test
	public void testJQueryTemplate() {
		context.setLocalVariable("v", "1");
		assertRender("<script type=text/x-jquery-tmpl><div>1</div></script>",
					 "<script type=text/x-jquery-tmpl><div>${v}</div></script>");
		
		assertRender("<script type=text/x-jquery-tmpl><div>${v}</div></script>",
				 "<script type=text/x-jquery-tmpl ht-inline-el=false><div>${v}</div></script>");		
		
		assertRender("<script type=text/x-jquery-tmpl><tr><td>1</td></tr></script>",
				 "<script type=text/x-jquery-tmpl><tr><td>${v}</td></tr></script>");

		assertRender("<script type=text/x-jquery-tmpl><tr><td>1</td></tr></script>",
				 "<script type=text/x-jquery-tmpl ht-inline-html=true><tr><td>${v}</td></tr></script>");
		
		assertRender("<script type=text/x-jquery-tmpl><div>1</div></script>",
				 "<script type=text/x-jquery-tmpl ht-inline-html='true'><div ht-text=${v}></div></script>");	
		
		assertRender("<script type=text/x-jquery-tmpl><div>${v}</div></script>",
				 "<script type=text/x-jquery-tmpl ht-inline-html='true' ht-inline-el=false><div>${v}</div></script>");
		
		assertRender("<body><script type=text/x-jquery-tmpl><div a=${v}>${v}</div><div>1</div></script></body>",
				 "<body><script type=text/x-jquery-tmpl ht-inline-html='true' ht-inline-el=false>" + 
				 "<div a=${v}>${v}</div><div ht-inline-el=true>${v}</div></script></body>");		
	}
}