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

import leap.lang.New;
import leap.lang.http.Cookie;
import leap.lang.http.HTTP;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import okhttp3.CookieJar;
import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class OkTHttpClient extends THttpClientBase {

    private static final Log log = LogFactory.get(OkTHttpClient.class);

    private final DnsImpl       dns;
    private final CookieJarImpl cookieJar;
    private final OkHttpClient  httpClient;

    public OkTHttpClient(int port){
        this(port, false);
    }

    public OkTHttpClient(int port, boolean https) {
        this((https ? LOCAL_HTTPS_BASE_URL_PREFIX : LOCAL_HTTP_BASE_URL_PREFIX) + ":" + port);
    }

    public OkTHttpClient(String baseUrl) {
        super(baseUrl);

        this.dns = new DnsImpl();
        this.cookieJar = new CookieJarImpl();
        this.httpClient = newHttpClient();
    }

    OkHttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public Cookie getCookie(String name) {
        return cookieJar.get(name);
    }

    @Override
    public THttpClient addCookie(String name, String value) {
        cookieJar.add(name, value);
        return this;
    }

    @Override
    public Cookie removeCookie(String name) {
        return cookieJar.remove(name);
    }

    @Override
    public THttpClient addHostName(String hostName) {
        try {
            dns.add(hostName, InetAddress.getByName("127.0.0.1"));
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Cannot add host name '" + hostName + "', " + e.getMessage(), e);
        }
        return this;
    }

    @Override
    public THttpRequest request(String uri) {
        return new OkTHttpRequest(this, uri);
    }

    @Override
    public THttpRequest request(HTTP.Method method, String uri) {
        return new OkTHttpRequest(this, uri).setMethod(method);
    }

    protected OkHttpClient newHttpClient() {
        OkHttpClient.Builder cb = new OkHttpClient.Builder();

        cb.followRedirects(false);
        cb.followSslRedirects(false);
        cb.dns(dns);
        cb.cookieJar(cookieJar);
        cb.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
        cb.hostnameVerifier((s, sslSession) -> true);
        cb.readTimeout(30, TimeUnit.MINUTES); //for debugging

        return cb.build();
    }

    private static final class DnsImpl implements Dns {
        private final Map<String, InetAddress[]> dnsMap = new ConcurrentHashMap<>();

        public void add(final String host, final InetAddress... ips) {
            dnsMap.put(host, ips);
        }

        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            InetAddress[] addresses = dnsMap.get(hostname);
            if(null != addresses) {
                return New.arrayList(addresses);
            }else{
                return Dns.SYSTEM.lookup(hostname);
            }
        }

    }

    private static final class CookieJarImpl implements CookieJar {

        private static final Comparator<Cookie> COMPARATOR = (c1, c2) -> {
            int res = c1.getName().compareTo(c2.getName());
            if (res == 0) {
                // do not differentiate empty and null domains
                String d1 = c1.getDomain();
                if (d1 == null) {
                    d1 = "";
                } else if (d1.indexOf('.') == -1) {
                    d1 = d1 + ".local";
                }
                String d2 = c2.getDomain();
                if (d2 == null) {
                    d2 = "";
                } else if (d2.indexOf('.') == -1) {
                    d2 = d2 + ".local";
                }
                res = d1.compareToIgnoreCase(d2);
            }
            if (res == 0) {
                String p1 = c1.getPath();
                if (p1 == null) {
                    p1 = "/";
                }
                String p2 = c2.getPath();
                if (p2 == null) {
                    p2 = "/";
                }
                res = p1.compareTo(p2);
            }
            return res;
        };

        private final TreeSet<Cookie> store = new TreeSet<>(COMPARATOR);

        public Cookie get(String name) {
            for(Cookie cookie : store) {
                if(cookie.getName().equals(name)) {
                    return cookie;
                }
            }
            return null;
        }

        public void add(String name, String value) {
            okhttp3.Cookie.Builder c = new okhttp3.Cookie.Builder();
            c.name(name).value(value).domain("localhost");
            store.add(new CookieImpl(c.build()));
        }

        public Cookie remove(String name) {
            Cookie found = null;
            for(Cookie c : store) {
                if(c.getName().equals(name)) {
                    found = c;
                    break;
                }
            }
            if(null != found) {
                store.remove(found);
                return found;
            }
            return null;
        }

        @Override
        public void saveFromResponse(HttpUrl url, List<okhttp3.Cookie> cookies) {
            cookies.forEach(c -> {
                CookieImpl cookie = new CookieImpl(c);

                log.debug("Add cookie : name={}, path={}, value={},", cookie.getName(), cookie.getPath(), cookie.getValue());

                // first remove any old cookie that is equivalent
                store.remove(cookie);
                store.add(cookie);
            });
        }

        @Override
        public List<okhttp3.Cookie> loadForRequest(HttpUrl url) {
            List<okhttp3.Cookie> cookies = new ArrayList<>();
            store.forEach(c -> cookies.add(((CookieImpl)c).cookie));
            return cookies;
        }
    }

    protected static final class CookieImpl implements Cookie {
        private final okhttp3.Cookie cookie;

        public CookieImpl(okhttp3.Cookie cookie) {
            this.cookie = cookie;
        }

        @Override
        public String getName() {
            return cookie.name();
        }

        @Override
        public String getValue() {
            return cookie.value();
        }

        @Override
        public String getDomain() {
            return cookie.domain();
        }

        @Override
        public String getPath() {
            return cookie.path();
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
            return cookie.secure();
        }

        @Override
        public boolean isHttpOnly() {
            return false;
        }

        @Override
        public String toString() {
            return cookie.toString();
        }
    }
}
