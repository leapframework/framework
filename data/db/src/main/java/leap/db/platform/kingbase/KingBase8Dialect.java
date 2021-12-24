package leap.db.platform.kingbase;

import leap.db.DbLimitQuery;
import leap.db.platform.GenericDbDialect;
import leap.lang.value.Limit;
import java.sql.*;

public class KingBase8Dialect extends GenericDbDialect {

    @Override
    protected String getTestDriverSupportsGetParameterTypeSQL() {
        return "select 1";
    }

    @Override
    protected String getOpenQuoteString() {
        return "\"";
    }

    @Override
    protected String getCloseQuoteString() {
        return "\"";
    }

    @Override
    public boolean supportsColumnComment() {
        return false;
    }

    @Override
    public String getLimitQuerySql(DbLimitQuery query) {
        Limit limit = query.getLimit();

        int offset = limit.getStart() - 1;
        int rows   = limit.getEnd()   - offset;

        String sql = query.getSql(db) + " limit ?,?";
        query.getArgs().add(offset);
        query.getArgs().add(rows);

        return sql;
    }

    @Override
    public String getDefaultSchemaName(Connection connection, DatabaseMetaData dm) throws SQLException {
        return connection.getSchema();
    }

    @Override
    protected void registerColumnTypes() {
        columnTypes.add(Types.BOOLEAN,       "BOOLEAN");
        columnTypes.add(Types.BIT,           "BIT");

        columnTypes.add(Types.TINYINT,       "TINYINT");
        columnTypes.add(Types.SMALLINT,      "SMALLINT");
        columnTypes.add(Types.INTEGER,       "INTEGER");
        columnTypes.add(Types.BIGINT,        "BIGINT");

        //JDBC's real type mapping to java's float, JDBC's float type mapping to java's double
        columnTypes.add(Types.REAL,          "REAL");
        columnTypes.add(Types.FLOAT,         "FLOAT");
        columnTypes.add(Types.DOUBLE,        "DOUBLE");

        columnTypes.add(Types.DECIMAL,       "DECIMAL($p,$s)");
        columnTypes.add(Types.NUMERIC,       "NUMERIC($p,$s)");

        columnTypes.add(Types.CHAR,          "CHAR($l)",0,32767);
        columnTypes.add(Types.VARCHAR,       "VARCHAR($l)",0,8188);
        columnTypes.add(Types.VARCHAR,       "CLOB");
        columnTypes.add(Types.LONGVARCHAR,   "CLOB");

        columnTypes.add(Types.BINARY,        "BINARY($l)",1,32767);
        columnTypes.add(Types.BINARY,        "BLOB");
        columnTypes.add(Types.VARBINARY,     "VARBINARY($l)",1,8188);
        columnTypes.add(Types.VARBINARY,     "BLOB");
        columnTypes.add(Types.LONGVARBINARY, "BLOB");

        columnTypes.add(Types.DATE,          "DATE");
        columnTypes.add(Types.TIME,          "TIME");
        columnTypes.add(Types.TIMESTAMP,     "TIMESTAMP");

        columnTypes.add(Types.BLOB,          "BLOB");
        columnTypes.add(Types.CLOB,          "CLOB");
    }

}