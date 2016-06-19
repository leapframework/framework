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

package tested.aop;

import java.io.IOException;

public class TAopBean {

    private String lastHello;

    public String getLastHello() {
        return lastHello;
    }

    @TIntercepted
    public void hello() {
        lastHello = "Hello aop";
    }

    @TIntercepted
    public void hello(int i) {
        lastHello = "Hello aop";
    }

    @TIntercepted
    public String getHello() {
        return "hello aop";
    }

    @TIntercepted
    public String getHello(String name) {
        return "hello " + name;
    }

    @TException
    public String testException(String s) throws IOException {
        throw new IOException("ha ha ha");
    }

    /**
     * Total 50 method calls.
     */
    @TIntercepted
    public void perfRoot() {
        perfNest1();
        perfNest3();
    }

    @TIntercepted
    public void perfNest1() {
        for(int i=0;i<12;i++) {
            perfNest2();
        }
    }

    @TIntercepted
    public void perfNest2() {

    }

    @TIntercepted
    public void perfNest3() {
        for(int i=0;i<35;i++) {
            perfNest4();
        }
    }

    @TIntercepted
    public void perfNest4() {

    }
}