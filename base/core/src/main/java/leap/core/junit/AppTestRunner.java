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
package leap.core.junit;

import leap.core.AppClassLoader;
import leap.core.AppContext;
import leap.core.AppContextInitializer;
import leap.core.BeanFactory;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.tools.DEV;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.List;

public class AppTestRunner extends BlockJUnit4ClassRunner {

    private static final Log log = LogFactory.get(AppTestRunner.class);

    final static BeanFactory factory;

	static {
        log.info("Init app context by '{}'",AppTestRunner.class.getSimpleName());
        AppContextInitializer.markTesting();
        try {
            AppContext.initStandalone();
        }catch (RuntimeException e) {
            log.error("Error init app context : " + e.getMessage(), e);
            throw e;
        }
        factory = AppContext.current().getBeanFactory();
	}

    private static Class<?> loadTestClass(Class<?> klass) throws InitializationError {
        Class<?> c = klass;

        AppClassLoader cl = AppClassLoader.get();
        if(cl.hasFailedClasses()) {
            c = cl.redefineTestClass(c);
        }

        DEV.setCurrentTestClass(c);
        return c;
    }

	public AppTestRunner(Class<?> klass) throws InitializationError {
	    super(loadTestClass(klass));
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        return super.getChildren();
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);

        System.out.println();
        System.out.println();
        System.out.println("============== Running test case : " + description + " ==============");
        System.out.println();
        System.out.println();

        super.runChild(method, notifier);
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        factory.injectStatic(getTestClass().getJavaClass());
        super.collectInitializationErrors(errors);
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
        return factory.getOrCreateBean(getTestClass().getJavaClass());
    }
	
}