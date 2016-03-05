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

import leap.core.AppContext;
import leap.lang.tools.DEV;
import org.junit.After;
import org.junit.Before;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

public class WebTestRunner extends BlockJUnit4ClassRunner {
    public WebTestRunner(Class<?> klass) throws InitializationError {
        super(klass);

        DEV.setCurrentTestClass(klass);
    }

    @Override
    protected void validateInstanceMethods(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(After.class, false, errors);
        validatePublicVoidNoArgMethods(Before.class, false, errors);
        validateTestMethods(errors);

		/* do not throw error is no test cases in this class.
        if (computeTestMethods().size() == 0) {
            errors.add(new Exception("No runnable methods"));
        }
        */
    }

    @Override
    protected Object createTest() throws Exception {
        Object test = super.createTest();

        AppContext context = AppContext.tryGetCurrent();
        if(null != context) {
            context.getBeanFactory().inject(test);
        }

        return test;
    }

}
