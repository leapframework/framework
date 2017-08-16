/*
 * Copyright 2017 the original author or authors.
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
package leap.webunit.client;

import leap.lang.Args;
import leap.lang.Charsets;
import leap.lang.Collections2;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

abstract class THttpClientBase implements THttpClient {

    protected static X509TrustManager trustManager;
    protected static SSLContext       sslContext;

    static {
        trustManager = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        };

        TrustManager[] trustAllCerts = new TrustManager[]{trustManager};

        HttpsURLConnection.setDefaultHostnameVerifier((arg0, arg1) -> true);

        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static final String LOCAL_HTTP_BASE_URL_PREFIX  = "http://localhost";
    public static final String LOCAL_HTTPS_BASE_URL_PREFIX = "https://localhost";

    protected final String baseUrl;
    protected final Set<String> contextPaths = new HashSet<>();

    protected boolean autoRedirect   = false;
    protected Charset defaultCharset = Charsets.UTF_8;

    protected THttpClientBase(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public Charset getDefaultCharset() {
        return defaultCharset;
    }

    @Override
    public void setDefaultCharset(Charset charset) {
        Args.notNull(charset, "charset");
        this.defaultCharset = charset;
    }

    @Override
    public Set<String> getContextPaths() {
        return contextPaths;
    }

    @Override
    public THttpClient addContextPaths(String... contextPaths) {
        Collections2.addAll(this.contextPaths, contextPaths);
        return this;
    }
}
