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

import leap.db.model.DbIndex;
import leap.lang.Strings;

public class IndexPropertyChange extends PropertyChange<DbIndex> {
	
	public static final String UNIQUE  = "unique";
	public static final String COLUMNS = "columns";

	public IndexPropertyChange(DbIndex object, String property, Object oldValue, Object newValue) {
	    super(object, property, oldValue, newValue);
    }

	@Override
    public SchemaObjectType getObjectType() {
	    return SchemaObjectType.INDEX;
    }
	
	public boolean isUniqueChanged(){
		return Strings.equals(getProperty(), UNIQUE);
	}
	
	public boolean isColumnsChanged(){
		return Strings.equals(getProperty(), COLUMNS);
	}
	
}