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

import java.sql.DatabaseMetaData;

public class DbSchemaName extends DbNamedObject {

    public static final String UNSUPPORTED = "unsupported";

	protected final String catalog;

	public DbSchemaName(String catalog,String name) {
	    super(name);
	    this.catalog = catalog;
    }

	/**
	 * @see DatabaseMetaData#getCatalogs()
	 */
	public String getCatalog() {
		return catalog;
	}

}