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

public abstract class AbstractExecution implements Execution {

    protected ExecutionState state     = ExecutionState.PREPARED;
    protected Throwable      exception = null;

    @Override
    public ExecutionState getState() {
        return state;
    }

    public void setState(ExecutionState state) {
        this.state = state;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public void failure() {
        state = ExecutionState.FAILURE;
    }

    public void failure(Throwable e) {
        state = ExecutionState.FAILURE;
        exception = e;
    }

    public void success() {
        state = ExecutionState.SUCCESS;
    }
}
