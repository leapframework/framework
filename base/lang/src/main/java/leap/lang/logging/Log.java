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
}