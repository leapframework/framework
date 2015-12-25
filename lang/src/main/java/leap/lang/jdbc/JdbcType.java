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
package leap.lang.jdbc;

public class JdbcType {

	private final int          code;
	private final String       name;
	private final JdbcTypeKind kind;
	private final Class<?>     defaultReadType; //the default java type for reading value from db
	private final Class<?>     defaultSaveType; //the default java type for saving value to db
	private final boolean	   supportsLength;
	private final boolean	   supportsPrecisionAndScale;
	
	public JdbcType(int code, String name,JdbcTypeKind kind,Class<?> defaultReadType,Class<?> defaultSaveType,
				    boolean supportsLength,boolean supportsPrecisionAndScale) {
	    this.code = code;
	    this.name = name;
	    this.kind = kind;
	    this.defaultReadType = defaultReadType;
	    this.defaultSaveType = defaultSaveType;
	    this.supportsLength  = supportsLength;
	    this.supportsPrecisionAndScale = supportsPrecisionAndScale;
    }
	
	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public JdbcTypeKind getKind() {
		return kind;
	}
	
	public Class<?> getDefaultReadType() {
		return defaultReadType;
	}

	public Class<?> getDefaultSaveType() {
		return defaultSaveType;
	}

	public boolean supportsLength() {
		return supportsLength;
	}

	public boolean supportsPrecisionAndScale() {
		return supportsPrecisionAndScale;
	}
}