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
package leap.core.junit;

import leap.core.AppConfig;
import leap.core.AppContext;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import org.junit.Test;

public class AppTestCaseTest extends AppTestBase implements PostCreateBean {

	protected @Inject AppConfig config;

    private boolean postCreateCalled;

	@Test
	public void testAppContextInited(){
		assertNotNull(AppContext.current());
	}

	@Test
	public void testInject() {
		assertNotNull(config);
        assertTrue(postCreateCalled);
	}

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        postCreateCalled = true;
    }
}
