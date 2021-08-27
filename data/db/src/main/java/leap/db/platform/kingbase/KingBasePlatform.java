package leap.db.platform.kingbase;

import leap.db.DbPlatforms;
import leap.db.platform.GenericDbDialect;
import leap.db.platform.GenericDbMetadataReader;
import leap.db.platform.GenericDbPlatform;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class KingBasePlatform extends GenericDbPlatform {

    public KingBasePlatform() {
        this(DbPlatforms.KINDBASE);
    }

    protected KingBasePlatform(String type) {
        super(type, productNameContainsIgnorecaseMatcher("KingBase"));
    }

    @Override
    protected GenericDbDialect createDialect(DatabaseMetaData jdbcMetadata) throws SQLException {
        return new KingBase8Dialect();
    }

    @Override
    protected GenericDbMetadataReader createMetadataReader(DatabaseMetaData jdbcMetadata) throws SQLException {
        return new KingBase8MetadataReader();
    }
}