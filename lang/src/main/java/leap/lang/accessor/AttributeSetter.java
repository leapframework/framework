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
package leap.lang.accessor;

public interface AttributeSetter {
	
	/**
	 * Set the attribute defined by {@code name} to the supplied {@code value}. If {@code value} is {@code null}, the
	 * attribute is {@link #removeAttribute removed}.
	 * 
	 * <p>
	 * In general, users should take care to prevent overlaps with other metadata attributes by using fully-qualified
	 * names, perhaps using class or package names as prefix.
	 * 
	 * @param name the unique attribute key
	 * @param value the attribute value to be attached
	 */
	void setAttribute(String name, Object value);

}
