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

import leap.lang.Strings;

final class StdLog implements Log {
	
	private boolean trace = false;
	private boolean debug = false;
	private boolean info = false;
	private boolean warn = true;
	private boolean error = true;
	
	public boolean isTraceEnabled() {
		return trace;
	}
	
	public boolean isDebugEnabled() {
		return debug;
	}

	public boolean isInfoEnabled() {
		return info;
	}

	public boolean isWarnEnabled() {
		return warn;
	}
	
	public boolean isErrorEnabled() {
		return error;
	}

	public StdLog(LogLevel lv) {
		trace = lv.isTraceEnabled();
		debug = lv.isDebugEnabled();
		info = lv.isInfoEnabled();
		warn = lv.isWarnEnabled();
		error = lv.isErrorEnabled();
	}

	public void trace(String msg) {
		if(isTraceEnabled()){
			out(msg);
		}
	}

	public void trace(Throwable throwable) {
	    if(isTraceEnabled()){
	    	out(throwable);
	    }
    }
	
	public void trace(String msg, Throwable throwable) {
	    if(isTraceEnabled()){
	    	out(msg,throwable);
	    }
    }
	
	public void trace(String msg, Object... args) {
		if(isTraceEnabled()){
			out(msg,args);
		}
	}

	public void debug(String msg) {
		if(isDebugEnabled()){
			out(msg);
		}
	}
	
	public void debug(Throwable throwable) {
	    if(isDebugEnabled()){
	    	out(throwable);
	    }
    }

	public void debug(String msg, Throwable throwable) {
	    if(isDebugEnabled()){
	    	out(msg,throwable);
	    }
    }
	
	public void debug(String msg, Object... args) {
		if(isDebugEnabled()){
			out(msg,args);
		}
	}
	
	public void info(String msg) {
		if(isInfoEnabled()){
			out(msg);
		}
	}
	
	public void info(Throwable throwable) {
	    if(isInfoEnabled()){
	    	out(throwable);
	    }
    }
	
	public void info(String msg, Throwable throwable) {
	    if(isInfoEnabled()){
	    	out(msg,throwable);
	    }
    }

	public void info(String msg, Object... args) {
		if(isInfoEnabled()){
			out(msg,args);
		}
	}

	public void warn(String msg) {
		if(isWarnEnabled()){
			err(msg);
		}
	}

	public void warn(Throwable throwable) {
	    if(isWarnEnabled()){
	    	err(throwable);
	    }
    }
	
	public void warn(String msg, Throwable throwable) {
	    if(isWarnEnabled()){
	    	err(msg,throwable);
	    }
    }
	
	public void warn(String msg, Object... args) {
		if(isWarnEnabled()){
			err(msg,args);
		}
	}
	
	public void error(String msg) {
		if(isErrorEnabled()){
			err(msg);	
		}
	}

	public void error(Throwable throwable) {
		if(isErrorEnabled()){
			err(throwable);
		}
    }
	
	public void error(String msg, Throwable throwable) {
		if(isErrorEnabled()){
			err(msg,throwable);
		}
    }
	
	public void error(String msg, Object... args) {
		if(isErrorEnabled()){
			err(msg,args);
		}
	}
	
	private void out(String msg){
		System.out.println(msg);
	}
	
	private void out(Throwable throwable){
		if(null == throwable){
			logNullThrowable();
		}else{
			System.out.println(throwable.getMessage());
			throwable.printStackTrace(System.out);
		}
	}
	
	private void out(String msg,Throwable throwable){
    	System.out.println(msg);
    	if(null != throwable){
    		throwable.printStackTrace(System.out);
    	}
	}
	
	private void out(String msg,Object... args){
		System.out.println(format(msg, args));
		if(null != args && args.length > 0 && args[args.length -1] instanceof Throwable){
			((Throwable)args[args.length -1]).printStackTrace(System.out);
		}
	}
	
	private void err(String msg){
		System.err.println(msg);
	}
	
	private void err(Throwable throwable){
		if(null == throwable){
			logNullThrowable();
		}else{
			System.err.println(throwable.getMessage());
			throwable.printStackTrace(System.err);
		}
	}
	
	private void err(String msg,Throwable throwable){
    	System.err.println(msg);
    	if(null != throwable){
    		throwable.printStackTrace(System.err);
    	}
	}
	
	private void err(String msg,Object... args){
		System.err.println(format(msg, args));
		if(null != args && args.length > 0 && args[args.length -1] instanceof Throwable){
			((Throwable)args[args.length -1]).printStackTrace(System.err);
		}
	}
	
	private void logNullThrowable(){
		if(isInfoEnabled()){
			System.out.println("null input in logging");	
		}
	}
	
	private static String format(String template, Object... args) {
		if (Strings.isEmpty(template)) {
			return Strings.EMPTY;
		}
		
		char[] templateChars = template.toCharArray();

		int templateLength = templateChars.length;
		int length = 0;
		int tokenCount = args.length;
		for (int i = 0; i < tokenCount; i++) {
			Object sourceString = args[i];
			if (sourceString != null) {
				length += sourceString.toString().length();
			}
		}

		// The following buffer size is just an initial estimate. It is legal for
		// any given pattern, such as {}, to occur more than once, in which case
		// the buffer size will expand automatically if need be.
		StringBuilder buffer = new StringBuilder(length + templateLength);

		int lastStart  = 0;
		int tokenIndex = 0;
		for (int i = 0; i < templateLength; i++) {
			char ch = templateChars[i];
			if (ch == '{') {
				if (i + 1 < templateLength && templateChars[i + 1] == '}') {
					if (tokenIndex >= 0 && tokenIndex < tokenCount) {
						buffer.append(templateChars, lastStart, i - lastStart);
						
						Object sourceString = args[tokenIndex];
						
						if (sourceString != null){
							buffer.append(sourceString.toString());
						}

						i += 1;
						lastStart = i + 1;
						tokenIndex++;
					}
				}
			}
			// ELSE: Do nothing. The character will be added in later.
		}

		buffer.append(templateChars, lastStart, templateLength - lastStart);

		return new String(buffer);
	}	
}