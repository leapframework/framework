package leap.db.platform.dm;

import leap.db.platform.GenericDbDialect;

import java.sql.Types;

public class DM7Dialect extends GenericDbDialect {

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
    protected void registerColumnTypes() {
        columnTypes.add(Types.BOOLEAN,       "BIT");
        columnTypes.add(Types.BIT,           "BIT");

        columnTypes.add(Types.TINYINT,       "TINYINT");
        columnTypes.add(Types.SMALLINT,      "SMALLINT");
        columnTypes.add(Types.INTEGER,       "INTEGER");
        columnTypes.add(Types.BIGINT,        "BIGINT"  );

        //JDBC's real type mapping to java's float, JDBC's float type mapping to java's double
        columnTypes.add(Types.REAL,          "REAL");
        columnTypes.add(Types.FLOAT,         "FLOAT");
        columnTypes.add(Types.DOUBLE,        "DOUBLE");

        columnTypes.add(Types.DECIMAL,       "DECIMAL($p,$s)");
        columnTypes.add(Types.NUMERIC,       "NUMERIC($p,$s)");

        columnTypes.add(Types.CHAR,          "CHAR($l)",0,32767);
        columnTypes.add(Types.VARCHAR,       "VARCHAR($l)",0,8188);
        columnTypes.add(Types.VARCHAR,       "CLOB");
        columnTypes.add(Types.LONGVARCHAR,   "LONGVARCHAR");

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
