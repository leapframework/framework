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
package leap.web;

import leap.lang.net.Urls;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.lang.servlet.Servlets;
import leap.web.download.ResourceDownload;

public class RenderableDownload implements Renderable {
	
	protected final String path;
	
	public RenderableDownload(String path) {
		this.path = path;
	}

	@Override
	public void render(Request request, Response response) throws Throwable {
		Resource resource;

		if(path.startsWith("/")) {
			resource = Servlets.getResource(request.getServletContext(), path);
		}else if(Urls.hasProtocolPrefix(path)) {
			resource = Resources.getResource(path);
		}else{
			resource = Servlets.getResource(request.getServletContext(), 
									 		Paths.applyRelative(request.getPath(), path));
		}

		new ResourceDownload(resource).render(request, response);
	}

	@Override
    public String toString() {
		return "{Download:" + path + "}";
	}
}