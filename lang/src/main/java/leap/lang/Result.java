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
package leap.lang;

public interface Result<T> {
    
    @SuppressWarnings("rawtypes")
    Result EMPTY = new Result(){
        public Object get() {return null;}
        public boolean isPresent() {return false;}
    };
    
    @SuppressWarnings("unchecked")
    static <T> Result<T> empty() {
        return (Result<T>)EMPTY;
    } 
    
    static <T> Result<T> of(final T value) {
        return new Result<T>(){
            public T get() {return value;}
        };
    }
    
    static <T> Result<T> err(String message) {
        return new Result<T>() {
            public T get() {return null;}
            public Error error() {return new Error(message);}
        };
    }
    
    static <T> Result<T> err(Throwable e) {
        return new Result<T>() {
            public T get() {return null;}
            public Error error() {return new Error(e);}
        };
    }
    
    static <T> Result<T> err(String message, Throwable e) {
        return new Result<T>() {
            public T get() {return null;}
            public Error error() {return new Error(message, e);}
        };
    }
    
    /**
     * Returns the result value. May be <code>null</code>.
     */
    T get();

    /**
     * Returns the error object. May be <code>null</code>.
     */
    default Error error() { return null;}
    
    /**
     * Returns <code>true</code> if the result value is not <code>null</code>.
     */
    default boolean isPresent() { return null != get(); }
    
    /**
     * Returns <code>true</code> if the error object is not <code>null</code>.
     */
    default boolean isError() { return false;} 
    
    /**
     * Returns <code>true</code> if no error and the valie is present.
     */
    default boolean isValid() { return isPresent() && !isError(); }
    
}