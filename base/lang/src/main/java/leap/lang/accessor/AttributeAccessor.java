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

import java.util.function.Supplier;

/**
 * Interface defining a generic contract for attaching and accessing metadata to/from arbitrary objects.
 */
public interface AttributeAccessor extends AttributeGetter,AttributeSetter {
	Object NULL = new Object();

	/**
	 * Removes the attribute.
	 */
	void removeAttribute(String name);

	/**
	 * Returns the exists attribute value or set the value if not exists.
	 */
	default <T> T getSetAttribute(String name, Supplier<T> supplier) {
		T value = (T)getAttribute(name);
		if(null == value) {
			value = supplier.get();
			if(null != value) {
				setAttribute(name, value);
			}
		}
		return value;
	}

	/**
	 * Returns the exists attribute value or set the value if not exists.
	 */
	default <T> T getSetNullAttribute(String name, Supplier<T> supplier) {
		Object value = (T)getAttribute(name);
		if(null == value) {
			value = supplier.get();
			if(null != value) {
				value = NULL;
				setAttribute(name, value);
			}
		}
		return value == NULL ? null : (T)value;
	}
}