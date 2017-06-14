/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package leap.lang.intercepting;

public interface Execution {

    /**
     * The execution state for intercepting.
     */
    enum ExecutionState {
        PREPARED, //The init state.
        INTERCEPTED,
        SUCCESS,
        FAILURE
    }

    /**
     * Required. The execution status.
     */
    ExecutionState getState();

    /**
     * Optional.
     */
    Throwable getException();

    /**
     * Returns <code>true</code> if intercepted.
     */
    default boolean isIntercepted() {
        return ExecutionState.INTERCEPTED == getState();
    }

    /**
     * Returns <code>true</code> if failure.
     */
    default boolean isFailure() {
        return ExecutionState.FAILURE == getState();
    }

    /**
     * Returns <code>true</code> if success.
     */
    default boolean isSuccess() {
        return ExecutionState.SUCCESS == getState();
    }

    /**
     * Returns <code>true</code> if exception exists.
     */
    default boolean hasException() {
        return null != getException();
    }
}