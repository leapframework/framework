/*
 * Copyright 2014 the original author or authors.
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
package app.servlets;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.beans.TestBean;
import leap.core.AppContext;
import leap.web.Request;
import leap.web.Response;

public class TestServlet extends HttpServlet {

	private static final long serialVersionUID = 688121518423782637L;
	
	@Override
    public void init(ServletConfig config) throws ServletException {
		AppContext.factory().getBean(TestBean.class);
	}

	@Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Request  request  = Request.current();
		Response response = request.response();
		response.getWriter().write("ok");
	}
	
}