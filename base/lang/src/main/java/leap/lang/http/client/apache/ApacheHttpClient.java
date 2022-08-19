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

package leap.lang.http.client.apache;

import leap.lang.Disposable;
import leap.lang.Initializable;
import leap.lang.http.client.AbstractHttpClient;
import leap.lang.http.client.HttpRequest;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class ApacheHttpClient extends AbstractHttpClient implements Initializable,Disposable {

    private static final Log log = LogFactory.get(ApacheHttpClient.class);

    private boolean             init;
    private CloseableHttpClient httpClient;
    private RequestConfig       requestConfig;
    private int                 maxConnectionTotal    = 1000;
    private int                 maxConnectionPerRoute = 1000;
    private int                 bufferSize;

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public int getMaxConnectionTotal() {
        return maxConnectionTotal;
    }

    public void setMaxConnectionTotal(int maxConnectionTotal) {
        this.maxConnectionTotal = maxConnectionTotal;
    }

    public int getMaxConnectionPerRoute() {
        return maxConnectionPerRoute;
    }

    public void setMaxConnectionPerRoute(int maxConnectionPerRoute) {
        this.maxConnectionPerRoute = maxConnectionPerRoute;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public HttpRequest request(String url) {
        return new ApacheHttpRequest(this, url);
    }

    public void init() {
        if(this.init) {
            return;
        }

        this.init          = true;
        this.requestConfig = initRequestConfig();
        this.httpClient    = initHttpClient();
    }

    @Override
    public void dispose() throws Throwable {
        if(null != httpClient) {
            log.info("Close http client");
            this.init = false;
            httpClient.close();
        }
    }

    RequestConfig getRequestConfig() {
        return requestConfig;
    }

    protected RequestConfig initRequestConfig() {
        return RequestConfig.copy(RequestConfig.DEFAULT)
                            .setConnectTimeout(getDefaultConnectTimeout())
                            .setSocketTimeout(getDefaultReadTimeout())
                            .build();
    }

    protected CloseableHttpClient initHttpClient() {
        HttpClientBuilder cb = HttpClientBuilder.create();

        //TODO : small buffer size will cause socket closed when reading response entity?
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(getDefaultRegistry());
        //cm.setDefaultConnectionConfig(ConnectionConfig.custom().setBufferSize(1024 * 1024).build());

        cm.setMaxTotal(maxConnectionTotal);
        cm.setDefaultMaxPerRoute(maxConnectionPerRoute);

        if(bufferSize > 0) {
            ConnectionConfig cc =
                    ConnectionConfig.copy(ConnectionConfig.DEFAULT).setBufferSize(bufferSize).build();

            cm.setDefaultConnectionConfig(cc);
        }

        cb.setRetryHandler(new DefaultHttpRequestRetryHandler());

        cb.setConnectionManager(cm);
        cb.setDefaultRequestConfig(requestConfig);
        cb.disableCookieManagement();
        return cb.build();
    }

    protected Registry<ConnectionSocketFactory> getDefaultRegistry() {
        RegistryBuilder<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create();

        reg.register("http", PlainConnectionSocketFactory.getSocketFactory());

        SSLConnectionSocketFactory sslSocketFactory =
                new SSLConnectionSocketFactory(SSL_CONTEXT, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        reg.register("https", sslSocketFactory);

        return reg.build();
    }
}
