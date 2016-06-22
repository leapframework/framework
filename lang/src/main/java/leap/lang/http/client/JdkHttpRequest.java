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

import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.HTTP;
import leap.lang.http.Headers;
import leap.lang.http.QueryStringBuilder;
import leap.lang.http.exception.HttpException;
import leap.lang.http.exception.HttpIOException;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.net.Urls;
import leap.lang.time.StopWatch;
import leap.lang.value.ImmutableNamedValue;
import leap.lang.value.NamedValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

public class JdkHttpRequest implements HttpRequest {
    
    private static final Log log = LogFactory.get(JdkHttpRequest.class);
    
    protected final JdkHttpClient client;
    protected final String        url;
    protected final boolean       ssl;
    
    protected final Map<String, String>      cookies     = new LinkedHashMap<>();
    protected final SimpleHttpHeaders        headers     = new SimpleHttpHeaders();
    protected final QueryStringBuilder       queryParams = new QueryStringBuilder();
    protected final List<NamedValue<String>> formParams  = new ArrayList<>();
    
    protected int         connectTimeout;
    protected int         readTimeout;
    protected Charset     charset;
    protected InputStream content;
    protected HTTP.Method method;
    
    private boolean form;
    
    protected JdkHttpRequest(JdkHttpClient client, String url) {
        Args.notEmpty(url, "url");
        Args.assertTrue(Strings.startsWithIgnoreCase(url, HttpClient.PREFIX_HTTP) ||
                        Strings.startsWithIgnoreCase(url, HttpClient.PREFIX_HTTPS), 
                        "The url must prefix with http:// or https://");
        this.client = client;
        this.url = url;
        this.ssl = Strings.startsWithIgnoreCase(url, HttpClient.PREFIX_HTTPS);
        
        this.connectTimeout = client.getDefaultConnectTimeout();
        this.readTimeout    = client.getDefaultReadTimeout();
        this.charset        = client.getDefaultCharset();
    }
    
    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public HttpRequest setCookie(String name, String value) {
        Args.notEmpty(name, "name");
        cookies.put(name, value);
        return this;
    }

    @Override
    public HttpRequest setHeader(String name, String value) {
        Args.notEmpty(name, "name");
        headers.set(name, value);
        return this;
    }

    @Override
    public HttpRequest addHeader(String name, String value) {
        Args.notEmpty(name);
        headers.add(name, value);
        return this;
    }

    @Override
    public HttpRequest addQueryParam(String name, String value) {
        Args.notEmpty(name, "name");
        queryParams.add(name, value);
        return this;
    }

    @Override
    public HttpRequest addFormParam(String name, String value) {
        Args.notEmpty(name, "name");
        formParams.add(new ImmutableNamedValue<String>(name, value));
        return this;
    }

    @Override
    public HttpRequest setMethod(HTTP.Method method) {
        this.method = method;
        return this;
    }

    @Override
    public HttpRequest setBody(byte[] data) {
        Args.notNull(data, "data");
        this.content = new ByteArrayInputStream(data);
        return this;
    }

    @Override
    public HttpRequest setBody(InputStream is) {
        Args.notNull(is);
        this.content = is;
        return this;
    }

    @Override
    public HttpResponse get() {
        return send(HTTP.Method.GET);
    }

    @Override
    public HttpResponse post() {
        return send(HTTP.Method.POST);
    }

    @Override
    public HttpResponse send() {
        String connUrl = url;

        if(!queryParams.isEmpty()) {
            connUrl = Urls.appendQueryString(connUrl, queryParams.build());
        }

        if(null == method) {
            if(form || null != content) {
                method = HTTP.Method.POST;
            }else{
                method = HTTP.Method.GET;
            }
        }

        return doSend(method, connUrl);

    }

    protected boolean hasHeader(String name) {
        return headers.exists(name);
    }

    protected JdkHttpResponse doSend(HTTP.Method m, String connUrl) {
        try {
            log.debug("Sending '{}' request to url : {}", m, connUrl);
            
            StopWatch sw = StopWatch.startNew();
            
            final URLConnection raw = new URL(connUrl).openConnection();
            if(!(raw instanceof HttpURLConnection)) {
                throw new IllegalStateException("Url opens '" + raw.getClass().getName() + 
                                                "' instead of expected HttpURLConnection");
            }
            
            final HttpURLConnection conn = (HttpURLConnection)raw;
            
            setConnParams(conn, m);
            setHeaders(conn);
            setCookies(conn);
            
            if(m == HTTP.Method.POST || m == HTTP.Method.PUT || m == HTTP.Method.PATCH) {
                conn.setDoOutput(true);
                
                try(final InputStream body = getBody()) {
                    if(null != body) {
                        
                        //Set form content type.
                        if(form && !hasHeader(Headers.CONTENT_TYPE)) {
                            conn.setRequestProperty(Headers.CONTENT_TYPE, 
                                                    ContentTypes.create(ContentTypes.APPLICATION_FORM_URLENCODED, charset.name()));
                        }
                        
                        log.debug("writing body content");
                        
                        //Writes content.
                        try(final OutputStream out = conn.getOutputStream()){
                            IO.copy(body, out);
                        }
                    }
                }
            }
            
            log.debug("Responsed, used {}ms", sw.getElapsedMilliseconds());
            return new JdkHttpResponse(client, this, conn);
        } catch (MalformedURLException e) {
            throw new HttpException("Invalid url : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new HttpIOException(e);
        }
    }
    
    protected void setConnParams(HttpURLConnection conn, HTTP.Method m) throws IOException {
        if(connectTimeout > 0) {
            conn.setConnectTimeout(connectTimeout);
        }

        if(readTimeout > 0) {
            conn.setReadTimeout(readTimeout);
        }
        
        conn.setRequestMethod(m.name());
        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(false);
    }
    
    protected void setHeaders(HttpURLConnection conn) throws IOException {
        headers.forEach((name,value) -> conn.addRequestProperty(name, value));
    }
    
    protected void setCookies(HttpURLConnection conn) throws IOException {
        if(!cookies.isEmpty()) {
            StringBuilder header = new StringBuilder();
            int i=0;
            for(Entry<String, String> cookie : cookies.entrySet()) {
                if(i>0) {
                    header.append(';');
                }
                i++;
                header.append(cookie.getKey()).append('=').append(cookie.getValue());
            }
            conn.setRequestProperty(Headers.COOKIE, header.toString());
        }
    }
    
    protected InputStream getBody() throws IOException {
        if(null != content) {
            return content;
        }
        
        if(!formParams.isEmpty()) {
            form = true;
            return getFormInputStream();
        }
        
        return null;
    }
    
    protected InputStream getFormInputStream() throws IOException {
        final StringBuilder content = new StringBuilder();
        for (final NamedValue<String> parameter : formParams) {
            final String encodedName = Urls.encode(parameter.getName(), charset.name());
            final String encodedValue = Urls.encode(parameter.getValue(), charset.name());
            if (content.length() > 0) {
                content.append('&');
            }
            content.append(encodedName);
            if (encodedValue != null) {
                content.append('=');
                content.append(encodedValue);
            }
        }
        return new ByteArrayInputStream(Strings.getBytesUtf8(content.toString()));
    }

}