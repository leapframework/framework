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
package tested;

import leap.core.AppPropertyProcessor;
import leap.lang.Out;

public class DupPropertyProcessor implements AppPropertyProcessor {

	@Override
	public boolean process(String name, String value, Out<String> newValue) {
		if(value.startsWith("dup:")) {
			String v = value.substring(4);
			newValue.set(v + v);
			return true;
		}
		return false;
	}

}
