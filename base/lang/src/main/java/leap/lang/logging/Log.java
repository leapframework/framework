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

	public boolean isTraceEnabled();
	
	public boolean isDebugEnabled();
	
	public boolean isInfoEnabled();
	
	public boolean isWarnEnabled();
	
	public boolean isErrorEnabled();
	
	public void trace(String msg);
	
	public void trace(Throwable throwable);
	
	public void trace(String msg,Throwable throwable);
	
	public void trace(String msg,Object... args);
	
	public void debug(String msg);
	
	public void debug(Throwable throwable);
	
	public void debug(String msg,Throwable throwable);
	
	public void debug(String msg,Object... args);
	
	public void info(String msg);
	
	public void info(Throwable throwable);
	
	public void info(String msg,Throwable throwable);
	
	public void info(String msg,Object... args);
	
	public void warn(String msg);
	
	public void warn(Throwable throwable);
	
	public void warn(String msg,Throwable throwable);
	
	public void warn(String msg,Object... args);
	
	public void error(String msg);
	
	public void error(Throwable throwable);
	
	public void error(String msg,Throwable throwable);
	
	public void error(String msg,Object... args);
}