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
package leap.lang.path;

import leap.lang.Args;
import leap.lang.Strings;

public class AntPathPattern extends AbstractPathPattern {

	private final String	     pattern;
	private final AntPathMatcher matcher;
	private final String[]       parts;

	public AntPathPattern(String pattern){
		this(pattern,AntPathMatcher.DEFAULT_INSTANCE);
	}

	public AntPathPattern(String pattern,AntPathMatcher matcher) {
		Args.notEmpty(pattern,"pattern");
		this.pattern = pattern;
		this.matcher = matcher;
        this.parts   = Strings.split(pattern, '/', false);
	}

	@Override
	public String pattern() {
		return pattern;
	}

	@Override
	public boolean matches(String path) {
		return matcher.match(pattern, path);
	}

	@Override
	public int compareTo(PathPattern o) {
		if(!(o instanceof AntPathPattern)) {
			return super.compareTo(o);
		}

		AntPathPattern p = (AntPathPattern)o;
        if(pattern.equals(p.pattern)) {
            return 0;
        }

        String[] parts1 = parts;
        String[] parts2 = p.parts;

        int min = Math.min(parts1.length, parts2.length);
        for(int i=0;i<min;i++) {
            String part1 = parts1.length > i ? parts1[i] : null;
            String part2 = parts2.length > i ? parts2[i] : null;

            if(part1 != null && part2 == null) {
                return -1;
            }

            if(part1 == null && part2 == null) {
                return 1;
            }

            if(part1.equals(part2)) {
                continue;
            }

            if(part1.contains("**")) {
                return 1;
            }

            if(part2.contains("**")) {
                return -1;
            }

            if(part1.contains("*")) {
                return 1;
            }

            if(part2.contains("*")) {
                return -1;
            }
        }

        return 0;
	}
}