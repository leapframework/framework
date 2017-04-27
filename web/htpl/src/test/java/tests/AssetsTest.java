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

import leap.junit.contexual.Contextual;
import leap.lang.jsoup.nodes.Document;
import leap.lang.jsoup.nodes.Element;

import org.junit.Test;

public class AssetsTest extends HtplTestCase {

	@Test
	public void testSingleScriptAsset() {
		Document html = get("/test_assets/single_script.html").getDocument();
		
		String src = html.getElementsByTag("script").first().attr("src");
		assertTrue(src.startsWith("/assets/js/hello-"));
	}
	
	@Test
	@Contextual("/root")
	public void testAssetsAttributes(){
		Document html = get("/test_assets/attr.html").getDocument();

		Element e1 = html.getElementById("s1");
		Element e2 = html.getElementById("s2");
		Element e3 = html.getElementById("i1");
		
		assertTrue(e1.attr("src").startsWith(contextPath + "/assets/js/hello-"));
		assertTrue(e2.attr("src").startsWith(contextPath + "/assets/js/hello-"));
		assertTrue(e3.attr("value").startsWith(contextPath + "/assets/js/hello-"));
	}
	
	@Test
	public void testSimpleCssBundle(){
		Document html = get("/test_assets/bundle_css.html").getDocument();
		String src = html.getElementsByTag("link").first().attr("href");
		assertTrue(src.startsWith("/assets/css/bundle-") && src.endsWith(".css"));
		
		String content = get(src).getContent();
		assertTrue(content.startsWith("a{color:blue}") && content.endsWith("p{margin:5px 5px 5px 5px}"));
	}

    @Test
    public void testSimpleJsBundle() {
        Document html = get("/test_assets/bundle_js.html").getDocument();
        String src = html.getElementsByTag("script").first().attr("src");
        assertTrue(src.startsWith("/assets/js/bundle-") && src.endsWith(".js"));
        
        String content = get(src).getContent();
        assertTrue(content.startsWith("var i;") && content.endsWith("var j;"));

    }

	@Test
	@Contextual("/root")
	public void testCssBundleUrl() {
		Document html = get("/test_assets/bundle_css_url.html").getDocument();

		String src = html.getElementsByTag("link").first().attr("href");

		String content = get(src).getContent();

		assertContains(content, "url(/root/assets/css/css2-");
		assertContains(content, "url(/root/assets/img/line-");
		assertContains(content, "url(/root/assets/img/1.jpg)");

		assertContains(content, "url(/root/assets/img/line1-");
	}

    @Test
    public void testAssetsInTheme() {
        Document html = get("/test_assets/theme.html").getDocument();
        String src = html.getElementsByTag("script").first().attr("src");
        assertTrue(src.startsWith("/assets/js/hello1-"));
    }
}