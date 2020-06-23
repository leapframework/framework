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
package leap.core.variable;

public interface Variable {
	
	enum Scope {
		SESSION("session"),
		
		REQUEST("request"),

		AUTHENTICATION("authentication"),
		
		SINGLETON("singleton"),
		
		PROTOTYPE("prototype");
		
		public boolean isPrototype(){
			return this == PROTOTYPE;
		}
		
		public boolean isSingleton(){
			return this == SINGLETON;
		}
		
		public boolean isRequest(){
			return this == REQUEST;
		}

		public boolean isAuthentication() {
			return this == AUTHENTICATION;
		}
		
		public boolean isSession(){
			return this == SESSION;
		}

		private final String value;

		Scope(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
	
	Object getValue();
}