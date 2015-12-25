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

import leap.core.exception.TooManyRecordsException;
import leap.core.value.Scalar;
import leap.core.value.SimpleScalar;

public class SimpleScalarReader implements ResultSetReader<Scalar> {
	
	public static SimpleScalarReader DEFAULT_INSTANCE = new SimpleScalarReader(1);
	
	private static final SimpleScalar NULL_SCALAR = new SimpleScalar(null);
	
	private final int column;

	public SimpleScalarReader(int column){
		this.column = column;
	}

	@Override
    public Scalar read(ResultSet rs) throws SQLException {
		if(rs.next()){
			
			Object value = rs.getObject(column);
			
			if(rs.next()){
				throw new TooManyRecordsException("Two or more rows returned for reading scalar value");
			}
			
			if(null == value) {
				return NULL_SCALAR;
			}else{
				return new SimpleScalar(value);	
			}
		}
		
        return null;
    }

}