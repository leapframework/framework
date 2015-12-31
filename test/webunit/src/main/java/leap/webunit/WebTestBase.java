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

import javax.servlet.ServletContext;

import leap.junit.TestBase;
import leap.lang.Strings;
import leap.lang.http.HTTP.Method;
import leap.webunit.client.THttpClient;
import leap.webunit.client.THttpClientImpl;
import leap.webunit.client.THttpRequest;
import leap.webunit.client.THttpResponse;
import leap.webunit.server.TWebServer;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(WebTestRunner.class)
public abstract class WebTestBase extends TestBase {
	
    private static THttpClient httpClient;
    private static THttpClient httpsClient;

    protected static int            httpPort  = TWebServer.DEFAULT_HTTP_PORT;
    protected static int            httpsPort = TWebServer.DEFAULT_HTTPS_PORT;
    protected static TWebServer     server;
    protected static ServletContext rootServletContext;
	protected static boolean        defaultHttps;
	protected static boolean        duplicateRootContext;
	
    @BeforeClass
    public static void startServer() {
        synchronized (WebTestBase.class) {
            if(null == httpClient) {
                httpClient = new THttpClientImpl(httpPort);
            }
            
            if(null == httpsClient) {
                httpsClient = new THttpClientImpl(httpsPort, true);
            }
            
            if (null == server) {
                server = new TWebServer(httpPort, httpsPort, true);
                if(duplicateRootContext) {
                    server.duplicateContext("", "/root");
                }
                
                server.start();
            }
        }
        
        rootServletContext = server.getServletContext("");
    }
    
    protected static THttpClient httpsClient() {
        return httpsClient;
    }
    
    protected static THttpClient httpClient(){
        return httpClient;
    }
	
	private boolean https = defaultHttps;
	
    protected ServletContext servletContext;
    protected String         contextPath;	
	protected THttpRequest   request;
	protected THttpResponse  response;
	
    @Override
    protected void setUp() throws Exception {
        if (null == servletContext) {
            servletContext = rootServletContext;
        }
        contextPath = servletContext.getContextPath();
        this.doSetUp();
    }
    
    protected void doSetUp() throws Exception {
        
    }
	
	protected final void runHttps(Runnable runnable) {
	    final boolean state = https;
	    https = true;
	    try{
	        runnable.run();
	    }finally{
	        https = state;
	    }
	}
	
    protected final void runHttp(Runnable runnable) {
        final boolean state = https;
        https = false;
        try {
            runnable.run();
        } finally {
            https = state;
        }
    }
	
	protected final THttpClient client() {
	    return https ? httpsClient : httpClient;
	}
	
	protected final THttpResponse get(String path) {
		response = client().get(path(path));
		request  = response.request();
		return response;
	}
	
	protected final THttpResponse post(String path) {
		response = client().post(path(path));
		request  = response.request();
		return response;
	}
	
	protected final THttpResponse post(String path,String name,String value) {
		response = forPost(path).addFormParam(name, value).send();
		request  = response.request();
		return response;
	}
	
	protected final THttpResponse post(String path,String name1,String value1,String name2,String value2) {
		response = forPost(path)
	                    .addFormParam(name1, value1)
	                    .addFormParam(name2, value2).send();
		request  = response.request();
		return response;
	}
	
    protected final THttpRequest forGet(String path) {
        return client().request(Method.GET, path(path));
    }

    protected final THttpRequest forPost(String path) {
        return client().request(Method.POST, path(path));
    }

    protected final THttpRequest forDelete(String path) {
        return client().request(Method.DELETE, path(path));
    }

    protected final THttpRequest forPut(String path) {
        return client().request(Method.PUT, path(path));
    }

    protected final THttpRequest request(Method method, String path) {
        return client().request(method, path(path));
    }

    protected THttpResponse ajaxGet(String path) {
        response = forGet(path).ajax().send();
        request = response.request();
        return response;
    }

    protected THttpResponse ajaxPost(String path) {
        response = forPost(path).ajax().send();
        request = response.request();
        return response;
    }

    private final String path(String path) {
        if (Strings.isEmpty(contextPath)) {
            return path;
        } else {
            if(path.startsWith(contextPath + "/")) {
                return path;
            }else{
                return contextPath + (Strings.isEmpty(path) ? "/" : path);
            }
        }
    }
}