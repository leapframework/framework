package leap.db.platform.dm;

import leap.db.platform.GenericDbDialect;

import java.sql.Types;

public class DM7Dialect extends GenericDbDialect {

    @Override
    protected String getTestDriverSupportsGetParameterTypeSQL() {
        return "select 1 from dual where 1 = ?";
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
        columnTypes.add(Types.BOOLEAN,       "bit");
        columnTypes.add(Types.BIT,           "bit");

        columnTypes.add(Types.TINYINT,       "tinyint");
        columnTypes.add(Types.SMALLINT,      "smallint");
        columnTypes.add(Types.INTEGER,       "INTEGER");
        columnTypes.add(Types.BIGINT,        "bigint"  );

        //JDBC's real type mapping to java's float, JDBC's float type mapping to java's double
        columnTypes.add(Types.REAL,          "real");
        columnTypes.add(Types.FLOAT,         "float");
        columnTypes.add(Types.DOUBLE,        "double");

        columnTypes.add(Types.DECIMAL,       "decimal($p,$s)");
        columnTypes.add(Types.NUMERIC,       "numeric($p,$s)");

        columnTypes.add(Types.CHAR,          "char($l)",0,32767);
        columnTypes.add(Types.VARCHAR,       "varchar($l)",0,8188);
        columnTypes.add(Types.VARCHAR,       "longtext");
        columnTypes.add(Types.LONGVARCHAR,   "longvarchar");

        columnTypes.add(Types.BINARY,        "binary($l)",1,32767);
        columnTypes.add(Types.BINARY,        "longblob");
        columnTypes.add(Types.VARBINARY,     "varbinary($l)",1,8188);
        columnTypes.add(Types.VARBINARY,     "longblob");
        columnTypes.add(Types.LONGVARBINARY, "longvarbinary"); // image

        columnTypes.add(Types.DATE,          "date");
        columnTypes.add(Types.TIME,          "time");
        columnTypes.add(Types.TIMESTAMP,     "TIMESTAMP");

        columnTypes.add(Types.BLOB,          "blob");
        columnTypes.add(Types.CLOB,          "clob");
    }
}
