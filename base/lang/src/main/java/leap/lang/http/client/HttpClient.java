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

import leap.lang.Charsets;
import leap.lang.path.Paths;

import java.nio.charset.Charset;

/**
 * A simple http client interface.
 * 
 * <p>
 * The implementation must supports <code>http</code> and <code>https</code> protocol.
 */
public interface HttpClient {
    
    String PREFIX_HTTP  = "http://";
    String PREFIX_HTTPS = "https://";

    /**
     * Returns the default charset.
     */
    default Charset getDefaultCharset() {
        return Charsets.UTF_8;
    }

    /**
     * Creates a new http request of the given url.
     */
    HttpRequest request(String url);

    /**
     * Creates a new http request of the given base url and relative path.
     */
    default HttpRequest request(String baseUrl, String path) {
        return request(Paths.suffixWithoutSlash(baseUrl) + Paths.prefixWithSlash(path));
    }
    
}