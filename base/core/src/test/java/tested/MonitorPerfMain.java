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

import leap.core.AppMainBase;
import leap.core.annotation.Inject;
import leap.core.annotation.Monitored;
import leap.core.monitor.DefaultMonitorConfig;
import leap.core.monitor.MonitorConfig;
import leap.lang.time.StopWatch;
import tested.beans.TMonitorBean;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Monitored(false)
public class MonitorPerfMain extends AppMainBase {

    public static void main(String[] args) {
        AppMainBase.main(MonitorPerfMain.class, args);
    }

    private @Inject TMonitorBean  bean;
    private @Inject MonitorConfig config;

    @Override
    protected void run(Object[] args) throws Throwable {
        DefaultMonitorConfig conf = (DefaultMonitorConfig)config;

        int count = 100000;

        for(int i=0;i<10;i++) {
            conf.setEnabled(false);
            runTest("",count, true);
        }

        for(int i=0;i<10;i++) {
            conf.setEnabled(true);
            runTest("", count, true);
        }

        System.out.println("\n\n\n\n\n");
        System.out.println("Warm up done, Let's begin!\n");
        System.out.println("Total " + count + " requests / round, 50 monitored methods / request");
        System.out.println();

        for(int i=0;i<10;i++) {
            conf.setEnabled(false);
            runTest("Disable         " ,count, false);

            conf.setEnabled(true);
            runTest("Enable (All)    ", count, false);

            conf.setReportLineNumber(false);
            runTest("Enable (No Line)",count, false);

            conf.setReportArgs(false);
            runTest("Enable (No Args)",count, false);

            System.out.println();
        }

        System.out.println("\n\n\n");
    }

    protected void runTest(String name, int count, boolean warmup) {
        StopWatch sw = StopWatch.startNew();

        for(int i=0;i<count;i++) {
            bean.perfRoot();
        }

        if(!warmup) {

            long ns = sw.getElapsedNanoseconds();
            long ms = sw.getElapsedMilliseconds();

            String log = name + " : " + ms + "ms";
            if(config.isEnabled()) {
                double factor = 1.0 / TimeUnit.MILLISECONDS.toNanos(1);

                long avgns = (ns/count);

                double avgms = new BigDecimal(avgns * factor)
                        .setScale(4, BigDecimal.ROUND_HALF_UP)
                        .doubleValue();
                log += ", " + avgms + "ms/request";
            }

            System.out.println(log);
        }
    }

}
