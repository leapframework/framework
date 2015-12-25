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
package leap.web.form;

import java.util.Map;

import leap.lang.Args;
import leap.lang.Beans;


public abstract class FormBase implements Form {

	public FormBase copyFrom(Object model) {
		Args.notNull(model);
		Beans.copyProperties(model, this);
		return this;
	}
	
	public FormBase copyTo(Object model) {
		Args.notNull(model);
		Beans.copyProperties(this, model);
		return this;
	}
	
	public Map<String, Object> fields() {
		return Beans.toMap(this);
	}

}