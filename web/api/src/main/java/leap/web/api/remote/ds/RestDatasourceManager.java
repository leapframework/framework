package leap.web.api.remote.ds;

import java.util.Map;


import leap.lang.exception.ObjectExistsException;

public interface RestDatasourceManager {

	boolean hasDataSources();

	RestDataSource tryGetDataSource(String name);

	Map<String, RestDataSource> getAllDataSources();

    void registerDataSource(String name, RestDataSource ds) throws ObjectExistsException;

    boolean removeDataSource(String name);
}
