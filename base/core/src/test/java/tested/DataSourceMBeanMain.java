/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package tested;

import leap.core.AppMainBase;
import leap.core.annotation.Inject;
import leap.core.ds.DataSourceManager;
import leap.lang.Threads;

import javax.sql.DataSource;
import java.sql.Connection;

public class DataSourceMBeanMain extends AppMainBase {

    public static void main(String[] args) {
        main(DataSourceMBeanMain.class, args);
    }

    private @Inject DataSource ds;

    @Override
    protected void run(Object[] args) throws Throwable {
        for(;;) {
            try(Connection conn = ds.getConnection()) {
                Threads.sleep(1000);
            }
        }
    }

}