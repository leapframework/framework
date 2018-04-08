/*
 * Copyright 2015 the original author or authors.
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
package leap.web.api.spec.swagger;

public enum SwaggerType {
	
	INTEGER("integer","int32"),
	LONG("integer","int64"),
	FLOAT("number","float"),
	DOUBLE("number","double"),
	STRING("string",null),
	BYTE("string","byte"),
	BINARY("string","binary"),
	BOOLEAN("boolean",null),
	DATE("string","date"),
	DATETIME("string","date-time"),
	TIME("string", "time"), // extended type
	PASSWORD("string","password"),
	FILE("file",null);
	
	private final String type;
	private final String format;
	
	private SwaggerType(String type, String format) {
		this.type   = type;
		this.format = format;
	}
	
	public String type() {
		return type;
	}
	
	public String fomrat() {
		return format;
	}

}
