package leap.web.api.remote;

import leap.lang.Out;
import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.HTTP;
import leap.lang.http.Headers;
import leap.lang.http.client.HttpClient;
import leap.lang.http.client.HttpRequest;
import leap.lang.http.client.HttpResponse;
import leap.lang.json.JSON;
import leap.lang.json.JsonParsable;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.oauth2.webapp.OAuth2Constants;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public abstract class AbstractRestResource implements RestResource {
    private final Log log = LogFactory.get(AbstractRestResource.class);

    protected final HttpClient    httpClient;
    protected final TokenStrategy tokenStrategy;

    private boolean                canNewAccessToken;
    private Consumer<HttpRequest>  preSendHandler;
    private Consumer<HttpResponse> postSendHandler;

    public AbstractRestResource(HttpClient httpClient, TokenStrategy tokenStrategy) {
        this.httpClient = httpClient;
        this.tokenStrategy = tokenStrategy;
    }

    protected Token getAccessToken() {
        return tokenStrategy.getToken();
    }

    protected abstract String getEndpoint();

    @Override
    public boolean isCanNewAccessToken() {
        return canNewAccessToken;
    }

    @Override
    public void setCanNewAccessToken(boolean canNewAccessToken) {
        this.canNewAccessToken = canNewAccessToken;
    }

    public Consumer<HttpRequest> getPreSendHandler() {
        return preSendHandler;
    }

    public void setPreSendHandler(Consumer<HttpRequest> preSendHandler) {
        this.preSendHandler = preSendHandler;
    }

    public Consumer<HttpResponse> getPostSendHandler() {
        return postSendHandler;
    }

    public void setPostSendHandler(Consumer<HttpResponse> postSendHandler) {
        this.postSendHandler = postSendHandler;
    }

    protected String buildOperationPath(String relativePath) {
        String baseUrl = getEndpoint();
        return mergePath(baseUrl, relativePath);
    }

    protected String mergePath(String parentPath, String relativePath) {
        parentPath = Strings.trimEnd(parentPath, Paths.UNIX_SEPARATOR);
        if (Strings.isEmpty(relativePath)) {
            return parentPath;
        }
        if (!relativePath.startsWith(Paths.UNIX_SEPARATOR_STR)) {
            relativePath = Paths.UNIX_SEPARATOR_STR + relativePath;
        }
        return parentPath + relativePath;
    }

    protected void send(HttpRequest request, Token at, Consumer<HttpResponse> consumer) {
        if (at != null) {
            request.setHeader(Headers.AUTHORIZATION, OAuth2Constants.BEARER + " " + at.getValue());
        }
        if (this.preSendHandler != null) {
            preSendHandler.accept(request);
        }

        HttpResponse response = request.send();
        if (response.getStatus() == HTTP.SC_UNAUTHORIZED && at != null) {
            at = at.refresh();
            if (null == at) {
                throw new RestResourceInvokeException(response);
            }
            request.setHeader(Headers.AUTHORIZATION, OAuth2Constants.BEARER + " " + at.getValue());
            response = request.send();
        }
        consumer.accept(response);

        if (this.postSendHandler != null) {
            postSendHandler.accept(response);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T send(Type targetType, HttpRequest request, Token at) {
        final Out<T> out = new Out<>();

        send(request, at, (response) -> {
            String content = response.getString();
            log.debug("status:{},Received response : {}", response.getStatus(), content);
            if (response.is2xx()) {
                if (Boolean.class.equals(targetType)) {
                    out.set((T) Boolean.TRUE);
                    return;
                }

                if (Strings.isEmpty(content)) {
                    return;
                }
                if (targetType != null && ContentTypes.APPLICATION_JSON_TYPE.isCompatible(response.getContentType())) {
                    out.accept((T) decode(targetType, content));
                }
                return;
            }
            if (response.isNotFound()) {
                return;
            }
            throw new RestResourceInvokeException(response);
        });

        if (out.isEmpty()) {
            return null;
        }

        return out.get();
    }

    @SuppressWarnings("unchecked")
    protected <T> T decode(Type targetType, String jsonStr) {
        if (Strings.isEmpty(jsonStr)) {
            return null;
        }
        if (targetType instanceof ParameterizedType) {
            ParameterizedType pType       = ((ParameterizedType) targetType);
            Class<?>          rawType     = (Class<?>) pType.getRawType();
            Type[]            actualTypes = pType.getActualTypeArguments();
            if (rawType.isAssignableFrom(List.class)) {
                Object[] arr = JSON.decodeArray(jsonStr, (Class<?>) actualTypes[0]);
                T        val = (T) Arrays.stream(arr).collect(Collectors.toList());
                return val;
            } else if (rawType.isAssignableFrom(Map.class)) {
                Map<String, Object> val = JSON.decodeMap(jsonStr, (Class<?>) actualTypes[0]);
                return (T) val;
            } else {
                throw new RuntimeException("unsupported type:" + targetType.getTypeName());
            }
        } else {
            T        val;
            Class<T> cls = (Class<T>) targetType;
            if (JsonParsable.class.isAssignableFrom(cls)) {
                JsonParsable temp;
                try {
                    temp = (JsonParsable) cls.newInstance();
                    temp.parseJson(jsonStr);
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                val = (T) temp;
            } else {
                val = JSON.decode(jsonStr, (Class<T>) targetType);
            }
            return val;
        }
    }

    protected HttpRequest buildQueryOption(HttpRequest request, QueryOptionsBase queryOptions) {
        if (queryOptions == null) {
            return request;
        }
        if (Strings.isNotEmpty(queryOptions.getSelect())) {
            request.addQueryParam("select", queryOptions.getSelect());
        }
        if (Strings.isNotEmpty(queryOptions.getExpand())) {
            request.addQueryParam("expand", queryOptions.getExpand());
        }
        if (queryOptions instanceof QueryOptions) {
            QueryOptions qo = (QueryOptions) queryOptions;
            if (qo.getPageSize() != null) {
                request.addQueryParam("page_size", qo.getPageSize().intValue() + "");
            }
            if (qo.getPageIndex() != null) {
                request.addQueryParam("page", qo.getPageIndex().intValue() + "");
            }
            if (qo.getLimit() != null) {
                request.addQueryParam("limit", qo.getLimit().intValue() + "");
            }
            if (qo.getOffset() != null) {
                request.addQueryParam("offset", qo.getOffset().intValue() + "");
            }
            if (qo.isTotal()) {
                request.addQueryParam("total", "true");
            }
            if (Strings.isNotEmpty(qo.getOrderBy())) {
                request.addQueryParam("orderby", qo.getOrderBy());
            }
            if (Strings.isNotEmpty(qo.getFilters())) {
                request.addQueryParam("filters", qo.getFilters());
            }
            if (Strings.isNotEmpty(qo.getJoins())) {
                request.addQueryParam("joins", qo.getJoins());
            }
            if (Strings.isNotEmpty(qo.getGroupBy())) {
                request.addQueryParam("groupby", qo.getGroupBy());
            }
            if (Strings.isNotEmpty(qo.getAggregates())) {
                request.addQueryParam("aggregates", qo.getAggregates());
            }
        }
        return request;
    }
}
