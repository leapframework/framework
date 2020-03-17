package leap.db.platform.dm;

import leap.db.platform.GenericDbMetadataReader;

public class DM7MetadataReader extends GenericDbMetadataReader {

    protected DM7MetadataReader() {

    }

    @Override
    protected boolean supportsReadAllPrimaryKeys() {
        return false;
    }
}
