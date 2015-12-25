/*
 * Copyright 2015 the original author or authors.
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
package leap.lang.http.client;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import leap.lang.Charsets;

public class JdkHttpClient implements HttpClient {
    
    static {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){  
            public X509Certificate[] getAcceptedIssuers(){return null;}  
            public void checkClientTrusted(X509Certificate[] certs, String authType){}  
            public void checkServerTrusted(X509Certificate[] certs, String authType){}  
        }}; 
        
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        }); 
        try {
            SSLContext sc = SSLContext.getInstance("TLS");  
            sc.init(null, trustAllCerts, new SecureRandom());  
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } 
    }
    
    protected int     defaultConnectTimeout = 30 * 1000;
    protected int     defaultReadTimeout    = 30 * 1000;
    protected Charset defaultCharset        = Charsets.UTF_8;
    
    public int getDefaultConnectTimeout() {
        return defaultConnectTimeout;
    }

    public void setDefaultConnectTimeout(int defaultConnectTimeout) {
        this.defaultConnectTimeout = defaultConnectTimeout;
    }
    
    public int getDefaultReadTimeout() {
        return defaultReadTimeout;
    }

    public void setDefaultReadTimeout(int defaultReadTimeout) {
        this.defaultReadTimeout = defaultReadTimeout;
    }
    
    public Charset getDefaultCharset() {
        return defaultCharset;
    }

    public void setDefaultCharset(Charset defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    @Override
    public HttpRequest request(String url) {
        return new JdkHttpRequest(this, url);
    }
    
}
