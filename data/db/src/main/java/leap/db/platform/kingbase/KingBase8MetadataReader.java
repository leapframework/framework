package leap.db.platform.kingbase;

import leap.db.platform.GenericDbMetadataReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KingBase8MetadataReader extends GenericDbMetadataReader {

    public KingBase8MetadataReader() {

    }

    @Override
    protected ResultSet getPrimaryKeys(Connection connection, DatabaseMetaData dm, MetadataParameters params) throws SQLException {
        String sql = "SELECT NULL AS TABLE_CAT, n.nspname AS TABLE_SCHEM, ct.relname AS TABLE_NAME, " +
                "a.attname AS COLUMN_NAME, (i.keys).n AS KEY_SEQ, ci.relname AS PK_NAME " +
                "FROM pg_catalog.pg_class ct " +
                "JOIN pg_catalog.pg_attribute a ON (ct.oid = a.attrelid) " +
                "JOIN pg_catalog.pg_namespace n ON (ct.relnamespace = n.oid) " +
                "JOIN (SELECT i.indexrelid, i.indrelid, i.indisprimary, information_schema._pg_expandarray(i.indkey) AS keys FROM pg_catalog.pg_index i) i ON (a.attnum = (i.keys).x AND a.attrelid = i.indrelid) " +
                "JOIN pg_catalog.pg_class ci ON (ci.oid = i.indexrelid) " +
                "WHERE true AND n.nspname = ? AND ct.relname LIKE '%' AND i.indisprimary  ORDER BY table_name, pk_name, key_seq";
        return executeSchemaQuery(connection, params, sql);
    }
}