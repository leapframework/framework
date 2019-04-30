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

import leap.htpl.HtplTemplate;
import leap.lang.Locales;
import leap.lang.jsoup.nodes.Document;
import leap.lang.jsoup.nodes.Element;
import org.junit.Test;

import java.util.Locale;

public class ViewTest extends HtplTestCase {

	@Test
	public void testForInIf(){
		Document html = get("/test/for_in_if").getDocument();
		Element select = html.getElementsByTag("select").first();
		
		Element option1 = select.child(0);
		assertEquals("selected",option1.attr("selected"));
		
		Element option2 = select.child(1);
		assertFalse(option2.attributes().hasKey("selected"));
	}

	@Test
	public void testConfigProperty() {
        Document html = get("/test/config_prop").getDocument();
        Element p = html.getElementsByTag("p").first();

        assertEquals("hello world", p.text().trim());
    }
	
    @Test
    public void testViewResolver() {
        HtplTemplate t = engine.resolveTemplate("index", null);
        assertNotNull(t);

        Locale locale = Locales.forName("zh");
        t = engine.resolveTemplate("index", locale);
        assertNotNull(t);
    }

    @Test
    public void testInterceptor() {
        String html = get("/html?__view_name__=1").getContent();

        assertTrue(html.startsWith("<!--view name:/html-->"));
    }

    @Test
    public void testVarMessage() {
	    get("/test/var_msg").assertContentContains("Hello vars");
    }
}
