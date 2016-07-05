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
package leap.core.ioc;

import java.util.List;

public interface BeanList<T> extends List<T> {
	
	/**
	 * Adds the given to the first of this list. 
	 */
	default void addFirst(T bean){
		add(0,bean);
	}

	/**
	 * Removes all the beans in this list of the given type.
	 */
	List<T> removeAll(Class<? extends T> type);
	
}