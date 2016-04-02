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

import leap.lang.New;
import org.junit.Test;

import java.util.List;

public class ProcessorTest extends HtplTestCase {
    
    @Test
    public void testFor(){
        List<Object> list = New.<Object>arrayList(1,2);
        context.setLocalVariable("list", list);
        assertRender("<p>1</p><p>2</p>","<p ht-for=\"item : list\" ht-text=\"${item}\"/>");
    }
    
    @Test
    public void testIf(){
        assertRender("<p>1</p>","<p ht-if=\"true\">1</p>");
        assertRender("<p>1</p>","<p ht-if=\"${true}\">1</p>");
        assertRender("","<p ht-if=\"false\">1</p>");
        assertRender("<p>1</p>","<p ht-if=\"!false\">1</p>");
    }

	@Test
	public void testUrlAttr() {
		context.setContextPath("/t");
		
		assertRender("<a href=\"/t/1.jpg\">1</a>","<a href=\"/1.jpg\">1</a>");
		assertRender("<a href=\"/t/1.jpg\">1</a>","<a href=\"~/1.jpg\">1</a>");
		assertRender("<a href=\"1.jpg\">1</a>","<a href=\"1.jpg\">1</a>");
		assertRender("<a href=\"/1.jpg\">1</a>","<a href=\"^/1.jpg\">1</a>");
		
		assertRender("<a href=\"/t/1.jpg\">1</a>","<a href=\"err\" ht-href=\"/1.jpg\">1</a>");
	}
	
	@Test
	public void testClassAppendAttr() {
		assertRender("<li class=\"child x\">a</li>","<li class=\"child\" ht-class-append=\"${true ? 'x' : ''}\">a</li>");
		assertRender("<li class=\"child\">a</li>","<li class=\"child\" ht-class-append=\"${false ? 'x' : ''}\">a</li>");
	}
	
}
