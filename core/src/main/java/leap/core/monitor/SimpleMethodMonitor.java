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

    private static final String PKG = Classes.getPackageName(SimpleMethodMonitor.class);

    private static final Log SLOW_LOG = LogFactory.get("applog.monitor.slow");
    private static final Log ERR_LOG  = LogFactory.get("applog.monitor.error");

    private static final ThreadLocal<CallStack> local = new ThreadLocal<>();

    private static final int MAX_DEPTH = 10;

    private final MonitorConfig config;
    private final String        className;
    private final String        methodDesc;
    private final Object[]      args;

    private CallStack stack;
    private int       level;
    private long      start;
    private long      duration;

    private String callerClassName;
    private String callerMethodName;
    private int    callerLineNumber;

    public SimpleMethodMonitor(SimpleMonitorProvider provider, String className, String methodDesc, Object[] args) {
        this.config     = provider.config;
        this.className  = className;
        this.methodDesc = methodDesc;
        this.args       = args;
        this.start();
    }

    protected void resolveLineNumber() {
        if(config.isReportLineNumber()) {
            StackTraceElement[] stes = new Throwable().getStackTrace();
            for(int i=0;i<stes.length;i++) {
                StackTraceElement ste = stes[i];
                if(ste.getClassName().startsWith(PKG)) {
                    continue;
                }

                if(i < stes.length - 1) {
                    ste = stes[i+1];
                    callerClassName  = ste.getClassName();
                    callerMethodName = ste.getMethodName();
                    callerLineNumber = ste.getLineNumber();

                    int dotIndex = callerClassName.lastIndexOf('.');
                    if(dotIndex > 0) {
                        callerClassName = callerClassName.substring(dotIndex + 1);
                    }

                    break;
                }else{
                    break;
                }
            }
        }
    }

    protected void start() {
        if(SLOW_LOG.isInfoEnabled()) {
            stack = local.get();
            if(stack == null) {
                stack = new CallStack(config);
                local.set(stack);
            }
            stack.start(this);
            this.start = System.currentTimeMillis();
        }
    }

    protected boolean isRoot() {
        return level == 0;
    }

    @Override
    public void error(Throwable e) {
        if(isRoot() && config.isReportError() && ERR_LOG.isInfoEnabled()) {
            ERR_LOG.info("Error at : {}.{}", className, methodDesc, e);
        }
    }

    @Override
    public void exit() {
        if(SLOW_LOG.isInfoEnabled()) {
            duration = System.currentTimeMillis() - start;

            stack.exit(this);

            if(duration >= config.getMethodThreshold()) {
                resolveLineNumber();
            }

            if(isRoot()) {

                if(duration >= config.getMethodThreshold()) {
                    stack.logExecutions();
                }

                stack.release();
                local.set(null);
            }
        }
    }

    @Override
    public String toString() {
        return className + "." + methodDesc;
    }

    protected static final class CallStack {
        MonitorConfig             config;
        int                       level   = 0;
        List<SimpleMethodMonitor> methods = new ArrayList<>(5);

        CallStack(MonitorConfig config) {
            this.config = config;
        }

        int len() {
            return methods.size();
        }

        SimpleMethodMonitor get(int i) {
            return methods.get(i);
        }

        void start(SimpleMethodMonitor method) {
            method.level = level;
            methods.add(method);
            level++;
        }

        void exit(SimpleMethodMonitor method) {
            level--;
        }

        void release() {
            methods.clear();
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

            final int threshold = config.getMethodThreshold();
            for(int i=0;i<methods.size();i++) {
                SimpleMethodMonitor method = get(i);

                if(method.duration < threshold) {
                    continue;
                }

                int level = method.level;
                if(level > MAX_DEPTH) {
                    continue;
                }

                if(i > 0) {
                    s.append("\n  ");
                    for(int j=0;j<level;j++) {
                        s.append(".. ");
                    }
                }else{
                    s.append("  ");
                }

                s.append(method.className)
                        .append('.')
                        .append(method.methodDesc);

                if(null != method.callerClassName) {
                    s.append(" (at ")
                            .append(method.callerClassName)
                            .append(":")
                            .append(method.callerLineNumber)
                            .append(")");
                }

                s.append("  ").append(method.duration).append("ms");

                Object[] args = method.args;

                if(config.isReportArgs() && null != args && args.length > 0) {

                    s.append("\n");

                    for(int j=0;j<args.length;j++) {

                        if(j > 0) {
                            s.append("\n");
                        }

                        if(level > 0) {

                            s.append("   ");
                            for(int k=0;k<level;k++) {
                                s.append("   ");
                            }
                            s.append("  ");

                        }else{
                            s.append("     ");
                        }

                        s.append("p").append(j).append(" = ");

                        Object v = args[j];

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

            SLOW_LOG.info("Slow Execution ({}ms) : \n\n{}\n", get(0).duration, s);
        }
    }
}