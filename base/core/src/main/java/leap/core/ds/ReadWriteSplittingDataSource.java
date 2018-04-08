package leap.core.ds;

import leap.core.exception.DataAccessException;
import leap.core.jdbc.SqlExecutionContext;
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

    private DataSource writeDataSource;

    private DataSource defaultDataSource;

    private List<DataSource> readDataSources;

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

        if(SqlExecutionContext.getType().equals(SqlExcutionType.Write)){

            returnDataSource = writeDataSource;

        } else if(SqlExecutionContext.getType().equals(SqlExcutionType.Read)){

            int index = determindIndex();
            returnDataSource = readDataSources.get(index);

        }else{

            if(null == defaultDataSource) {
                throw new DataAccessException("default data source must not be null while sql execution type is unknown");
            }

            returnDataSource = defaultDataSource;
        }

        return returnDataSource;
    }

    private int determindIndex() {

        if(readDataSources.size() < 2) return 0;

        int count = counter.incrementAndGet();

        if(count > 1000000){
            counter.set(0);
        }

        int index = count % readDataSources.size();

        return index;
    }

    public DataSource getWriteDataSource() {
        return writeDataSource;
    }

    public void setWriteDataSource(DataSource writeDataSource) {
        this.writeDataSource = writeDataSource;
    }

    public List<DataSource> getReadDataSources() {
        return readDataSources;
    }

    public void setReadDataSources(List<DataSource> readDataSources) {
        this.readDataSources = readDataSources;
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
