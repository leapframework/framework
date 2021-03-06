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

package leap.core.ds.management;

import leap.lang.jdbc.*;

import java.sql.*;

public class MConnectionProxy extends ConnectionProxy implements MConnection {

    protected final MDataSourceProxy ds;
    protected final long             openTime;

    public MConnectionProxy(MDataSourceProxy ds, Connection conn) {
        super(conn, conn instanceof ConnectionProxy ? !((ConnectionProxy) conn).hasStackTraceOnOpen() : true);
        this.ds = ds;
        this.openTime = System.currentTimeMillis();
    }

    @Override
    public long getOpenTime() {
        return openTime;
    }

    @Override
    public final StackTraceElement[] getStackTraceOnOpen() {
        return hasStackTraceOnOpen() ? super.getStackTraceOnOpen() : ((ConnectionProxy)conn).getStackTraceOnOpen();
    }

    @Override
    public void close() throws SQLException {
        ds.closeConnection(this);
    }

    @Override
    protected void endExecuteStatement(StatementProxy stmt) {
        ds.onStatementEndExecute(this, stmt);
    }

}