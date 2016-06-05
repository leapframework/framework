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

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class SimpleMethodMonitor implements MethodMonitor {

    private static final Log log = LogFactory.get(SimpleMethodMonitor.class);

    private static final ThreadLocal<List<SimpleMethodMonitor>> local = new ThreadLocal<>();

    private static final int MAX_DEPTH = 10;

    private final SimpleMonitorProvider provider;
    private final String                className;
    private final String                methodDesc;
    private final Object[]              args;

    private List<SimpleMethodMonitor> stack;
    private boolean   root;
    private long      start;
    private long      duration;
    private Throwable error;

    public SimpleMethodMonitor(SimpleMonitorProvider provider, String className, String methodDesc, Object[] args) {
        this.provider   = provider;
        this.className  = className;
        this.methodDesc = methodDesc;
        this.args       = args;
        this.start();
    }

    protected void start() {
        if(log.isInfoEnabled()) {
            stack = local.get();
            if(stack == null) {
                this.root = true;
                stack = new ArrayList<>(5);
                local.set(stack);
            }
            stack.add(this);
            this.start = System.currentTimeMillis();
        }
    }

    @Override
    public void error(Throwable e) {
        //todo : log error?
        this.error = e;
    }

    @Override
    public void exit() {
        if(log.isInfoEnabled()) {
            duration = System.currentTimeMillis() - start;

            if(root) {

                if(duration >= provider.config.getMethodThreshold()) {
                    logExecutions();
                }

                stack.clear();
                local.set(null);
            }
        }
    }

    private void logExecutions() {
        /*
            class#method 100
              class#method 50
                class#method 30
         */
        StringBuilder s = new StringBuilder(100);

        int len = Math.min(stack.size(), MAX_DEPTH);

        final int threshold = provider.config.getMethodThreshold();
        for(int i=0;i<len;i++) {
            SimpleMethodMonitor monitor = stack.get(i);

            if(monitor.duration < threshold) {
                break;
            }

            if(i > 0) {
                s.append('\n');
                for(int j=0;j<i;j++) {
                    s.append("  ");
                }
            }
            s.append(monitor.className).append('#').append(monitor.methodDesc).append(' ').append(monitor.duration);
        }

        log.warn("Report slow methods :\n{}", s);
    }

}