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

import java.util.Collections;
import java.util.List;

import leap.db.model.DbColumn;
import leap.db.model.DbTable;
import leap.lang.Args;
import leap.lang.Iterables;
import leap.lang.annotation.Immutable;
import leap.lang.json.JsonWriter;

public class ColumnDefinitionChange extends ColumnChangeBase {
	
	protected final List<ColumnPropertyChange> propertyChanges;

	public ColumnDefinitionChange(DbTable table, DbColumn oldColumn, DbColumn newColumn,List<ColumnPropertyChange> propertyChanges) {
		super(table, oldColumn, newColumn);
		Args.notEmpty(propertyChanges);
		this.propertyChanges = Collections.unmodifiableList(propertyChanges);
	}

	@Override
    public SchemaChangeType getChangeType() {
	    return SchemaChangeType.UPDATE;
    }
	
	@Override
    public void toJson(JsonWriter writer) {
		writer.startObject();
		
		writer.property("table", table.getName())
			  .property("oldColumn",oldColumn)
			  .property("newColumn",newColumn);
		
		writer.endObject();
    }

	public @Immutable List<ColumnPropertyChange> getPropertyChanges() {
		return propertyChanges;
	}
	
	public boolean isSizeChanged(){
		return Iterables.any(propertyChanges, PropertyChange.nameEquals(ColumnPropertyChange.SIZE));
	}
	
	public boolean isTypeChanged(){
		return Iterables.any(propertyChanges, PropertyChange.nameEquals(ColumnPropertyChange.TYPE));
	}
	
	public boolean isNullableChanged(){
		return Iterables.any(propertyChanges, PropertyChange.nameEquals(ColumnPropertyChange.NULLABLE));
	}
	
	public boolean isUniqueChanged(){
		return Iterables.any(propertyChanges, PropertyChange.nameEquals(ColumnPropertyChange.UNIQUE));
	}
	
	public boolean isDefaultChanged(){
		return Iterables.any(propertyChanges, PropertyChange.nameEquals(ColumnPropertyChange.DEFAULT));
	}
	
	public boolean isCommentChanged(){
		return Iterables.any(propertyChanges, PropertyChange.nameEquals(ColumnPropertyChange.COMMENT));
	}
}