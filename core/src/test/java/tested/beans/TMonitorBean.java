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

package tested.beans;

import leap.lang.Threads;

public class TMonitorBean {

    public void test(int i, String s) {
        Threads.sleep(5);
        test1();
    }

    protected void test1() {
        Threads.sleep(10);
        throw new RuntimeException("err");
    }

    public void root(long sleep) {
        Threads.sleep(sleep);
        nest1(sleep * 2);
        nest1(sleep * 2);
        nest2(sleep * 3);
        for(int i=0;i<5;i++) {
            nest1(sleep);
        }
    }

    public void nest1(long sleep) {
        Threads.sleep(sleep);
        nest2(sleep);
    }

    public void nest2(long sleep) {
        Threads.sleep(sleep);
    }

    /**
     * Total 50 method calls.
     */
    public void perfRoot() {
        perfNest1();
        perfNest3();
    }

    public void perfNest1() {
        for(int i=0;i<12;i++) {
            perfNest2();
        }
    }

    public void perfNest2() {

    }

    public void perfNest3() {
        for(int i=0;i<35;i++) {
            perfNest4();
        }
    }

    public void perfNest4() {

    }
}