package leap.db.platform.dm;

import leap.db.DbPlatforms;
import leap.db.platform.GenericDbDialect;
import leap.db.platform.GenericDbMetadataReader;
import leap.db.platform.GenericDbPlatform;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class DMPlatform extends GenericDbPlatform {

    public DMPlatform() {
        this(DbPlatforms.DAMENG);
    }

    public DMPlatform(String type){
        super(type,productNameContainsIgnorecaseMatcher("DM DBMS"));
    }

    @Override
    protected GenericDbDialect createDialect(DatabaseMetaData jdbcMetadata) throws SQLException {
        return new DM7Dialect();
    }

    @Override
    protected GenericDbMetadataReader createMetadataReader(DatabaseMetaData jdbcMetadata) throws SQLException {
        return new DM7MetadataReader();
    }
}
