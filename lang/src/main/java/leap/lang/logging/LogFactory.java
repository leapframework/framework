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

public final class LogFactory {
	
	private static final boolean slflog;
	private static final StdLog  stdlog = new StdLog();
	
	static {
		slflog = forName("org.slf4j.impl.StaticLoggerBinder");
	}

	public static Log get(String name) {
		return slflog ? getSlf4jLogger(name) : stdlog;
	}
	
	public static Log get(Class<?> clazz) {
		return slflog ? getSlf4jLogger(clazz) : stdlog;
	}
	
	private static Log getSlf4jLogger(String name){
		return new SlfLog(org.slf4j.LoggerFactory.getLogger(name));
	}
	
	private static Log getSlf4jLogger(Class<?> clazz){
		return new SlfLog(org.slf4j.LoggerFactory.getLogger(clazz));
	}
	
	private static boolean forName(String className) {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable ex) {
		}
		if (cl == null) {
			cl = LogFactory.class.getClassLoader();
		}
		try {
	        return null != cl.loadClass(className);
        } catch (Throwable e) {
        	return false;
        }
	}
}