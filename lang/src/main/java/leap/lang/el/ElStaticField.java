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
package leap.lang.el;

import leap.lang.Args;
import leap.lang.reflect.ReflectField;

public class ElStaticField implements ElProperty {
	
	private final ReflectField field;

	public ElStaticField(ReflectField field) {
		Args.assertTrue(field.isStatic(),"Must be static field");
		this.field = field;
	}
	
	@Override
	public Object getValue(ElEvalContext context, Object instance) throws Throwable {
		return field.getValue(null);
	}

}