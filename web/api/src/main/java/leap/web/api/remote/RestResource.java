package leap.web.api.remote;

import leap.core.value.Record;
import leap.core.value.SimpleRecord;
import leap.web.api.mvc.params.CountOptions;
import leap.web.api.mvc.params.DeleteOptions;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface RestResource {

    /**
     * Sets can create new access token if no token found.
     */
    void setCanNewAccessToken(boolean b);

    /**
     * Returns <code>true</code> if can new access token if no token found.
     *
     * <p/>
     * Default is <code>false</code>.
     */
    boolean isCanNewAccessToken();

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
    default Record find(Object id, QueryOptionsBase options) {
        Map<String, Object> map = find(Map.class, id, options);
        if(null == map) {
            return null;
        }else {
            return new SimpleRecord(map);
        }
    }

    /**
     * Finds the record by the given id.
     */
    <T> T find(Class<T> entityClass, Object id, QueryOptionsBase options);

    /**
     * Finds the record of many-to-one relation by the given id.
     */
    default Record findRelationOne(String relationPath, Object id, QueryOptionsBase options) {
        Map<String, Object> map = findRelationOne(Map.class, relationPath, id, options);
        if(null == map) {
            return null;
        }else {
            return new SimpleRecord(map);
        }
    }

    /**
     * Finds the record of many-to-one relation by the given id.
     */
    <T> T findRelationOne(Class<T> resultClass, String relationPath, Object id, QueryOptionsBase options);

    /**
     * Query the records of model.
     */
    default RestQueryListResult<Record> queryList(QueryOptions options) {
        RestQueryListResult<Map> result = queryList(Map.class, options, null);

        Object records =
                result.getList().stream().map(m -> new SimpleRecord(m)).collect(Collectors.toList());

        return new RestQueryListResult((List<Record>)records, result.getCount());
    }

    /**
     * Query the records of model.
     */
    default <T> RestQueryListResult<T> queryList(Class<T> resultElementClass, QueryOptions options) {
        return queryList(resultElementClass, options, null);
    }

    /**
     * Query list of resources.
     */
    <T> RestQueryListResult<T> queryList(Class<T> resultElementClass, QueryOptions options, Map<String, Object> filters);

    /**
     * Query the records of relation.
     */
    default RestQueryListResult<Record> queryRelationList(String relationPath, Object id, QueryOptions options){
        RestQueryListResult<Map> result = queryRelationList(Map.class, relationPath, id, options);

        Object records =
                result.getList().stream().map(m -> new SimpleRecord(m)).collect(Collectors.toList());

        return new RestQueryListResult((List<Record>)records, result.getCount());
    }

    /**
     * Query the records of relation.
     */
    <T> RestQueryListResult<T> queryRelationList(Class<T> resultElementClass, String relationPath, Object id, QueryOptions options);

    /**
     * Count the total records of resources.
     */
    int count(CountOptions options);
}