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
package leap.lang.meta;

import java.util.regex.Pattern;

import leap.lang.Args;

public class MPattern extends ImmutableMNamed {
	
	protected final Pattern pattern;

	public MPattern(String name,Pattern pattern) {
	    super(name);
	    Args.notNull(pattern,"pattern");
	    this.pattern = pattern;
    }
	
	public MPattern(String name, String title, Pattern pattern) {
	    super(name, title);
	    Args.notNull(pattern,"pattern");
	    this.pattern = pattern;
    }

	public Pattern getPattern() {
		return pattern;
	}

}