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

import leap.db.platform.GenericDbMetadataReader;
import leap.lang.jdbc.ResultSetWrapper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by KAEL on 2017/3/11.
 */
public class SqlServerMetadataReader extends GenericDbMetadataReader {
    @Override
    protected MetadataParameters createMetadataParameters(Connection connection, DatabaseMetaData dm, String catalog, String schema) {
        MetadataParameters p = super.createMetadataParameters(connection, dm, catalog, schema);
        
        p.schema = "dbo";
        p.schemaPattern = "%";
        
        return p;
    }
    
    @Override
    protected ResultSet getTables(Connection connection, DatabaseMetaData dm,
                                  MetadataParameters params) throws SQLException {
        return new SqlServerResultSetWrapper(super.getTables(connection,dm,params));
    }
    
    
    
    private class SqlServerResultSetWrapper extends ResultSetWrapper{

        public SqlServerResultSetWrapper(ResultSet rs) {
            super(rs);
        }

        @Override
        public Statement getStatement() throws SQLException {
            return null;
        }
    }
}
