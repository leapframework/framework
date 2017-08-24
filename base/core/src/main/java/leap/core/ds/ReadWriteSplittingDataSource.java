package leap.core.ds;

import leap.core.exception.DataAccessException;
import leap.core.jdbc.SqlExcutionContext;
import leap.core.jdbc.SqlExcutionType;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ReadWriteSplittingDataSource implements DataSource {

    private AtomicInteger counter = new AtomicInteger();

    private DataSource master;

    private DataSource defaultDataSource;

    private List<DataSource> slaves;

    @Override
    public Connection getConnection() throws SQLException {
        return determineTargetDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineTargetDataSource().getConnection(username, password);
    }

    protected DataSource determineTargetDataSource() {
        DataSource returnDataSource;

        if(SqlExcutionContext.getType().equals(SqlExcutionType.Write)){

            returnDataSource = master;

        } else if(SqlExcutionContext.getType().equals(SqlExcutionType.Read)){

            int index = determindIndex();
            returnDataSource = slaves.get(index);

        }else{

            if(null == defaultDataSource) {
                throw new DataAccessException("default data source must not be null while sql excution type is unknown");
            }

            returnDataSource = defaultDataSource;
        }

        return returnDataSource;
    }

    private int determindIndex() {

        if(slaves.size() < 2) return 0;

        int count = counter.incrementAndGet();

        if(count > 1000000){
            counter.set(0);
        }

        int index = count % slaves.size();

        return index;
    }

    public DataSource getMaster() {
        return master;
    }

    public void setMaster(DataSource master) {
        this.master = master;
    }

    public List<DataSource> getSlaves() {
        return slaves;
    }

    public void setSlaves(List<DataSource> slaves) {
        this.slaves = slaves;
    }

    public DataSource getDefaultDataSource() {
        return defaultDataSource;
    }

    public void setDefaultDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return determineTargetDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return determineTargetDataSource().isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return determineTargetDataSource().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        determineTargetDataSource().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        determineTargetDataSource().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return determineTargetDataSource().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return determineTargetDataSource().getParentLogger();
    }
}
