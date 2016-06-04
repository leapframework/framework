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

package tested;

import leap.lang.time.StopWatch;

public class MonitorMain {

    public static void main(String[] args) {
        for(int i=0;i<10;i++) {
            run(100000,true);
            runWithMonitor(100000,true);
        }

        int count = 100000;

        for(int i=0;i<10;i++) {
            System.out.println("===== Round " + (i+1) + " =====");
            run(count, false);
            runWithMonitor(count, false);
            System.out.println();
        }
    }

    private static void run(int count, boolean warmup) {
        StopWatch sw = StopWatch.startNew();
        for(int i=0;i<count;i++) {
            doSomething();
        }
        if(!warmup) {
            System.out.println("Run 1 : " + sw.getElapsedMilliseconds());
        }
    }

    private static void runWithMonitor(int count, boolean warmup) {
        StopWatch sw = StopWatch.startNew();
        for(int i=0;i<count;i++) {
            doSomethingWithMonitor();
        }
        if(!warmup) {
            System.out.println("Run 2 : " + sw.getElapsedMilliseconds());
        }
    }

    private static void doSomethingWithMonitor() {
        Monitor monitor = new Monitor("className","methodName", new Object[]{1,2,3,4});
        try{
            monitor.start();

            doSomething();

        }finally {
            monitor.stop();
        }
    }

    private static void doSomething() {

    }

    private static class Monitor {

        private final String  className;
        private final String   methodName;
        private final Object[] args;

        private long start;
        private long time;

        public Monitor(String className, String methodName, Object[] args) {
            this.className  = className;
            this.methodName = methodName;
            this.args       = args;
        }

        public void start() {
            start = System.currentTimeMillis();
        }

        public void error(Throwable e) {

        }

        public void stop() {
            time = System.currentTimeMillis() - start;
        }
    }

}
