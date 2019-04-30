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
package leap.webunit.client;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import leap.lang.Args;
import leap.lang.Charsets;
import leap.lang.Collections2;
import leap.lang.http.Cookie;
import leap.lang.http.HTTP.Method;

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.CookieIdentityComparator;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.InMemoryDnsResolver;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;

class ApacheTHttpClient extends THttpClientBase {

    private static final Log log = LogFactory.get(ApacheTHttpClient.class);

	private final DnsResolverImpl dnsResolver;
	private final CookieStoreImpl cookieStore;
	private final HttpClient 	  httpClient;

	public ApacheTHttpClient(int port){
		this(port, false);
	}
	
    public ApacheTHttpClient(int port, boolean https) {
        this((https ? LOCAL_HTTPS_BASE_URL_PREFIX : LOCAL_HTTP_BASE_URL_PREFIX) + ":" + port);
    }
	
	public ApacheTHttpClient(String baseUrl){
        super(baseUrl);

		this.dnsResolver = new DnsResolverImpl();
		this.cookieStore = new CookieStoreImpl();
		this.httpClient  = createDefaultHttpClient();
	}
	
    public HttpClient getHttpClient(){
		return httpClient;
	}

    @Override
    public Cookie getCookie(String name) {
		for(org.apache.http.cookie.Cookie cookie : cookieStore.getCookies()) {
			if(cookie.getName().equals(name)) {
				return new CookieImpl(cookie);
			}
		}
	    return null;
    }

    @Override
    public THttpClient addCookie(String name, String value) {
        BasicClientCookie c = new BasicClientCookie(name, value);
        c.setDomain("localhost");
        cookieStore.addCookie(c);
        return this;
    }

    @Override
    public Cookie removeCookie(String name) {
		org.apache.http.cookie.Cookie removed = cookieStore.removeCookie(name);
		return null == removed ? null : new CookieImpl(removed);
    }

	@Override
    public THttpClient addHostName(String hostName) {
		try {
	        dnsResolver.add(hostName, InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
        	throw new IllegalStateException("Cannot add host name '" + hostName + "', " + e.getMessage(), e);
        }
	    return this;
    }

	@Override
    public THttpRequest request(String uri) {
        return new ApacheTHttpRequest(this, uri);
    }

    public THttpRequest request(Method method,String uri){
		return new ApacheTHttpRequest(this,uri).setMethod(method);
	}
	
	protected HttpClient createDefaultHttpClient() {
		HttpClientBuilder cb = HttpClientBuilder.create();
		
		//TODO : small buffer size will cause socket closed when reading response entity?
		PoolingHttpClientConnectionManager cm = 
				new PoolingHttpClientConnectionManager(getDefaultRegistry(), this.dnsResolver);
		cm.setDefaultConnectionConfig(ConnectionConfig.custom().setBufferSize(1024 * 1024).build());

		cb.setConnectionManager(cm);
		cb.setDefaultCookieStore(this.cookieStore);
		cb.setRedirectStrategy(new DefaultRedirectStrategy() {
			@Override
			public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
				if(!autoRedirect){
					return false;
				}
				return super.isRedirected(request, response, context);
			}
		});
		
		return cb.build();
    }
	
    private static Registry<ConnectionSocketFactory> getDefaultRegistry() {
        RegistryBuilder<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create();
                
        reg.register("http", PlainConnectionSocketFactory.getSocketFactory());
        
        SSLConnectionSocketFactory sslSocketFactory = 
                new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        
        reg.register("https", sslSocketFactory);
        
        return reg.build();
    }
    
    protected static final class DnsResolverImpl implements DnsResolver {
    	
    	private final InMemoryDnsResolver      inMemoryResolver   = new InMemoryDnsResolver();
    	private final SystemDefaultDnsResolver sysDefaultResolver = SystemDefaultDnsResolver.INSTANCE;
    	
        public void add(final String host, final InetAddress... ips) {
            inMemoryResolver.add(host, ips);
        }

		@Override
        public InetAddress[] resolve(String host) throws UnknownHostException {
			try {
	            return inMemoryResolver.resolve(host);
            } catch (UnknownHostException e) {
            	return sysDefaultResolver.resolve(host);
            }
        }
    	
    }
    
    protected static final class CookieStoreImpl implements CookieStore, Serializable {

        private static final long serialVersionUID = 9107080066556967263L;
		private final TreeSet<org.apache.http.cookie.Cookie> cookies;

        public CookieStoreImpl() {
            this.cookies = new TreeSet<>(new CookieIdentityComparator());
        }
        
        public synchronized org.apache.http.cookie.Cookie removeCookie(String name) {
        	org.apache.http.cookie.Cookie found = null;
        	for(org.apache.http.cookie.Cookie c : cookies) {
        		if(c.getName().equals(name)) {
        			found = c;
        			break;
        		}
        	}
        	if(null != found) {
        		cookies.remove(found);
        		return found;
        	}
        	
        	return null;
        }

        public synchronized void addCookie(final org.apache.http.cookie.Cookie cookie) {
            if (cookie != null) {
                log.debug("Add cookie : name={}, path={}, value={},", cookie.getName(), cookie.getPath(), cookie.getValue());
                // first remove any old cookie that is equivalent
                cookies.remove(cookie);
                if (!cookie.isExpired(new Date())) {
                    cookies.add(cookie);
                }
            }
        }

        public synchronized void addCookies(final org.apache.http.cookie.Cookie[] cookies) {
            if (cookies != null) {
                for (final org.apache.http.cookie.Cookie cooky : cookies) {
                    this.addCookie(cooky);
                }
            }
        }

        public synchronized List<org.apache.http.cookie.Cookie> getCookies() {
            //create defensive copy so it won't be concurrently modified
            return new ArrayList<>(cookies);
        }

        public synchronized boolean clearExpired(final Date date) {
            if (date == null) {
                return false;
            }
            boolean removed = false;
            for (final Iterator<org.apache.http.cookie.Cookie> it = cookies.iterator(); it.hasNext();) {
                if (it.next().isExpired(date)) {
                    it.remove();
                    removed = true;
                }
            }
            return removed;
        }

        /**
         * Clears all cookies.
         */
        public synchronized void clear() {
            cookies.clear();
        }

        @Override
        public synchronized String toString() {
            return cookies.toString();
        }
    }    
    
    protected static final class CookieImpl implements Cookie {
    	private final org.apache.http.cookie.Cookie cookie;

		public CookieImpl(org.apache.http.cookie.Cookie cookie) {
			this.cookie = cookie;
		}

		@Override
        public String getName() {
	        return cookie.getName();
        }

		@Override
        public String getValue() {
	        return cookie.getValue();
        }

		@Override
        public String getDomain() {
	        return cookie.getDomain();
        }

		@Override
        public String getPath() {
	        return cookie.getPath();
        }

		@Override
        public int getMaxAge() {
	        return -1;
        }

		@Override
        public String getComment() {
	        return null;
        }

		@Override
        public boolean isSecure() {
	        return cookie.isSecure();
        }

		@Override
        public boolean isHttpOnly() {
	        return false;
        }
    }
}