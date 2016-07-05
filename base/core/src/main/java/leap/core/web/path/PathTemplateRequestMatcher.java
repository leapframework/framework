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
package leap.core.web.path;

import java.util.HashMap;

import leap.core.web.RequestBase;
import leap.lang.Args;

public class PathTemplateRequestMatcher implements PathRequestMatcher {
	
	private final PathTemplate template;

	public PathTemplateRequestMatcher(PathTemplate template) {
		Args.notNull(template);
		this.template = template;
	}
	
	@Override
    public String getPath() {
	    return template.getTemplate();
    }

	@Override
	public boolean matches(RequestBase request) {
		return template.match(request.getPath(), new HashMap<String, String>(1));
	}

	@Override
    public int compareTo(PathRequestMatcher o2) {
		if(o2 instanceof PathTemplateRequestMatcher){
			return this.template.compareTo(((PathTemplateRequestMatcher) o2).template);
		}
		return PathRequestMatcher.super.compareTo(o2);
    }
}