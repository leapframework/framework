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
package leap.orm.annotation;

import leap.lang.jdbc.JdbcTypes;

/**
 * Defines common used jdbc type
 * 
 * @see JdbcTypes
 */
public enum ColumnType {
	AUTO(""),
	BOOLEAN,
	TINYINT,
	SMALLINT,
	INTEGER,
    BIGINT,
	DECIMAL,
	DATE,
	TIME,
	TIMESTAMP,
	VARCHAR,
	CHAR,
	TEXT("longvarchar"),
	CLOB,
	BINARY,
	VARBINARY,
    BLOB;
	
	private final String typeName;
	
	private ColumnType(){
		this.typeName = this.name().toLowerCase();
	}
	
	private ColumnType(String name){
		this.typeName = name;
	}
	
	public String getTypeName(){
		return typeName;
	}
}