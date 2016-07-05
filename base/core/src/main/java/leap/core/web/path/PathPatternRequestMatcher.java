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

import leap.core.web.RequestBase;
import leap.lang.Args;
import leap.lang.path.AntPathMatcher;
import leap.lang.path.PathMatcher;

public class PathPatternRequestMatcher implements PathRequestMatcher {
	
	private final String 	  path;
	private final PathMatcher matcher;
	private final boolean	  isPattern;
	private final boolean	  ignoreCase;
	private final String	  pattern;
	
	public PathPatternRequestMatcher(String pattern) {
		this(pattern,AntPathMatcher.DEFAULT_INSTANCE,false);
	}

	public PathPatternRequestMatcher(String pattern,boolean ignoreCase) {
		this(pattern,AntPathMatcher.DEFAULT_INSTANCE,ignoreCase);
	}
	
	public PathPatternRequestMatcher(String path,PathMatcher matcher, boolean ignroeCase) {
		Args.notNull(path);
		Args.notNull(matcher);
		this.path       = path;
		this.matcher    = matcher;
		this.isPattern  = matcher.isPattern(path);
		this.ignoreCase = ignroeCase;
		this.pattern    = ignroeCase ? path.toLowerCase() : path;
	}
	
	@Override
    public String getPath() {
	    return path;
    }

	@Override
	public boolean matches(RequestBase request) {
		String path = request.getPath(ignoreCase);
		return isPattern ? matcher.match(pattern, path) : pattern.equals(path);
	}
}