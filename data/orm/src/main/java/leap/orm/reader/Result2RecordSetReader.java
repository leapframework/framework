/*
 * Copyright 2020 the original author or authors.
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

package leap.orm.reader;

import leap.core.jdbc.ResultSetReader;
import leap.core.value.Record;
import leap.orm.mapping.DefaultResultSetMapping;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.ResultSetMapping;
import leap.orm.sql.SqlContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Result2RecordSetReader<T> extends DefaultEntityReader implements ResultSetReader<T> {

    private final SqlContext         sqlContext;
    private final EntityMapping      entityMapping;
    private final RecordSetReader<T> recordSetReader;

    public Result2RecordSetReader(SqlContext sqlContext, EntityMapping entityMapping, RecordSetReader<T> recordSetReader) {
        this.sqlContext = sqlContext;
        this.entityMapping = entityMapping;
        this.recordSetReader = recordSetReader;
    }

    @Override
    public T read(ResultSet rs) throws SQLException {
        final ResultSetMapping rsm = new DefaultResultSetMapping(sqlContext.getOrmContext(), sqlContext, rs, entityMapping);
        final RecordSetImpl    rsi = new RecordSetImpl(rs, rsm);
        return recordSetReader.read(rsi);
    }

    protected class RecordSetImpl implements RecordSet {
        private final ResultSet        rs;
        private final ResultSetMapping rsm;

        private Record record;

        public RecordSetImpl(ResultSet rs, ResultSetMapping rsm) {
            this.rs = rs;
            this.rsm = rsm;
        }

        @Override
        public ResultSet getResultSet() {
            return rs;
        }

        @Override
        public boolean next() throws SQLException {
            record = null;
            return rs.next();
        }

        @Override
        public Record record() throws SQLException {
            if (null == record) {
                record = readRecord(sqlContext.getOrmContext(), rs, rsm);
            }
            return record;
        }
    }
}
