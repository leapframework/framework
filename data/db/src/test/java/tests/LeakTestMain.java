/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests;

import leap.core.AppMainBase;
import leap.db.cp.PooledDataSource;
import tests.cp.mock.MockDataSource;

public class LeakTestMain extends AppMainBase {

    private static MockDataSource   ms;
    private static PooledDataSource ds;

    static  {
        ms = new MockDataSource();
        ds = new PooledDataSource(ms);
    }

    static void init() throws Exception {
        ds.setMaxActive(10);
        ds.setMinIdle(5);
        ds.setMaxWait(10000);
        ds.setIdleTimeout(2);

        ds.open();
        ms.setOpenConnectionError(true);
    }

    public static void main(String[] args) throws Exception {
        init();

        for(;;) {
            String s = "";
        }
    }

}
