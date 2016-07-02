/*
 * Copyright 2016 the original author or authors.
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
package leap.core.web;

import leap.core.AppContext;
import leap.lang.Classes;
import leap.lang.Strings;

import javax.servlet.*;
import java.io.IOException;

public class ServletProxy implements Servlet {

    public static final String INIT_PARAM_SERVLET_CLASS = "servlet-class";

    private Servlet       servlet;
    private ServletConfig config;

    @Override
    public void init(ServletConfig config) throws ServletException {
        String servletClassName = config.getInitParameter(INIT_PARAM_SERVLET_CLASS);
        if(Strings.isEmpty(servletClassName)) {
            throw new ServletException("The init param '" + INIT_PARAM_SERVLET_CLASS + "' must be configured");
        }

        Class<? extends Servlet> servletClass = (Class<? extends Servlet>)Classes.forName(servletClassName);

        AppContext context = AppContext.get(config.getServletContext());
        if(null == context) {
            throw new ServletException("AppContext must be initialized!");
        }

        this.config = config;

        servlet = context.getBeanFactory().getOrCreateBean(servletClass);
        servlet.init(config);
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        servlet.service(req, res);
    }

    @Override
    public String getServletInfo() {
        return servlet.getServletInfo();
    }

    @Override
    public void destroy() {
        servlet.destroy();
    }
}
