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

import leap.lang.jdbc.DataSourceWrapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;

public class MDataSourceProxy extends DataSourceWrapper implements MDataSource {

    //todo : check the performance?
    protected CopyOnWriteArrayList<MConnection> activeConnections = new CopyOnWriteArrayList<>();

    public MDataSourceProxy(DataSource ds) {
        super(ds);
    }

    @Override
    public MConnection[] getActiveConnections() {
        return activeConnections.toArray(new MConnection[0]);
    }

    public DataSource wrapped() {
        return ds;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return openConnection(super.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return openConnection(super.getConnection(username, password));
    }

    public void destroy() {
        activeConnections.clear();
    }

    protected Connection openConnection(Connection connection) {
        if(null == connection) {
            return null;
        }

        MConnectionProxy proxy = new MConnectionProxy(this, connection);

        activeConnections.add(proxy);

        return proxy;
    }

    protected void closeConnection(MConnectionProxy proxy) throws SQLException {
        Connection conn = proxy.wrapped();
        try{
            activeConnections.remove(proxy);
        }finally{
            conn.close();
        }
    }

}