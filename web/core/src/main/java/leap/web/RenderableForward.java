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
package leap.web;

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;

public class RenderableForward implements Renderable {
	
	private static final Log log = LogFactory.get(RenderableForward.class);
	
	public static final String VIEWS_LOCATION_PREFIX = "views:";
	
	private final String path;
	
	public RenderableForward(String path){
		this.path = path;
	}

	@Override
    public void render(Request request, Response response) throws Exception {
		String forwardPath;
		
		if(path.startsWith(VIEWS_LOCATION_PREFIX)){
			forwardPath = path.substring(VIEWS_LOCATION_PREFIX.length());
			
			if(!forwardPath.startsWith("/")){
				forwardPath = Paths.applyRelative(request.getPath(), forwardPath);
			}
			
			forwardPath = request.app().getWebConfig().getViewsLocation() + forwardPath;
			
		}else {
			if(!path.startsWith("/")){
				forwardPath = Paths.applyRelative(request.getPath(), path);
			}else{
				forwardPath = path;
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Forwarding to resource [" + forwardPath + "]");
		}	
		
		// Note: The forwarded resource is supposed to determine the content type itself.
		request.getServletRequest().getRequestDispatcher(forwardPath)
								   .forward(request.getServletRequest(), response.getServletResponse());
    }

	@Override
    public String toString() {
		return "{Forward:'" + path + "'}";
    }
	
	
}