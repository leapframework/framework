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
package leap.web.multipart;

import leap.lang.Patterns;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.web.App;
import leap.web.AppBootable;
import leap.web.route.Route;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class MultipartInitializer implements AppBootable {

	private static final Log log = LogFactory.get(MultipartInitializer.class);
	
	@Override
    public void onAppBooting(App app, ServletContext sc) throws ServletException {
		List<Route> multipartRoutes = resolveMultipartRoutes(app);
		if(multipartRoutes.isEmpty()) {
			return;
		}
		
		log.info("Found {} multipart routes",multipartRoutes.size());
		
		ServletRegistration r = findRegisteredMultipartServlet(sc);
		if(null == r){
			r = addMultipartServlet(sc);
		}
		
		registerMultipartMappings(r, multipartRoutes);
    }
	
	protected ServletRegistration addMultipartServlet(ServletContext sc) {
		Dynamic dynamic = sc.addServlet("multipart-servlet", MultipartServlet.class);

		//TODO : config multipart
		dynamic.setMultipartConfig(new MultipartConfigElement(System.getProperty("java.io.tmpdir")));
		dynamic.setLoadOnStartup(1);
		
		return dynamic; 
	}
	
	protected void registerMultipartMappings(ServletRegistration r,List<Route> routes) {
		Set<String> mappings = new HashSet<String>();
		
		for(Route route : routes) {
            String path = Paths.suffixWithoutSlash(route.getPathTemplate().getTemplateBeforeVariables());
			if(route.getPathTemplate().hasVariables()){
				path += "/*";
			}

            log.debug("Register multipart path mapping '{}'", path);
			mappings.add(path);
		}
		
		r.addMapping(mappings.toArray(new String[]{}));
	}
	
	protected ServletRegistration findRegisteredMultipartServlet(ServletContext sc) {
		for(ServletRegistration r : sc.getServletRegistrations().values()) {
			if(r.getClassName().equals(MultipartServlet.class.getName())) {
				return r;
			}
		}
		return null;
	}

	protected List<Route> resolveMultipartRoutes(App app) {
		List<Route> list = new ArrayList<Route>();
		
		for(Route route : app.routes()) {
			if(route.supportsMultipart()) {
				list.add(route);
			}
 		}
		
		return list;
	}
}