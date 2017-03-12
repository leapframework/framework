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

import leap.db.change.ColumnDefinitionChange;
import leap.db.change.SchemaChangeContext;
import leap.db.model.DbColumn;
import leap.db.model.DbColumnBuilder;
import leap.db.model.DbSchemaObjectName;
import leap.db.platform.GenericDbDialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by KAEL on 2017/3/11.
 */
public class SqlServerDialect extends GenericDbDialect {
    @Override
    protected String getTestDriverSupportsGetParameterTypeSQL() {
        return "select 1";
    }

    @Override
    protected String getOpenQuoteString() {
        return "\"";
    }

    @Override
    protected String getCloseQuoteString() {
        return "\"";
    }

    @Override
    protected void registerColumnTypes() {
        columnTypes.add(Types.VARCHAR,       "varchar($l)",0,65535);
    }
    
    
    @Override
    protected List<String> createSafeAlterColumnSqlsForChange(SchemaChangeContext context,
                                                              ColumnDefinitionChange change) {
        List<String> sqls = new ArrayList<String>();

        if(change.isUniqueChanged()){
            sqls.add(getAddUniqueColumnSql(change.getTable(), change.getOldColumn().getName()));

            if(change.getPropertyChanges().size() > 1){
                DbColumn c = new DbColumnBuilder(change.getNewColumn()).setUnique(false).build();
                sqls.add(getAlterColumnSql(change.getTable(), c));
            }
        }else{
            sqls.add(getAlterColumnSql(change.getTable(), change.getNewColumn()));
        }

        return sqls;
    }
    protected String getAddUniqueColumnSql(DbSchemaObjectName tableName, String columnName) {
        return "ALTER TABLE " + qualifySchemaObjectName(tableName) +
                " ADD UNIQUE(" + quoteIdentifier(columnName) + ")";
    }
    protected String getAlterColumnSql(DbSchemaObjectName tableName,DbColumn column){
        return "ALTER TABLE " + qualifySchemaObjectName(tableName) +
                " MODIFY COLUMN " + getColumnDefinitionForAlterTable(column);
    }
}
