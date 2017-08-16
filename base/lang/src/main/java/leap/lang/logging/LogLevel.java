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
package leap.lang.logging;

import leap.lang.Valued;

public enum LogLevel implements Valued<Integer>{

    ALL(-999),
	TRACE(-4),
	DEBUG(-3),
	INFO(-2),
	WARN(-1),
	ERROR(0);

	private final int value;
	
	LogLevel(int value) {
		this.value = value;
    }

	public Integer getValue() {
	    return value;
    }
	
	public boolean isTraceEnabled(){
		return value <= TRACE.value;
	}
	
	public boolean isDebugEnabled(){
		return value <= DEBUG.value;
	}
	
	public boolean isInfoEnabled(){
		return value <= INFO.value;
	}
	
	public boolean isWarnEnabled(){
		return value <= WARN.value;
	}
	
	public boolean isErrorEnabled(){
		return value <= ERROR.value;
	}
	
	public static LogLevel byName(String name){
		if("trace".equalsIgnoreCase(name)){
			return TRACE;
		}
		
		if("debug".equalsIgnoreCase(name)){
			return DEBUG;
		}
		
		if("info".equalsIgnoreCase(name)){
			return INFO;
		}
		
		if("warn".equalsIgnoreCase(name)){
			return WARN;
		}
		
		if("error".equalsIgnoreCase(name)){
			return ERROR;
		}
		
		throw new IllegalArgumentException("invalid log level name '" + name + "'");
	}
}