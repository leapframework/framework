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
package leap.db.platform.postgresql;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.function.Function;
import leap.core.AppContext;
import leap.db.DbPlatforms;
import leap.db.platform.GenericDbDialect;
import leap.db.platform.GenericDbMetadataReader;
import leap.db.platform.GenericDbPlatform;

public class PostgreSQLPlatform extends GenericDbPlatform {
	
	public PostgreSQLPlatform() {
		this(DbPlatforms.POSTGRESQL);
	}

	public PostgreSQLPlatform(String type) {
		this(type, productNameContainsIgnorecaseMatcher("PostgreSQL"));
	}

	public PostgreSQLPlatform(String type, Function<DatabaseMetaData, Boolean> matcher) {
		super(type, matcher);
	}

	@Override
	protected GenericDbDialect createDialect(DatabaseMetaData jdbcMetadata) throws SQLException {
		Boolean shouldQuoteIdentifier = AppContext.current().getConfig()
				.getProperty("db.dialect.shouldQuoteIdentifier", Boolean.class);
		return new PostgreSQL9Dialect(shouldQuoteIdentifier);
	}

	@Override
    protected GenericDbMetadataReader createMetadataReader(DatabaseMetaData jdbcMetadata) throws SQLException {
		return new PostgreSQL9MetadataReader();
	}
}
