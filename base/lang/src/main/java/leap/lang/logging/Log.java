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

public interface Log {

	boolean isTraceEnabled();
	
	boolean isDebugEnabled();
	
	boolean isInfoEnabled();
	
	boolean isWarnEnabled();
	
	boolean isErrorEnabled();
	
	void trace(String msg);
	
	void trace(Throwable throwable);
	
	void trace(String msg,Throwable throwable);
	
	void trace(String msg,Object... args);
	
	void debug(String msg);
	
	void debug(Throwable throwable);
	
	void debug(String msg,Throwable throwable);
	
	void debug(String msg,Object... args);
	
	void info(String msg);
	
	void info(Throwable throwable);
	
	void info(String msg,Throwable throwable);
	
	void info(String msg,Object... args);
	
	void warn(String msg);
	
	void warn(Throwable throwable);
	
	void warn(String msg,Throwable throwable);
	
	void warn(String msg,Object... args);
	
	void error(String msg);
	
	void error(Throwable throwable);
	
	void error(String msg,Throwable throwable);
	
	void error(String msg,Object... args);

	default void log(LogLevel level, String msg){
		switch (level){
			case DEBUG:
				debug(msg);
				break;
			case INFO:
				info(msg);
				break;
			case WARN:
				warn(msg);
				break;
			case ERROR:
				error(msg);
				break;
			case TRACE:
			default:
				trace(msg);
				break;
		}
	}

	default void log(LogLevel level, Throwable throwable){
		switch (level){
			case DEBUG:
				debug(throwable);
				break;
			case INFO:
				info(throwable);
				break;
			case WARN:
				warn(throwable);
				break;
			case ERROR:
				error(throwable);
				break;
			case TRACE:
			default:
				trace(throwable);
				break;
		}
	};

	default void log(LogLevel level, String msg,Throwable throwable){
		switch (level){
			case DEBUG:
				debug(msg, throwable);
				break;
			case INFO:
				info(msg, throwable);
				break;
			case WARN:
				warn(msg, throwable);
				break;
			case ERROR:
				error(msg, throwable);
				break;
			case TRACE:
			default:
				trace(msg, throwable);
				break;
		}
	}

	default void log(LogLevel level, String msg,Object... args){
		switch (level){
			case DEBUG:
				debug(msg, args);
				break;
			case INFO:
				info(msg, args);
				break;
			case WARN:
				warn(msg, args);
				break;
			case ERROR:
				error(msg, args);
				break;
			case TRACE:
			default:
				trace(msg, args);
				break;
		}
	}
}