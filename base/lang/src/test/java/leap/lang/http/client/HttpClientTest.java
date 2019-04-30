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

package leap.lang.http.client;

import leap.junit.TestBase;
import leap.junit.contexual.Contextual;
import leap.junit.contexual.ContextualProvider;
import leap.junit.contexual.ContextualRule;
import leap.lang.Charsets;
import leap.lang.New;
import leap.lang.http.client.apache.ApacheHttpClient;
import leap.lang.io.IO;
import leap.lang.path.Paths;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Contextual
public class HttpClientTest extends TestBase {

    private static int    port    = 10999;
    private static String baseUrl = "http://127.0.0.1:" + port;
    private static Server server;

    private static Map<String, HttpClient> clients  = new HashMap<>();
    private static Map<String, Handler>    handlers = new HashMap<>();

    @Rule
    public ContextualRule rule = new ContextualRule(new ContextualProvider() {
        @Override
        public Iterable<String> names(Description description) {
            return New.arrayList("jdk","apache");
        }

        @Override
        public void beforeTest(Description description, String name) throws Exception {
            client = clients.get(name);
        }
    });

    private HttpClient client;

    @Test
    public void testNotFound() throws IOException {
        assertEquals(404, client.request(url("/")).get().getStatus());
    }

    @Test
    public void testSimpleGet() throws IOException {
        handle("/simple_get", (req,resp) -> resp.getWriter().write("Hello"));

        HttpResponse response = client.request(url("/simple_get")).get();
        assertTrue(response.isOk());
        assertEquals("Hello", response.getString());

        client.request(url("/simple_get")).sendAsync((req, resp) -> {
            assertTrue(resp.isOk());
            assertEquals("Hello", IO.readStringAndClose(resp.getInputStream(), Charsets.UTF_8));
        });
    }

    private static void handle(String path, Handler handler) {
        handlers.put(path, handler);
    }

    @BeforeClass
    public static void startServer() throws Exception {
        clients.put("jdk",    createJdkClient());
        clients.put("apache", createApacheClient());

        server = new Server(port);

        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target,
                               Request baseRequest,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException {

                Handler handler = handlers.get(Paths.suffixWithoutSlash(target));
                if(null != handler){
                    handler.handle(request, response);

                    if(response.getStatus() == 0) {
                        response.setStatus(200);
                    }

                    baseRequest.setHandled(true);
                }
            }
        });

        server.start();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        server.stop();
    }

    private static String url(String path) {
        return baseUrl + Paths.suffixWithSlash(path);
    }

    private static HttpClient createJdkClient() {
        return new JdkHttpClient();
    }

    private static HttpClient createApacheClient() {
        ApacheHttpClient client = new ApacheHttpClient();

        client.setMaxConnectionTotal(2);
        client.setMaxConnectionPerRoute(2);

        client.init();

        return client;
    }

    private interface Handler {
        void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException,ServletException;
    }
}
