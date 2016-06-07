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

import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class SimpleMethodMonitor implements MethodMonitor {

    private static final Log SLOW_LOG = LogFactory.get("applog.monitor.slow");
    private static final Log ERR_LOG  = LogFactory.get("applog.monitor.error");

    private static final ThreadLocal<List<SimpleMethodMonitor>> local = new ThreadLocal<>();

    private static final int MAX_DEPTH = 10;

    private final MonitorConfig config;
    private final String        className;
    private final String        methodDesc;
    private final Object[]      args;

    private List<SimpleMethodMonitor> stack;
    private boolean   root;
    private long      start;
    private long      duration;

    public SimpleMethodMonitor(SimpleMonitorProvider provider, String className, String methodDesc, Object[] args) {
        this.config     = provider.config;
        this.className  = className;
        this.methodDesc = methodDesc;
        this.args       = args;
        this.start();
    }

    protected void start() {
        if(SLOW_LOG.isInfoEnabled()) {
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
        if(root && config.isReportError() && ERR_LOG.isInfoEnabled()) {
            ERR_LOG.info("Error at : {}.{}", className, methodDesc, e);
        }
    }

    @Override
    public void exit() {
        if(SLOW_LOG.isInfoEnabled()) {
            duration = System.currentTimeMillis() - start;

            if(root) {

                if(duration >= config.getMethodThreshold()) {
                    logExecutions();
                }

                stack.clear();
                local.set(null);
            }
        }
    }

    private void logExecutions() {
        /*
            className.methodName 100
                arg0 :
                arg1 :

              className.methodName 50
                className.methodName 30
         */
        StringBuilder s = new StringBuilder(100);

        int len = Math.min(stack.size(), MAX_DEPTH);

        final int threshold = config.getMethodThreshold();
        for(int i=0;i<len;i++) {
            SimpleMethodMonitor monitor = stack.get(i);

            if(monitor.duration < threshold) {
                break;
            }

            if(i > 0) {
                s.append("\n  ");
                for(int j=0;j<i;j++) {
                    s.append("..");
                }
            }else{
                s.append("  ");
            }

            s.append(monitor.className)
                    .append('.')
                    .append(monitor.methodDesc)
                    .append(' ')
                    .append(monitor.duration);

            if(config.isReportArgs() && null != args && args.length > 0) {

                s.append("\n");

                for(int j=0;j<args.length;j++) {
                    Object v = args[j];

                    if(j > 0) {
                        s.append("\n");
                    }

                    if(i > 0) {

                        s.append("  ");
                        for(int k=0;k<i;k++) {
                            s.append("  ");
                        }
                        s.append("  ");

                    }else{
                        s.append("    ");
                    }

                    s.append(j).append(" : ");

                    if(null == v) {
                        s.append("(null)");
                    }else{
                        String str;

                        Class<?> c = v.getClass();
                        if(Classes.isSimpleValueType(c)) {
                            str = v.toString();
                        }else {
                            str = "(obj:" +
                                    (Strings.isEmpty(c.getSimpleName()) ? c.getName() : c.getSimpleName()) +
                                    ")";
                        }

                        str = Strings.abbreviateMiddle(str, 50);
                        str = Strings.replace(str, "\n", "");

                        if(str.length() == 0) {
                            str = "(empty)";
                        }

                        s.append(str);
                    }
                }

                s.append("\n");
            }
        }

        SLOW_LOG.info("Slow Execution ({}ms) : \n\n{}\n", duration, s);
    }

}