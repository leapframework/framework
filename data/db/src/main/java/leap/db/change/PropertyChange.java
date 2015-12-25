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
package leap.db.change;

import java.util.Objects;
import java.util.function.Predicate;

import leap.db.model.DbObject;
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.json.JsonWriter;

public abstract class PropertyChange<T extends DbObject> extends SchemaChangeBase {

	protected final T      dbObject;
	protected final String property;
	protected final Object oldValue;
	protected final Object newValue;
	
	public PropertyChange(T dbObject,String property, Object oldValue, Object newValue) {
		Args.notNull(dbObject,"dbObject");
		Args.notEmpty(property,"property");
		Args.assertFalse(Objects.equals(oldValue, newValue), "oldValue must not equals to newValue");
		
		this.dbObject = dbObject;
	    this.property = property;
	    this.oldValue = oldValue;
	    this.newValue = newValue;
    }
	
	public T getDbObject() {
		return dbObject;
	}

	public String getProperty() {
		return property;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}

	@Override
    public SchemaChangeType getChangeType() {
	    return SchemaChangeType.UPDATE;
    }
	
	@Override
    public void toJson(JsonWriter writer) {
		writer.startObject();
		
		writer.property("property", property)
			  .key("oldValue").value(oldValue)
			  .key("newValue").value(newValue);
		
		writer.endObject();
    }

	protected static <T extends PropertyChange<?>> Predicate<T> nameEquals(final String name){
		return new Predicate<T>() {
			public boolean test(T object) {
				return Strings.equals(object.getProperty(), name);
			}
		};
	}
}