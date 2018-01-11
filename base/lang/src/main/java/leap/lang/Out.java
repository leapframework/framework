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
    private static final Out empty = new Out();

    public static <T> Out<T> empty(){
        return (Out<T>)empty;
    }

    protected T value;

	@Override
    public T get() {
        return value;
    }

    public T getAndReset() {
        T r = value;
        value = null;
        return r;
    }

    @Override
    public boolean isPresent() {
        return null != value;
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
	    return this;
	}

    @Override
    public String toString() {
        if(null == value){
            return "null";
        }else{
            return value.toString();
        }
    }
}