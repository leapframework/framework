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
    
    Result EMPTY = new Result(){
        public Object get() {return null;}
    };

    Result INTERCEPTED = new Result(){
        public Object get() { return null;}
        public boolean isIntercepted() { return true;}
    };

    @SuppressWarnings("unchecked")
    static <T> Result<T> empty() {
        return (Result<T>)EMPTY;
    }

    @SuppressWarnings("unchecked")
    static <T> Result<T> intercepted() {
        return (Result<T>) INTERCEPTED;
    }

    static boolean empty(Result r) {
        return null != r && r.isEmpty();
    }

    static boolean present(Result r) {
        return null != r && r.isPresent();
    }

    static boolean intercepted(Result r) {
        return null != r && r.isIntercepted();
    }

    static <T> T value(Result<T> r) {
        return null == r ? null : r.get();
    }

    static <T> Result<T> of(final T value) {
        return () -> value;
    }

    /**
     * Returns the result value. May be <code>null</code>.
     */
    T get();

    /**
     * Returns <code>true</code> if the result is empty, that means no error, no value and not intercepted.
     */
    default boolean isEmpty() { return null == get() && !isIntercepted(); }
    
    /**
     * Returns <code>true</code> if the result value is not <code>null</code>.
     */
    default boolean isPresent() { return null != get(); }
    
    /**
     * Returns <code>true</code> if no result returned and the execution was intercepted.
     */
    default boolean isIntercepted() { return false; }
}