/*
 *  Copyright 2020 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package leap.orm.reader;

import leap.core.jdbc.SimpleScalarsReader;
import leap.db.DbDialect;
import leap.orm.OrmContext;

import java.sql.ResultSet;
import java.sql.SQLException;

class ScalarsReader extends SimpleScalarsReader {

    protected final DbDialect dialect;

    public ScalarsReader(OrmContext context, int column) {
        super(column);
        this.dialect = context.getDb().getDialect();
    }

    @Override
    protected Object getColumnValue(ResultSet rs) throws SQLException {
        return dialect.getColumnValue(rs, column);
    }

}
