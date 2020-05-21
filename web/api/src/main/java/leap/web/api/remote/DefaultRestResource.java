package leap.web.api.remote;

import leap.core.value.Record;
import leap.core.value.SimpleRecord;
import leap.lang.Out;
import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.HTTP.Method;
import leap.lang.http.client.HttpClient;
import leap.lang.http.client.HttpRequest;
import leap.lang.json.JSON;
import leap.lang.json.JsonSettings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.webapp.token.at.AccessToken;
import leap.orm.mapping.EntityMapping;
import leap.web.api.mvc.params.CountOptions;
import leap.web.api.mvc.params.DeleteOptions;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.remote.json.TypeReference;
import leap.web.api.restd.CrudUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultRestResource extends AbstractRestResource {
    private final Log log = LogFactory.get(DefaultRestResource.class);

    protected final EntityMapping em;
    protected final String        endpoint;

    public DefaultRestResource(HttpClient httpClient, TokenStrategy tokenStrategy, EntityMapping em, String endpoint) {
        super(httpClient, tokenStrategy);
        this.em = em;
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public <T> T insert(Class<T> resultClass, Object obj) {
        String op = "";
        HttpRequest request = httpClient.request(buildOperationPath(op))
                .ajax()
                .setJson(JSON.encode(obj, JsonSettings.MIN))
                .setMethod(Method.POST);
        T val = send(resultClass, request, getAccessToken());
        return val;
    }

    @Override
    public Record create(Map<String, Object> properties) {
        return new SimpleRecord(insert(Map.class, properties));
    }

    @Override
    public boolean delete(Object id, DeleteOptions options) {
        String op = idPath(id);

        HttpRequest request = httpClient.request(buildOperationPath(op))
                .ajax()
                .setMethod(Method.DELETE);

        if (options != null && options.isCascadeDelete()) {
            request.addQueryParam("cascade_delete", "true");
        }

        return Boolean.TRUE == send(Boolean.class, request, getAccessToken());
    }

    @Override
    public boolean update(Object id, Object partial) {
        String op = idPath(id);

        HttpRequest request = httpClient.request(buildOperationPath(op))
                .ajax()
                .setJson(JSON.encode(partial, JsonSettings.MIN))
                .setMethod(Method.PATCH);

        return Boolean.TRUE == send(Boolean.class, request, getAccessToken());
    }

    @Override
    public <T> T find(Class<T> entityClass, Object id, QueryOptionsBase options) {
        String op = idPath(id);

        return doFind(entityClass, buildOperationPath(op), options);
    }

    @Override
    public <T> T findRelationOne(Class<T> resultClass, String relationPath, Object id, QueryOptionsBase options) {
        String op = idPath(id) + "/" + relationPath;

        return doFind(resultClass, buildOperationPath(op), options);
    }

    protected <T> T doFind(Class<T> resultClass, String url, QueryOptionsBase options) {
        HttpRequest request = httpClient.request(url)
                .ajax()
                .setMethod(Method.GET);

        buildQueryOption(request, options);

        return send(resultClass, request, getAccessToken());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> RestQueryListResult<T> queryList(Class<T> resultElementClass, final QueryOptions options, Map<String, Object> filters) {
        String op = "";

        applyFilters(options, filters);

        return doQueryList(resultElementClass, buildOperationPath(op), options);
    }

    @Override
    public <T> RestQueryListResult<T> queryRelationList(Class<T> resultElementClass, String relationPath, Object id, QueryOptions options) {
        String op = idPath(id) + "/" + relationPath;
        return doQueryList(resultElementClass, buildOperationPath(op), options);
    }

    protected <T> RestQueryListResult<T> doQueryList(Class<T> resultElementClass, String url, QueryOptions options) {
        HttpRequest request = httpClient.request(url)
                .ajax()
                .setMethod(Method.GET);

        buildQueryOption(request, options);

        final Out<RestQueryListResult<T>> out = new Out<>();
        final Type targetType = new TypeReference<List<T>>(resultElementClass) {
        }.getType();

        send(request, getAccessToken(), (response) -> {
            String content = response.getString();
            log.debug("status:{},Received response : {}", response.getStatus(), content);
            if (response.is2xx()) {
                List<T> list = new ArrayList<>();
                if (Strings.isNotEmpty(content) && ContentTypes.APPLICATION_JSON_TYPE.isCompatible(response.getContentType())) {
                    list = (List<T>) decode(targetType, content);
                }
                int    count    = list.size();
                String countStr = response.getHeader("X-Total-Count");
                if (options.isTotal() && Strings.isNotBlank(countStr)) {
                    count = Integer.parseInt(countStr);
                }
                out.accept(new RestQueryListResult<T>(list, count));
                return;
            }
            throw new RestResourceInvokeException(response);
        });
        if (out.isEmpty()) {
            return null;
        }
        return out.get();
    }

    @Override
    public int count(CountOptions options) {
        String op = "/count";

        HttpRequest request = httpClient.request(buildOperationPath(op))
                .ajax()
                .setMethod(Method.GET);

        if (options != null && Strings.isNotEmpty(options.getFilters())) {
            request.addQueryParam("filters", options.getFilters());
        }

        Integer val = send(Integer.class, request, getAccessToken());
        return val == null ? 0 : val.intValue();
    }

    protected String idPath(Object id) {
        return null == em ? "/" + id : CrudUtils.getIdPath(em, id);
    }

    protected void applyFilters(QueryOptions options, Map<String, Object> filters) {
        if (null == filters || filters.isEmpty()) {
            return;
        }
        StringBuilder filtersBuilder = new StringBuilder();
        String        opFilters      = options.getFilters();

        if (!Strings.isEmpty(opFilters)) {
            filtersBuilder.append(opFilters);
        }

        filters.forEach((field, value) -> {
            if (!Strings.isEmpty(filtersBuilder.toString())) {
                filtersBuilder.append(" and ");
            }
            if (null == value) {
                filtersBuilder.append(field).append(" is null");
            } else if (value instanceof String) {
                filtersBuilder.append(field).append(" eq ").append(value);
            } else if (value instanceof List) {
                List list = (List) value;
                if (!list.isEmpty()) {
                    filtersBuilder.append(field).append(" in ").append(Strings.join(list, ","));
                }
            }
        });

        options.setFilters(filtersBuilder.toString());
    }
}
