/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.orm.sql.ast;

import java.util.ArrayList;
import java.util.List;

public class SqlInsert extends SqlTableContainer {

    protected SqlTableName tableName;

    protected List<SqlObjectName> columns = new ArrayList<>();
    protected List<AstNode>       values  = new ArrayList<>();

    public SqlTableName getTableName() {
        return tableName;
    }

    public void setTableName(SqlTableName tableName) {
        this.tableName = tableName;
        addTableSource(tableName);
    }

    public List<SqlObjectName> getColumns() {
        return columns;
    }

    public SqlObjectName getColumn(int i) {
        return columns.get(i);
    }

    public void addColumn(SqlObjectName column) {
        columns.add(column);
    }

    public List<AstNode> getValues() {
        return values;
    }

    public AstNode getValue(int i) {
        return values.get(i);
    }

    public void addValue(AstNode node) {
        values.add(node);
    }
}