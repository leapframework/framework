package leap.web.api.remote;

import java.util.Map;

import leap.lang.http.client.HttpResponse;
import leap.web.api.mvc.params.CountOptions;
import leap.web.api.mvc.params.DeleteOptions;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;

public interface RestResource {

	RestOrmContext getOrmContext();

	void setEndpoint(String endpoint);

	String getEndpoint();

	<T> T insert(Class<T> entityClass, Object obj);

	boolean delete(Object id, DeleteOptions options);

	void update(Object id, Object partial);

    /**
     * Finds the record by the given id.
     */
    <T> T find(Class<T> entityClass,Object id, QueryOptionsBase options);

    <T> RestQueryListResult<T> queryList(Class<T> entityClass,QueryOptions options, Map<String, Object> filters);

    int count(CountOptions options);

    /**
     * Query the records of model.
     */
     default <T> RestQueryListResult<T> queryList(Class<T> entityClass,QueryOptions options) {
        return queryList(entityClass,options, null);
    }

    HttpResponse getResponse();
}
