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

import java.util.List;

import leap.db.model.DbIndex;
import leap.db.model.DbTable;
import leap.lang.Args;
import leap.lang.json.JsonWriter;

public class IndexDefinitionChange extends IndexChangeBase {
	
	protected final List<IndexPropertyChange> propertyChanges;

	public IndexDefinitionChange(DbTable table, DbIndex oldIndex, DbIndex newIndex,List<IndexPropertyChange> propertyChanges) {
		super(table, oldIndex, newIndex);
		Args.notNull(oldIndex,"oldIndex");
		Args.notNull(newIndex,"newIndex");
		Args.notEmpty(propertyChanges,"propertyChanges");
		this.propertyChanges = propertyChanges;
	}

	@Override
	public SchemaChangeType getChangeType() {
		return SchemaChangeType.UPDATE;
	}

	@Override
    public void toJson(JsonWriter writer) {
		writer.startObject();
		
		writer.property("table", table.getName())
			  .property("oldIndex", oldIndex)
			  .property("newIndex", newIndex);
		
		writer.endObject();	    
    }
	
}
