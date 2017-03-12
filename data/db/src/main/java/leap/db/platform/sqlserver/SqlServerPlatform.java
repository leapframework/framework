/*
 * Copyright 2017 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.db.platform.sqlserver;

import leap.db.DbPlatforms;
import leap.db.platform.GenericDbDialect;
import leap.db.platform.GenericDbMetadata;
import leap.db.platform.GenericDbMetadataReader;
import leap.db.platform.GenericDbPlatform;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Created by KAEL on 2017/3/11.
 */
public class SqlServerPlatform extends GenericDbPlatform {
    public SqlServerPlatform() {
        this(DbPlatforms.SQLSERVER);
    }

    public SqlServerPlatform(String type) {
        super(type,productNameContainsIgnorecaseMatcher("Microsoft SQL Server"));
    }

    @Override
    protected GenericDbDialect createDialect(DatabaseMetaData jdbcMetadata) throws SQLException {
        return new SqlServerDialect();
    }
    @Override
    protected GenericDbMetadataReader createMetadataReader(DatabaseMetaData jdbcMetadata) throws SQLException {
        return new SqlServerMetadataReader();
    }

    @Override
    protected GenericDbMetadata createMetadata(Connection connection, DatabaseMetaData jdbcMetadata,
                                               String defaultSchemaName,
                                               GenericDbMetadataReader metadataReader) throws SQLException {
        return new SqlServerMetadata(jdbcMetadata,defaultSchemaName,metadataReader);
    }
}
