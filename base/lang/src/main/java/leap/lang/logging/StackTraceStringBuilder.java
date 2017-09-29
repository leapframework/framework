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
package leap.lang.logging;

public class StackTraceStringBuilder {
	
	private final String 		      message;
	private final StackTraceElement[] stes;
	
	public StackTraceStringBuilder(StackTraceElement[] stes) {
		this(null,stes);
	}

	public StackTraceStringBuilder(String message, StackTraceElement[] stes) {
		if(null == stes) {
			throw new IllegalArgumentException("The stack trace elements must not be null");
		}
		this.message = message;
		this.stes    = stes;
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		
		if(null != message) {
			s.append(message).append('\n');
		}
		
		for(StackTraceElement ste : stes) {
			s.append("at ").append(ste.toString()).append('\n');
		}
		
		return s.toString();
	}

    public String toString(String ignorePackage) {
        StringBuilder s = new StringBuilder();

        if(null != message) {
            s.append(message).append('\n');
        }

        for(StackTraceElement ste : stes) {
            String line = ste.toString();
            if(null != ignorePackage) {
                if(line.startsWith(ignorePackage)) {
                    continue;
                }
            }
            s.append("at ").append(line).append('\n');
        }

        return s.toString();
    }

}
