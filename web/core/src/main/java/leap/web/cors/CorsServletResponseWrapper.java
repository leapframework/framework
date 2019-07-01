/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.cors;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.util.HashSet;
import java.util.Set;

import static leap.web.cors.DefaultCorsHandler.*;

public class CorsServletResponseWrapper extends HttpServletResponseWrapper {

    protected boolean     exposeAndHeaders;
    protected Set<String> exposedHeaders;
    protected String      exposedHeadersValue;

    public CorsServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    public void setExposeAndHeaders(boolean exposeAndHeaders) {
        this.exposeAndHeaders = exposeAndHeaders;

        //init expose headers
        StringBuilder exposeHeaders = null;

        for(String header : getHeaderNames()) {
            if(isIgnoredHeader(header)) {
                continue;
            }

            if(null == exposeHeaders) {
                exposeHeaders = new StringBuilder();
            }

            if(exposeHeaders.length() > 0) {
                exposeHeaders.append(',');
            }

            exposeHeaders.append(header);
        }

        if(null != exposeHeaders) {
            exposedHeadersValue = exposeHeaders.toString();
        }
    }

    @Override
    public void setDateHeader(String name, long date) {
        super.setDateHeader(name, date);
        doAddOrSetHeader(name);
    }

    @Override
    public void addDateHeader(String name, long date) {
        super.addDateHeader(name, date);
        doAddOrSetHeader(name);
    }

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        doAddOrSetHeader(name);
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        doAddOrSetHeader(name);
    }

    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        doAddOrSetHeader(name);
    }

    @Override
    public void addIntHeader(String name, int value) {
        super.addIntHeader(name, value);
        doAddOrSetHeader(name);
    }

    protected boolean isIgnoredHeader(String name) {
        if(CORS_RESPONSE_HEADERS.contains(name)) {
            return true;
        }
        if(SIMPLE_RESPONSE_HEADERS.contains(name.toLowerCase())){
            return true;
        }
        return false;
    }

    protected void doAddOrSetHeader(String name) {
        if(!exposeAndHeaders) {
            return;
        }

        if(isIgnoredHeader(name)) {
            return;
        }

        if(null == exposedHeaders) {
            exposedHeaders = new HashSet<>(2);
        }

        if(exposedHeaders.contains(name)) {
            return;
        }

        if(exposedHeadersValue == null) {
            exposedHeadersValue = name;
        }else {
            exposedHeadersValue = exposedHeadersValue + "," + name;
        }

        super.setHeader(CorsHandler.RESPONSE_HEADER_ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeadersValue);
        exposedHeaders.add(name);
    }
}