/*
 * Copyright 2014 the original author or authors.
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
package leap.core.jdbc;

import leap.core.value.Scalars;
import leap.core.value.SimpleScalars;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleScalarsReader implements ResultSetReader<Scalars> {

    public static final SimpleScalarsReader DEFAULT_INSTANCE = new SimpleScalarsReader(1);

    @SuppressWarnings({"unchecked"})
    protected static final Scalars EMPTY_SCALARS = new SimpleScalars(Collections.EMPTY_LIST);

    protected final int column;

    public SimpleScalarsReader(int column) {
        this.column = column;
    }

    @Override
    public Scalars read(ResultSet rs) throws SQLException {
        if (rs.next()) {
            List<Object> l = new ArrayList<>();
            do {
                l.add(getColumnValue(rs));
            } while (rs.next());

            return new SimpleScalars(l);
        }

        return EMPTY_SCALARS;
    }

    protected Object getColumnValue(ResultSet rs) throws SQLException {
        return rs.getObject(column);
    }
}
