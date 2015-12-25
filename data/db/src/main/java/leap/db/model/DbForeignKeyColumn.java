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
package leap.db.model;

import leap.lang.Strings;
import leap.lang.json.JsonStringable;
import leap.lang.json.JsonWriter;

public class DbForeignKeyColumn implements JsonStringable {
	
	protected final String localColumnName;
	protected final String foreignColumnName;
	
	public DbForeignKeyColumn(String localColumnName, String foreignColumnName) {
	    this.localColumnName   = localColumnName;
	    this.foreignColumnName = foreignColumnName;
    }

	public String getLocalColumnName() {
		return localColumnName;
	}

	public String getForeignColumnName() {
		return foreignColumnName;
	}

	@Override
    public String toString() {
		StringBuffer result = new StringBuffer();

		result.append(getLocalColumnName());
		result.append(" -> ");
		result.append(getForeignColumnName());

		return result.toString();
    }
	
	public boolean equalsIgnoreCase(DbForeignKeyColumn that){
		if(null == that){
			return false;
		}
		if(!Strings.equalsIgnoreCase(this.localColumnName, that.getLocalColumnName())){
			return false;
		}
		if(!Strings.equalsIgnoreCase(this.foreignColumnName, that.getForeignColumnName())){
			return false;
		}
		return true;
	}

	@Override
    public void toJson(JsonWriter writer) {
		writer.startObject();
		
		writer.property("localColumnName", localColumnName)
			  .property("foreignColumnName", foreignColumnName);
		
		writer.endObject();
		
    }
}
