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
package leap.orm.reader;

import leap.core.exception.EmptyRecordsException;
import leap.core.exception.TooManyRecordsException;
import leap.core.jdbc.RawScalarReader;
import leap.core.jdbc.ResultSetReader;
import leap.core.value.Scalar;
import leap.core.value.Scalars;
import leap.orm.OrmContext;
import leap.orm.mapping.EntityMapping;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlResultSetReader;

import java.util.List;

public class ResultSetReaders {

    private static final ResultSetReader<Boolean> CHECK_EXISTS_READER = rs -> rs.next();

    public static ResultSetReader<Boolean> forCheckExists() {
        return CHECK_EXISTS_READER;
    }

    public static ResultSetReader<Scalar> forScalar(final OrmContext context) throws EmptyRecordsException, TooManyRecordsException {
        return new ScalarReader(context, 1);
    }

    public static ResultSetReader<Scalars> forScalars(final OrmContext context) throws EmptyRecordsException, TooManyRecordsException {
        return new ScalarsReader(context, 1);
    }

    public static <T> ResultSetReader<T> forScalarValue(final Class<T> targetType, final boolean checkEmptyResult) throws EmptyRecordsException, TooManyRecordsException {
        return new RawScalarReader<>(targetType, checkEmptyResult);
    }

    public static <T> ResultSetReader<T> forFirstEntity(final OrmContext context, final SqlContext sqlContext, final EntityMapping em, final Class<T> resultClass) {
        return rs -> context.getEntityReader().readFirst(context, sqlContext, rs, em, resultClass);
    }

    public static <T> ResultSetReader<T> forSingleEntity(final OrmContext context, final SqlContext sqlContext, final EntityMapping em, final Class<T> resultClass) {
        return rs -> context.getEntityReader().readSingle(context, sqlContext, rs, em, resultClass);
    }

    public static <T> ResultSetReader<List<T>> forListEntity(final OrmContext context, final SqlContext sqlContext, final EntityMapping em, final Class<T> elementType) {
        return forListEntity(context, sqlContext, em, elementType, elementType);
    }

    public static <T> ResultSetReader<List<T>> forListEntity(final OrmContext context, final SqlContext sqlContext, final EntityMapping em, final Class<T> elementType, final Class<? extends T> resultClass) {
        return rs -> context.getEntityReader().readList(context, sqlContext, rs, em, elementType, resultClass);
    }

    public static <T> SqlResultSetReader<T> forFirstRow(final OrmContext context, final Class<T> resultClass, final SqlCommand command) {
        return (c, rs) -> context.getRowReader().readFirst(c, rs, resultClass, command);
    }

    public static <T> SqlResultSetReader<T> forSingleRow(final OrmContext context, final Class<T> resultClass, final SqlCommand command) {
        return (c, rs) -> context.getRowReader().readSingle(c, rs, resultClass, command);
    }

    public static <T> SqlResultSetReader<List<T>> forListRow(final OrmContext context, final Class<T> elementType,
                                                             final Class<? extends T> resultClass, final SqlCommand command) {
        return (c, rs) -> context.getRowReader().readList(c, rs, elementType, resultClass, command);
    }

    public static <T> SqlResultSetReader<List<T>> forNativeListRow(final OrmContext context, final Class<T> elementType,
                                                                   final Class<? extends T> resultClass, final SqlCommand command) {
        return (c, rs) -> context.getRowReader().readNativeList(c, rs, elementType, resultClass, command);
    }

    protected ResultSetReaders() {

    }
}
