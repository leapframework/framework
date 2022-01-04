/*
 * Copyright 2021 the original author or authors.
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
package leap.db.platform.kingbase;

import leap.db.DbPlatforms;
import leap.db.platform.GenericDbDialect;
import leap.db.platform.GenericDbMetadataReader;
import leap.db.platform.GenericDbPlatform;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class KingBasePlatform extends GenericDbPlatform {

    public KingBasePlatform() {
        this(DbPlatforms.KINDBASE);
    }

    protected KingBasePlatform(String type) {
        super(type, productNameContainsIgnorecaseMatcher("KingBase"));
    }

    @Override
    protected GenericDbDialect createDialect(DatabaseMetaData jdbcMetadata) throws SQLException {
        return new KingBase8Dialect();
    }

    @Override
    protected GenericDbMetadataReader createMetadataReader(DatabaseMetaData jdbcMetadata) throws SQLException {
        if (jdbcMetadata.getDatabaseMajorVersion() >= 8) {
            return new KingBase8MetadataReader();
        }
        return new KingBase7MetadataReader();
    }
}