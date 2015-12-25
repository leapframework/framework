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

import leap.db.model.DbSequence;
import leap.lang.Args;
import leap.lang.json.JsonWriter;

public class RemoveSequenceChange extends SequenceChangeBase {

	public RemoveSequenceChange(DbSequence oldSequence) {
		super(oldSequence,null);
		Args.notNull(oldSequence,"oldSequence");
	}

	@Override
	public SchemaChangeType getChangeType() {
		return SchemaChangeType.REMOVE;
	}

	@Override
    public void toJson(JsonWriter writer) {
		writer.startObject();
		
		writer.property("sequence", oldSequence.getName());
		
		writer.endObject();	    
    }

}