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
package leap.htpl;

import java.io.StringWriter;

import leap.core.AppContext;
import leap.htpl.resolver.StringHtplResource;
import leap.webunit.WebTestBaseContextual;

public abstract class HtplTestCase extends WebTestBaseContextual {

	protected static HtplEngine engine;
	
	static {
		engine = AppContext.factory().getBean(HtplEngine.class);
	}
	
	protected DefaultHtplContext context;
	
	@Override
	protected void doSetUp() throws Exception {
		context = new DefaultHtplContext(engine);
	}

	protected HtplDocument parseDocument(String html){
		return engine.parseDocument(new StringHtplResource(html));
	}
	
	protected HtplTemplate parseTemplate(String html){
		return engine.createTemplate(html);
	}
	
	protected String render(String html){
		StringWriter writer = new StringWriter();
		parseTemplate(html).render(context, writer);
		return writer.toString();
	}
	
	protected void assertRender(String result,String html){
		assertEquals(result, render(html));
	}
	
	protected void assertRender(String input){
		assertEquals(input, render(input));
	}

}