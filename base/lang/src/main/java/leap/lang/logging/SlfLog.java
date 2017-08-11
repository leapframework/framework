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

import leap.lang.Classes;

final class SlfLog implements Log {
	
	private final org.slf4j.Logger logger;
	
	public SlfLog(org.slf4j.Logger logger) {
		this.logger = logger;
    }
	
	public boolean isTraceEnabled() {
		return logger.isTraceEnabled() && LogContext.level().isTraceEnabled();
    }
	
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled() && LogContext.level().isDebugEnabled();
    }
	
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled() && LogContext.level().isInfoEnabled();
    }

	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
    }

	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
    }
	
	public void trace(String msg) {
		if(isTraceEnabled()){
			logger.trace(msg);	
		}
    }
	
	public void trace(Throwable throwable) {
		if(isTraceEnabled()){
			if(null == throwable){
				logNullThrowable();
			}else{
				logger.trace(throwable.getMessage(),throwable);
			}
		}
    }
	
	public void trace(String msg, Throwable throwable) {
		if(isTraceEnabled()){
			logger.trace(msg,throwable);
		}
    }
	
	public void trace(String msg, Object... args) {
		if(isTraceEnabled()){
			logger.trace(msg,args);	
		}
    }

	public void debug(String msg) {
		if(isDebugEnabled()){
			logger.debug(msg);	
		}
    }
	
	public void debug(Throwable throwable) {
		if(isDebugEnabled()){
			if(null == throwable){
				logNullThrowable();
			}else{
				logger.debug(throwable.getMessage(),throwable);
			}
		}
	}
	
	public void debug(String msg, Throwable throwable) {
		if(isDebugEnabled()){
			logger.debug(msg,throwable);	
		}
    }
	
	public void debug(String msg, Object... args) {
		if(isDebugEnabled()){
			logger.debug(msg,args);	
		}
    }
	
	public void info(String msg) {
		if(isInfoEnabled()){
			logger.info(msg);	
		}
    }

	public void info(Throwable throwable) {
		if(isInfoEnabled()){
			if(null == throwable){
				logNullThrowable();
			}else{
				logger.info(throwable.getMessage(),throwable);	
			}
		}
    }
	
	public void info(String msg, Throwable throwable) {
		if(isInfoEnabled()){
			logger.info(msg,throwable);	
		}
    }
	
	public void info(String msg, Object... args) {
		if(isInfoEnabled()){
			logger.info(msg,args);	
		}
    }
	
	public void warn(String msg) {
		if(isWarnEnabled()){
			logger.warn(msg);	
		}
    }
	
	public void warn(Throwable throwable) {
		if(isWarnEnabled()){
			if(null == throwable){
				logNullThrowable();
			}else{
				logger.warn(throwable.getMessage(),throwable);	
			}
		}
    }
	
	public void warn(String msg, Throwable throwable) {
		if(isWarnEnabled()){
			logger.warn(msg,throwable);	
		}
    }

	public void warn(String msg, Object... args) {
		if(isWarnEnabled()){
			logger.warn(msg,args);	
		}
    }
	
	public void error(String msg) {
		if(isErrorEnabled()){
			logger.error(msg);	
		}
    }
	
	public void error(Throwable throwable) {
		if(isErrorEnabled()){
			if(null == throwable){
				logNullThrowable();
			}else{
				logger.error(throwable.getMessage(),throwable);
			}
		}
    }
	
	public void error(String msg, Throwable throwable) {
		if(isErrorEnabled()){
			logger.error(msg,throwable);	
		}
    }
	
	public void error(String msg, Object... args) {
		if(isErrorEnabled()){
			logger.error(msg,args);	
		}
    }
	
	private void logNullThrowable(){
		if(isInfoEnabled()){
			logger.info("null input in logging");	
		}
	}

    static void init() {
        if(null != Classes.tryForName("ch.qos.logback.classic.LoggerContext")) {
            initLogback();
        }
    }

    private static void initLogback() {
        ch.qos.logback.classic.LoggerContext lc = (ch.qos.logback.classic.LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        lc.getFrameworkPackages().add(Classes.getPackageName(LogFactory.class));
    }
}