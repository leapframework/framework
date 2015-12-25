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
package leap.web.download;

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.servlet.Servlets;
import leap.web.Request;
import leap.web.Response;

/**
 * A file content in webapp.
 */
public class ServletResourceDownload extends AbstractDownload {
	
	private static final Log log = LogFactory.get(ServletResourceDownload.class);
	
	protected String path;
	
	/**
	 * Pass the servlet path.
	 */
	public ServletResourceDownload(String path) {
		this.path = path;
	}

	@Override
    protected Resource getResource(Request request, Response response) throws Throwable {
		log.debug("Returning war file content of path : {}",path);
		return Servlets.getResource(request.getServletContext(), path);
    }
	
}