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
package leap.db.permission;

import java.security.Permission;

public class DbSchemaPermission extends Permission {

	private static final long serialVersionUID = -1020565064307710454L;
	
	public static final String CREATE = "create";
	public static final String DROP   = "drop";
	public static final String ALTER  = "alter";
	public static final String READ   = "read";
	public static final String ANY    = "*";

	private final String actions;
	
	public DbSchemaPermission(String name,String actions) {
		super(name);
		this.actions = actions;
	}

	@Override
    public boolean implies(Permission permission) {
	    // TODO implement DbPermission.implies
	    return false;
    }

	@Override
    public boolean equals(Object obj) {
		if(!(obj instanceof DbSchemaPermission)){
			return false;
		}
		
		if(obj == this){
			return true;
		}
		
		DbSchemaPermission that = (DbSchemaPermission)obj;
	    return this.getName().equals(this.getName()) && that.getActions().equals(this.getActions());
    }

	@Override
    public int hashCode() {
	    return getName().hashCode() * 37 + actions.hashCode();
    }

	@Override
    public String getActions() {
	    return actions;
    }
}