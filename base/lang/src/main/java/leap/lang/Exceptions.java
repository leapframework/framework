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

import leap.lang.exception.NestedIOException;
import leap.lang.exception.NestedSQLException;
import leap.lang.exception.NestedUnsupportedEncodingException;
import leap.lang.io.IO;

import java.io.*;
import java.sql.SQLException;
import java.util.function.Supplier;

public class Exceptions {
	
	public static RuntimeException uncheck(Throwable e){
		if(e instanceof RuntimeException){
			return (RuntimeException)e;
		}
		
		return new RuntimeException(e);
	}
	
	public static RuntimeException uncheck(Throwable e, Supplier<RuntimeException> re) {
		if(e instanceof RuntimeException){
			return (RuntimeException)e;
		}
		return re.get();
	}
	
	public static void uncheckAndThrow(Throwable e){
		if(e instanceof RuntimeException){
			throw (RuntimeException)e;
		}
		
		throw new RuntimeException(e);
	}
	
	public static void uncheckAndThrow(Throwable e, Supplier<RuntimeException> re){
		if(e instanceof RuntimeException){
			throw (RuntimeException)e;
		}
		throw re.get();
	}
	
	public static NestedSQLException wrap(SQLException e){
		return new NestedSQLException(e);
	}
	
	public static void wrapAndThrow(SQLException e){
		throw new NestedSQLException(e);
	}
	
	public static NestedSQLException wrap(String message,SQLException e){
		return new NestedSQLException(message,e);
	}
	
	public static void wrapAndThrow(String message,SQLException e){
		throw new NestedSQLException(message,e);
	}
	
	public static NestedIOException wrap(IOException e){
		return new NestedIOException(e);
	}
	
	public static void wrapAndThrow(IOException e){
		throw new NestedIOException(e);
	}
	
	public static NestedIOException wrap(String message,IOException e){
		return new NestedIOException(message,e);
	}
	
	public static void wrapAndThrow(String message,IOException e){
		throw new NestedIOException(message,e);
	}
	
	public static NestedUnsupportedEncodingException wrap(UnsupportedEncodingException e){
		return new NestedUnsupportedEncodingException(e);
	}
	
	public static NestedUnsupportedEncodingException wrap(String message,UnsupportedEncodingException e){
		return new NestedUnsupportedEncodingException(message,e);
	}
	
	public static void wrapAndThrow(String message,UnsupportedEncodingException e){
		throw new NestedUnsupportedEncodingException(message,e);
	}
	
	public static UnsupportedOperationException notImplemented() {
		return new UnsupportedOperationException("Not implemented");
	}

    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter  pw = new PrintWriter(sw);
        try{
            e.printStackTrace(pw);
            return sw.toString();
        }finally{
            IO.close(pw);
            IO.close(sw);
        }
    }
	
	protected Exceptions(){
		
	}
}
