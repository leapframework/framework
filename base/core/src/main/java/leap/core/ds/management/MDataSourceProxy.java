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

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.lang.Dates;
import leap.lang.jdbc.DataSourceWrapper;
import leap.lang.jdbc.StatementProxy;
import leap.lang.jmx.Managed;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.logging.StackTraceStringBuilder;
import leap.lang.time.DateFormats;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MDataSourceProxy extends DataSourceWrapper implements MDataSource {

    private static final Log log = LogFactory.get(MDataSourceProxy.class);

    protected String name;

    protected @Inject @M MDataSourceConfig config;

    protected final DataSourceMBean   mbean             = new DataSourceMBean();
    
    protected final List<MConnection> activeConnections = new CopyOnWriteArrayList<>(); //todo : check the performance?

    private final    SlowSql[] slowSqls     = new SlowSql[50];
    private volatile int       slowSqlIndex = 0;
    protected final  Object    slowSqlsLock    = new Object();

    private final    SlowSql[] verySlowSqls     = new SlowSql[50];
    private volatile int       verySlowSqlIndex = 0;
    protected final   Object   verySlowSqlsLock   = new Object();
    
    public MDataSourceProxy(DataSource ds) {
        super(ds);
    }

    public MDataSourceProxy(DataSource ds, MDataSourceConfig config) {
        super(ds);
        this.config = config;
    }

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
    public MSlowSql[] getSlowSqls() {
        List<MSlowSql> list = new ArrayList<>();

        for(SlowSql ss : slowSqls) {
            if(null != ss) {
                list.add(ss);
            }
        }

        return list.toArray(new MSlowSql[list.size()]);
    }

    @Override
    public MSlowSql[] getVerySlowSqls() {
        List<MSlowSql> list = new ArrayList<>();

        for(SlowSql ss : verySlowSqls) {
            if(null != ss) {
                list.add(ss);
            }
        }

        return list.toArray(new MSlowSql[list.size()]);
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

    protected void onStatementEndExecute(MConnectionProxy conn, StatementProxy stmt) {
        if(!conn.printThreadDumpEnable()){
            return;
        }
        SlowSql ss;

        if(config.getVerySlowSqlThreshold() > 0 && stmt.getLastExecutingDurationMs() >= config.getVerySlowSqlThreshold()) {

            ss = new SlowSql(stmt.getLastExecutingSql(), stmt.getLastExecutingDurationMs(), conn.getStackTraceOnOpen());

            try {
                verySlowSqls[verySlowSqlIndex++] = ss;
            } catch (IndexOutOfBoundsException e) {
                synchronized (verySlowSqlsLock){
                    verySlowSqlIndex = 0;
                    verySlowSqls[verySlowSqlIndex++] = ss;
                }
            }

            if(config.isLogVerySlowSql()) {
                log.warn("Found very slow sql ->\n time  : {}ms\n sql   : {}\n trace : [ \n{}]",
                        ss.getDurationMs(), ss.getSql(), new StackTraceStringBuilder(ss.getStackTraceElements()).toString());

            }
        }else if(config.getSlowSqlThreshold() > 0 && stmt.getLastExecutingDurationMs() >= config.getSlowSqlThreshold()) {
            
            ss = new SlowSql(stmt.getLastExecutingSql(), stmt.getLastExecutingDurationMs(), conn.getStackTraceOnOpen());

            try {
                slowSqls[slowSqlIndex++] = ss;
            } catch (IndexOutOfBoundsException e) {
                synchronized (verySlowSqlsLock){
                    slowSqlIndex = 0;
                    slowSqls[slowSqlIndex++] = ss;
                }
            }

            if(config.isLogSlowSql()) {
                log.warn("Found slow sql ->\n time  : {}ms\n sql   : {}\n trace : [ \n{}]",
                        ss.getDurationMs(), ss.getSql(), new StackTraceStringBuilder(ss.getStackTraceElements()).toString());
            }
        }
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
        public ActiveConnectionsModel getActiveConnections() {

            MConnection[] activeConnections = MDataSourceProxy.this.getActiveConnections();

            ConnectionModel[] activeConnectionModels = new ConnectionModel[activeConnections.length];

            for(int i=0;i<activeConnectionModels.length;i++) {
                activeConnectionModels[i] = new ConnectionModel(activeConnections[i]);
            }

            return new ActiveConnectionsModel(activeConnectionModels);
        }

    }

    protected static class ActiveConnectionsModel {

        protected final int               size;
        protected final ConnectionModel[] connections;

        public ActiveConnectionsModel(ConnectionModel[] connections) {
            this.connections = connections;
            this.size        = connections.length;
        }

        public int getSize() {
            return size;
        }

        public ConnectionModel[] getConnections() {
            return connections;
        }
    }

    protected static class ConnectionModel {

        private final MConnection connection;

        public ConnectionModel(MConnection connection) {
            this.connection = connection;
        }

        public String getOpenTime() {
            return Dates.format(new Date(connection.getOpenTime()), DateFormats.TIMESTAMP_PATTERN);
        }

        public String getStackTrace() {
            return new StackTraceStringBuilder(connection.getStackTraceOnOpen()).toString();
        }
    }

    protected static class SlowSql implements MSlowSql {

        private final String              sql;
        private final long                durationMs;
        private final StackTraceElement[] stackTraceElements;

        public SlowSql(String sql, long duration, StackTraceElement[] stackTraceElements) {
            this.sql = sql;
            this.durationMs = duration;
            this.stackTraceElements = stackTraceElements;
        }

        public String getSql() {
            return sql;
        }

        @Override
        public long getDurationMs() {
            return durationMs;
        }

        public StackTraceElement[] getStackTraceElements() {
            return stackTraceElements;
        }
    }

}