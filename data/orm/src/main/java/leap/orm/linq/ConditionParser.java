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
package leap.orm.linq;

import java.util.concurrent.atomic.AtomicInteger;

import leap.lang.params.Params;

public interface ConditionParser {
	
	/**
	 * Returns the parsed sql conditon and puts the parameters in the given {@link Params} object.
	 */
	default String parse(Condition<?> condition,Params params) {
		return parse(condition,params,new AtomicInteger(0));
	}

	/**
	 * Returns the parsed sql conditon and puts the parameters in the given {@link Params} object.
	 */
	String parse(Condition<?> condition,Params params, AtomicInteger paramsCounter);
	
}