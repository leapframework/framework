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
import leap.lang.jmx.Managed;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;

public class MDataSourceProxy extends DataSourceWrapper implements MDataSource {

    protected String name;
    protected DataSourceMBean mbean = new DataSourceMBean();
    protected CopyOnWriteArrayList<MConnection> activeConnections = new CopyOnWriteArrayList<>(); //todo : check the performance?

    public MDataSourceProxy(DataSource ds) {
        super(ds);
    }

    @Managed
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataSource wrapped() {
        return ds;
    }

    @Override
    public Object getMBean() {
        return mbean;
    }

    @Override
    public MConnection[] getActiveConnections() {
        return activeConnections.toArray(new MConnection[0]);
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

    protected class DataSourceMBean {

        @Managed
        public ConnectionMBeanModel[] getActiveConnections() {

            MConnection[] activeConnections = MDataSourceProxy.this.getActiveConnections();

            ConnectionMBeanModel[] activeConnectionModels = new ConnectionMBeanModel[activeConnections.length];

            for(int i=0;i<activeConnectionModels.length;i++) {
                activeConnectionModels[i] = new ConnectionMBeanModel(activeConnections[i]);
            }

            return activeConnectionModels;
        }

    }

    protected static class ConnectionMBeanModel {

        private final MConnection connection;

        public ConnectionMBeanModel(MConnection connection) {
            this.connection = connection;
        }

        public long getOpenTime() {
            return connection.getOpenTime();
        }

    }

}