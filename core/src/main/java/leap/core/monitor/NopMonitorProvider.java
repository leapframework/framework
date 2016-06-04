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

import leap.core.ioc.DummyBean;

public class NopMonitorProvider implements MonitorProvider,DummyBean {

    public static final MonitorProvider INSTANCE = new NopMonitorProvider();

    private final MethodMonitor mm = new NopMethodMonitor();

    private NopMonitorProvider() {

    }

    @Override
    public MethodMonitor startMethodMonitor(String className, String methodDesc) {
        return mm;
    }

    @Override
    public MethodMonitor startMethodMonitor(String className, String methodDesc, Object[] args) {
        return mm;
    }

    private static final class NopMethodMonitor implements MethodMonitor {
        @Override
        public void error(Throwable e) {
        }

        @Override
        public void exit() {
        }
    }
}
