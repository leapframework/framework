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

import java.util.Comparator;

public interface PathPattern {

    /**
     * The default comparator.
     */
    Comparator<PathPattern> DEFAULT_COMPARATOR = (p1, p2) -> {
        if (p1 == null && p2 == null) {
            return 0;
        }
        if (p1 == null) {
            return 1;
        }
        if (p2 == null) {
            return -1;
        }

        int i = p1.pattern().length() - p2.pattern().length();
        if(i != 0){
            return i;
        }

        return -1;
    };

	/**
	 * Returns the pattern expression.
     */
	String pattern();

	/**
	 * Returns true if the pattern matches the given path.
     */
	boolean matches(String path);

}