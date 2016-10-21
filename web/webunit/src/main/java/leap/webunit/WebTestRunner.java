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
import leap.core.AppContextInitializer;
import leap.lang.tools.DEV;
import leap.webunit.client.THttpClient;
import leap.webunit.client.THttpClientImpl;
import leap.webunit.server.TWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import javax.servlet.ServletContext;
import java.util.List;

public class WebTestRunner extends BlockJUnit4ClassRunner {

    public static final String ROOT_CONTEXT_PATH = "/root";

    private static THttpClient httpClient;
    private static THttpClient httpsClient;

    protected static int            httpPort  = TWebServer.DEFAULT_HTTP_PORT;
    protected static int            httpsPort = TWebServer.DEFAULT_HTTPS_PORT;
    protected static TWebServer     server;
    protected static ServletContext rootServletContext;
    protected static boolean        duplicateRootContext;
    protected static AppContext     context;

    public WebTestRunner(Class<?> klass) throws InitializationError {
        super(init(klass));
    }

    protected static Class<?> init(Class<?> cls) {
        DEV.setCurrentTestClass(cls);

        startServer();

        return cls;
    }

    protected static void startServer() {
        synchronized (WebTestRunner.class) {
            if(null == server) {
                if(null == httpClient) {
                    httpClient = new THttpClientImpl(httpPort);
                }

                if(null == httpsClient) {
                    httpsClient = new THttpClientImpl(httpsPort, true);
                }

                if (null == server) {

                    AppContextInitializer.markTesting();

                    server = new TWebServer(httpPort, httpsPort, true);

                    if(duplicateRootContext) {
                        server.duplicateContext("", ROOT_CONTEXT_PATH);
                    }

                    server.start();

                    httpClient.addContextPaths(server.getContextPaths());
                    httpsClient.addContextPaths(server.getContextPaths());
                }

                rootServletContext = server.tryGetServletContext("");
                if(null != rootServletContext) {
                    context = AppContext.get(rootServletContext);
                }else if(server.getServletContexts().size() == 1){
                    context = AppContext.get(server.getServletContexts().values());
                }

                setup();
            }
        }
    }

    protected static void setup() {
        WebTestBase.server = server;
        WebTestBase.rootServletContext = rootServletContext;
        WebTestBase.httpClient = httpClient;
        WebTestBase.httpsClient = httpsClient;
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

    protected void collectInitializationErrors(List<Throwable> errors) {
        if(null != context) {
            context.getBeanFactory().injectStatic(getTestClass().getJavaClass());
        }
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
        AppContext context = null == rootServletContext ? null : AppContext.get(rootServletContext);
        if(null != context) {
            return context.getBeanFactory().getOrCreateBean(getTestClass().getJavaClass());
        }else{
            return super.createTest();
        }
    }

}
