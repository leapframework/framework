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
package leap.lang.jsoup;

import org.junit.Test;

import leap.junit.TestBase;
import leap.lang.jsoup.nodes.Document;
import leap.lang.jsoup.nodes.Element;
import leap.lang.jsoup.parser.HtmlParseMode;

public class ParserTest extends TestBase {

	@Test
	public void testTrTdParse() {
		Document doc = Jsoup.parse("<tr id='a'><td>1</td></tr>",HtmlParseMode.ORIGINAL);
		Element e = doc.getElementById("a");
		assertNotNull(e);
	}
	
}
