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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import leap.core.exception.TooManyRecordsException;
import leap.orm.OrmContext;
import leap.orm.mapping.EntityMapping;
import leap.orm.sql.SqlContext;


public interface EntityReader {
	
	<T> T readFirst(OrmContext context, SqlContext sqlContext, ResultSet rs, EntityMapping em, Class<T> resultClass) throws SQLException;

	<T> T readSingle(OrmContext context, SqlContext sqlContext, ResultSet rs, EntityMapping em, Class<T> resultClass) throws SQLException, TooManyRecordsException;
	
	<T> List<T> readList(OrmContext context, SqlContext sqlContext, ResultSet rs,EntityMapping em,Class<T> elementType, Class<? extends T> resultClass) throws SQLException;
	
}