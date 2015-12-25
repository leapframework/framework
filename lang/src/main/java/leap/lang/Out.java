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

import java.util.function.Consumer;

public class Out<T> implements Valued<T>,Consumer<T>,Result<T> {

    protected T     value;
    protected Error error;
    
	@Override
    public T get() {
        return value;
    }

    @Override
    public Error error() {
        return error;
    }
    
    @Override
    public boolean isPresent() {
        return null != value;
    }
    
    @Override
    public boolean isError() {
        return null != error;
    }
    
    @Override
    public void accept(T t) {
        value = t;
    }
    
    public T getValue() {
        return value;
    }

	public Out<T> set(T value) {
		this.value = value;
		return this;
	}
	
	public Out<T> ok(T value) {
	    this.value = value;
	    this.error = null;
	    return this;
	}
	
	public Out<T> err() {
	    return err(true);
	}
	
	public Out<T> err(boolean error) {
	    if(error) {
	        this.error = Error.EMPTY;
	    }else{
	        this.error = null;
	    }
	    return this;
	}
	
    public Out<T> err(Error error) {
        this.error = error;
        return this;
    }
    
    public Out<T> err(String message) {
        this.error = new Error(message);
        return this;
    }
    
    public Out<T> err(String code, String message) {
        this.error = new Error(code, message);
        return this;
    }
    
    public Out<T> err(Throwable error) {
        this.error = new Error(error);
        return this;
    }

    @Override
    public String toString() {
        if(null != error) {
            return error.toString();
        }else if(null == value){
            return "null";
        }else{
            return value.toString();
        }
    }
}