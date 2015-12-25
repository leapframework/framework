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
package leap.orm.model;

import java.lang.reflect.Field;

import leap.lang.Args;
import leap.lang.Named;

public class ModelField implements Named {
	
	private final String   name;
	private final Class<?> type;
	private final Field    field;
	
	public ModelField(String name,Class<?> type,Field field){
		Args.notNull(name,"name");
		Args.notNull(type,"type");
		Args.notNull(field,"field");
		this.name  = name;
		this.type  = type;
		this.field = field;
	}
	
	@Override
    public String getName() {
	    return name;
    }

	public Class<?> getType() {
		return type;
	}
	
	public Field getField() {
		return field;
	}
}