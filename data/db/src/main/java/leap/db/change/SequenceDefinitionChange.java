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

import leap.db.model.DbSequence;
import leap.lang.Args;
import leap.lang.json.JsonWriter;

public class SequenceDefinitionChange extends SequenceChangeBase {
	
	protected final List<SequencePropertyChange> propertyChanges;

	public SequenceDefinitionChange(DbSequence oldSequence, DbSequence newSequence,List<SequencePropertyChange> propertyChanges) {
		super(oldSequence,newSequence);
		Args.notNull(oldSequence,"oldSequence");
		Args.notNull(newSequence,"newSequence");
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
		
		writer.property("oldSequence", oldSequence)
		      .property("newSequence", newSequence);
		
		writer.endObject();	    
    }
}