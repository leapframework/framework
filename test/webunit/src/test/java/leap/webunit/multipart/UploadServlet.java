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
package leap.webunit.multipart;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import leap.lang.Assert;

public class UploadServlet extends HttpServlet {

	private static final long serialVersionUID = 1202829001210758758L;
	
	private boolean inited;
	
	@Override
    public void init(ServletConfig config) throws ServletException {
		String v = (String)config.getServletContext().getAttribute("test");
		Assert.isTrue("hello".equals(v));
		this.inited = true;
	}

	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Assert.isTrue(inited);
		
	    Collection<Part> parts = req.getParts();

	    StringBuilder sb = new StringBuilder();
	    
	    int i=0;
	    for(Part part : parts) {
	        if(i > 0) {
	            sb.append(',');   
	        }
	        sb.append(part.getName());
	        i++;
	    }
	    
	    resp.getWriter().write(sb.toString());
	    
	    System.out.println(sb.toString());
	}
	
}
