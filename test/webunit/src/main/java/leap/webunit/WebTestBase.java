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

import leap.junit.TestBase;
import leap.lang.Strings;
import leap.lang.http.HTTP.Method;
import leap.webunit.client.THttpClient;
import leap.webunit.client.THttpRequest;
import leap.webunit.client.THttpResponse;
import leap.webunit.server.TWebServer;
import org.junit.runner.RunWith;

import javax.servlet.ServletContext;

@RunWith(WebTestRunner.class)
public abstract class WebTestBase extends TestBase {
	
    static THttpClient httpClient;
    static THttpClient httpsClient;

    protected static TWebServer     server;
    protected static ServletContext rootServletContext;
	protected static boolean        defaultHttps;

    /**
     * Returns the {@link THttpClient} with base url <code>https://localhost:port</code>.
     */
    protected static THttpClient httpsClient() {
        return httpsClient;
    }

    /**
     * Returns the {@link THttpClient} with base url <code>http://localhost:port</code>.
     */
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
            contextPath = "";
        }else{
            contextPath = servletContext.getContextPath();
        }

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

    /**
     * Sends a GET request to the given path and returns the {@link THttpResponse}.
     *
     * <p/>
     * Example :
     * <pre>
     *
     *     get("/hello")
     *
     * </pre>
     */
	protected final THttpResponse get(String path) {
		response = client().get(path(path));
		request  = response.request();
		return response;
	}

    /**
     * Sends a POST request to the given path and returns the {@link THttpResponse}.
     */
	protected final THttpResponse post(String path) {
		response = client().post(path(path));
		request  = response.request();
		return response;
	}

    /**
     * Sends a PUT request to the given path and returns the {@link THttpResponse}.
     */
    protected final THttpResponse put(String path) {
        response = client().request(Method.PUT,path(path)).send();
        request  = response.request();
        return response;
    }

    /**
     * Sends a PATCH request to the given path and returns the {@link THttpResponse}.
     */
    protected final THttpResponse patch(String path) {
        response = client().request(Method.PATCH,path(path)).send();
        request  = response.request();
        return response;
    }

    /**
     * Sends a HEAD request to the given path and returns the {@link THttpResponse}.
     */
    protected final THttpResponse head(String path) {
        response = client().request(Method.HEAD,path(path)).send();
        request  = response.request();
        return response;
    }

    /**
     * Sends a DELETE request to the given path and returns the {@link THttpResponse}.
     */
    protected final THttpResponse delete(String path) {
        response = client().request(Method.DELETE,path(path)).send();
        request  = response.request();
        return response;
    }

    /**
     * Sends a OPTIONS request to the given path and returns the {@link THttpResponse}.
     */
    protected final THttpResponse options(String path) {
        response = client().request(Method.OPTIONS,path(path)).send();
        request  = response.request();
        return response;
    }

    /**
     * Sends a POST request with the given form param to the given path and returns the {@link THttpResponse}.
     *
     * @param name the form param's name.
     * @param value the form param's value.
     */
	protected final THttpResponse post(String path,String name,String value) {
		response = forPost(path).addFormParam(name, value).send();
		request  = response.request();
		return response;
	}

    /**
     * Sends a POST request with the given form params to the given path and returns the {@link THttpResponse}.
     *
     * @param name1 the form param's name.
     * @param value1 the form param's value.
     * @param name2 the form param's name.
     * @param value2 the form param's value.
     */
	protected final THttpResponse post(String path,String name1,String value1,String name2,String value2) {
		response = forPost(path)
	                    .addFormParam(name1, value1)
	                    .addFormParam(name2, value2).send();
		request  = response.request();
		return response;
	}

    /**
     * Creates a new {@link THttpRequest} for sending GET request later.
     */
    protected final THttpRequest forGet(String path) {
        return client().request(Method.GET, path(path));
    }

    /**
     * Creates a new {@link THttpRequest} for sending POST request later.
     */
    protected final THttpRequest forPost(String path) {
        return client().request(Method.POST, path(path));
    }

    /**
     * Creates a new {@link THttpRequest} for sending DELETE request later.
     */
    protected final THttpRequest forDelete(String path) {
        return client().request(Method.DELETE, path(path));
    }

    /**
     * Creates a new {@link THttpRequest} for sending PUT request later.
     */
    protected final THttpRequest forPut(String path) {
        return client().request(Method.PUT, path(path));
    }

    /**
     * Creates a new {@link THttpRequest} for sending the request with http {@link Method} later.
     */
    protected final THttpRequest request(Method method, String path) {
        return client().request(method, path(path));
    }

    /**
     * Sends an Ajax GET request to the given path and returns {@link THttpResponse}.
     */
    protected THttpResponse ajaxGet(String path) {
        response = forGet(path).ajax().send();
        request = response.request();
        return response;
    }

    /**
     * Sends an Ajax POST request to the given path and returns {@link THttpResponse}.
     */
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