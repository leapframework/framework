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

import java.sql.ResultSet;
import java.sql.SQLException;

import leap.core.exception.EmptyRecordsException;
import leap.core.exception.TooManyRecordsException;
import leap.lang.convert.Converts;

public class RawScalarReader<T> implements ResultSetReader<T> {

	private final Class<T> type;
	private final boolean  checkEmptyResult;
	
	public RawScalarReader(Class<T> type){
		this(type,false);
	}
	
	public RawScalarReader(Class<T> type, boolean checkEmptyResult){
		this.type             = type;
		this.checkEmptyResult = checkEmptyResult;
	}

	@Override
    public T read(ResultSet rs) throws SQLException {
		if(rs.next()){
			
			T value = Converts.convert(rs.getObject(1), type);
			
			if(rs.next()){
				throw new TooManyRecordsException("Two or more rows returned for reading scalar value");
			}
			
			return value;
		}
		
		if(checkEmptyResult){
			throw new EmptyRecordsException("No data returned for reading scalar value");
		}
		
        return null;
    }
}
