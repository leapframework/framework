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
package leap.lang;

import java.io.Serializable;

public class Error implements Serializable{
	
	private static final long serialVersionUID = -3060382631017010667L;
	
	public static final Error EMPTY = new Error("err");

	protected final String 	  code;
	protected final String 	  message;
	protected final Throwable cause;
	
	public Error(String message) {
		this(null,message,null);
    }
	
	public Error(String code, String message) {
		this(code,message,null);
    }
	
    public Error(String code, String message, Throwable cause) {
		Args.notEmpty(message,"error message");
	    this.code       = code;
	    this.message    = message;
	    this.cause      = cause;
	}
    
    public Error(Throwable cause) {
        Args.notNull(cause);
        this.code    = null;
        this.cause   = cause;
        this.message = cause.getMessage();
    }
    
    public Error(String message, Throwable cause) {
        Args.notEmpty(message,"error message");
        this.code       = null;
        this.message    = message;
        this.cause      = cause;
    }
    
	/**
	 * Returns the error code or <code>null</code>.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Returns the error message. 	
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the cause exception or <code>null</code>.
	 */
	public Throwable getCause() {
		return cause;
	}

	@Override
    public String toString() {
	    return "Error[code=" + code + ",message=" + message + "]";
    }
}