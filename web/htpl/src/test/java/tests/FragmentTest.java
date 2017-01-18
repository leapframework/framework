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

import leap.htpl.HtplDocument;
import leap.htpl.ast.Fragment;
import leap.htpl.resolver.StringHtplResource;
import leap.lang.Strings;

import org.junit.Test;

public class FragmentTest extends HtplTestCase {

	@Test
	public void testRenderFragment() {
		assertRender("<div>1</div>","<div ht-fragment=test>1</div>");
		assertRender("<div>1</div>","<div ht-fragment=test><!--#if(true)-->1<!--#endif--></div>");
		
		assertRender("<div>1</div>","<!--#fragment test--><div>1</div><!--#endfragment-->");
		assertRender("<div>1</div>","<!--#fragment test--><div><!--#if(true)-->1<!--#endif--></div><!--#endfragment-->");
	}
	
	@Test
	public void testRenderFragmentMulti() {
		assertRender("11","<!--#fragment test--><!--#if(true)-->1<!--#endif--><!--#endfragment--><!--#render-fragment test-->");
		assertRender("<div>1</div><div>1</div>","<!--#fragment test--><div ht:if=\"true\">1</div><!--#endfragment--><!--#render-fragment test-->");
	}
	
	@Test
	public void testRecrusiveFragment(){
		String expected = 
				"<ul><i>a</i><li><ul><i>a-1</i><li><ul><i>a-1-1</i></ul></li></ul><ul><i>a-2</i></ul><ul><i>a-3</i><li><ul><i>a-3-1</i><li><ul><i>a-3-1-1</i></ul></li></ul></li></ul></li></ul><ul><i>b</i><li><ul><i>b-1</i><li><ul><i>b-1-1</i></ul></li></ul></li></ul>";
		
		String result = Strings.removeBlank(get("/test/recursive_fragment?$debug=0").getContent());
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testRecrusiveFragmentNested(){
		String expected = 
				"<div><ul><i>a</i><li><ul><i>a-1</i><li><ul><i>a-1-1</i></ul></li></ul><ul><i>a-2</i></ul><ul><i>a-3</i><li><ul><i>a-3-1</i><li><ul><i>a-3-1-1</i></ul></li></ul></li></ul></li></ul><ul><i>b</i><li><ul><i>b-1</i><li><ul><i>b-1-1</i></ul></li></ul></li></ul></div>";
		
		String result = Strings.removeBlank(get("/test/recursive_fragment_nested?$debug=0").getContent());
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testRecrusiveFragmentMulti(){
		String expected = 
				"<ul><i>a</i><li><ul><i>a-1</i><li><ul><i>a-1-1</i></ul></li></ul><ul><i>a-2</i></ul><ul><i>a-3</i><li><ul><i>a-3-1</i><li><ul><i>a-3-1-1</i></ul></li></ul></li></ul></li></ul><ul><i>b</i><li><ul><i>b-1</i><li><ul><i>b-1-1</i></ul></li></ul></li></ul>" + 
				"<div><ul><i>a</i><li><ul><i>a-1</i><li><ul><i>a-1-1</i></ul></li></ul><ul><i>a-2</i></ul><ul><i>a-3</i><li><ul><i>a-3-1</i><li><ul><i>a-3-1-1</i></ul></li></ul></li></ul></li></ul><ul><i>b</i><li><ul><i>b-1</i><li><ul><i>b-1-1</i></ul></li></ul></li></ul></div>" + 
				"<ul><i>a</i><li><ul><i>a-1</i><li><ul><i>a-1-1</i></ul></li></ul><ul><i>a-2</i></ul><ul><i>a-3</i><li><ul><i>a-3-1</i><li><ul><i>a-3-1-1</i></ul></li></ul></li></ul></li></ul><ul><i>b</i><li><ul><i>b-1</i><li><ul><i>b-1-1</i></ul></li></ul></li></ul>";
		
		String result = Strings.removeBlank(get("/test/recursive_fragment_multi?$debug=0").getContent());
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testNestedFragment() {
		HtplDocument doc = engine.parseDocument(new StringHtplResource("<div ht-fragment=f1>1<div ht-fragment=f2>2</div></div>"));
		
		doc.process();
		
		assertEquals(2,doc.getFragments().size());
		
		Fragment f2 = doc.getFragment("f2");
		assertNotNull(f2);
	}
}