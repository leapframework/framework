package leap.web.api.remote;

import leap.core.value.Record;
import leap.web.api.mvc.params.CountOptions;
import leap.web.api.mvc.params.DeleteOptions;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;

import java.util.Map;

public interface RestResource {

    /**
     * Creates a record for remote resource.
     */
    <T> T insert(Class<T> resultClass, Object obj);

    /**
     * Creates a new record.
     */
    Record create(Map<String, Object> properties);

    /**
     * Updates a record.
     */
    boolean update(Object id, Object partial);

    /**
     * Deletes a record.
     */
    boolean delete(Object id, DeleteOptions options);

    /**
     * Query one by id.
     */
    Record find(Object id, QueryOptionsBase options);

    /**
     * Finds the record by the given id.
     */
    <T> T find(Class<T> entityClass, Object id, QueryOptionsBase options);

    /**
     * Query the records of model.
     */
    default <T> RestQueryListResult<T> queryList(Class<T> entityClass, QueryOptions options) {
        return queryList(entityClass, options, null);
    }

    /**
     * Query list of resources.
     */
    <T> RestQueryListResult<T> queryList(Class<T> resultElementClass, QueryOptions options, Map<String, Object> filters);

    /**
     * Count the total records of resources.
     */
    int count(CountOptions options);
}