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
package leap.webunit;

import java.util.ArrayList;
import java.util.List;

import leap.junit.contexual.ContextualProvider;
import leap.junit.contexual.ContextualRule;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.Description;

public abstract class WebTestBaseContextual extends WebTestBase {
	
	private static final List<String> contextPaths = new ArrayList<String>();
	
	static {
		contextPaths.add("");
		contextPaths.add("/root");
	}
	
    @BeforeClass
    public static void startServer() {
        duplicateRootContext = true;
        WebTestBase.startServer();
    }
	
	@Rule
	public final ContextualRule contextualRule = new ContextualRule(new ContextualProvider() {
		@Override
        public Iterable<String> names(Description description) {
	        return contextPaths;
        }

		@Override
        public void beforeTest(Description description, String name) throws Exception {
			servletContext = server.getServletContext(name);
        }
	},true);

}