/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.core.monitor;

public class SimpleMethodMonitor implements MethodMonitor {

    private final String   className;
    private final String   methodDesc;
    private final Object[] args;
    private final long     start;

    private long      duration;
    private Throwable error;

    public SimpleMethodMonitor(String className, String methodDesc, Object[] args) {
        this.className  = className;
        this.methodDesc = methodDesc;
        this.args       = args;
        this.start      = System.currentTimeMillis();
    }

    @Override
    public void error(Throwable e) {
        this.error = e;
    }

    @Override
    public void exit() {
        duration = System.currentTimeMillis() - start;
        //todo :
    }

}