package leap.web.api.remote;

import leap.core.annotation.Inject;
import leap.lang.Out;
import leap.lang.Strings;
import leap.lang.expirable.TimeExpirableSeconds;
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
import leap.oauth2.webapp.token.at.AccessToken;
import leap.web.Request;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.remote.json.TypeReference;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public abstract class AbstractRestResource implements RestResource {
    private final Log log = LogFactory.get(AbstractRestResource.class);

    protected @Inject HttpClient   httpClient;
    protected @Inject TokenFetcher tokenFetcher;

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setTokenFetcher(TokenFetcher tokenFetcher) {
        this.tokenFetcher = tokenFetcher;
    }

    protected abstract String getEndpoint();

    protected AccessToken getAccessToken() {
        Request request = Request.tryGetCurrent();
        if (request == null) {
            return null;
        }
        AccessToken at = tokenFetcher.getAccessToken(request);
        return at;
    }

    /**
     * 构造要调用的服务的请求地址
     *
     * @param relativePath
     * @return
     */
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

    protected void send(HttpRequest request, AccessToken at, Consumer<HttpResponse> consumer) {
        if (at != null) {
            request.addHeader(Headers.AUTHORIZATION, OAuth2Constants.BEARER + " " + at.getToken());
        }
        HttpResponse response = request.send();
        if (response.getStatus() == HTTP.SC_UNAUTHORIZED && at != null) {
            ((TimeExpirableSeconds) at).setExpiresIn(0);
            at = tokenFetcher.refreshAccessToken(at);
            request.addHeader(Headers.AUTHORIZATION, OAuth2Constants.BEARER + " " + at.getToken());
            response = request.send();
        }
        consumer.accept(response);
    }

    /**
     * 发送请求
     *
     * @param targetType 返回值类型，
     *                   如果是List<T>类型，需要构造对应的{@link TypeReference}，如：new TypeReference<List<GatewayRuntime>>(){}.getType()
     */
    @SuppressWarnings("unchecked")
    protected <T> T send(Type targetType, HttpRequest request, AccessToken at) {
        final Out<T> out = new Out<>();

        send(request, at, (response) -> {
            String content = response.getString();
            log.debug("status:{},Received response : {}", response.getStatus(), content);
            if (response.is2xx()) {
                if(Boolean.class.equals(targetType)) {
                    out.set((T)Boolean.TRUE);
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
            if(response.isNotFound()) {
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
                request.addQueryParam("orderBy", qo.getOrderBy());
            }
            if (Strings.isNotEmpty(qo.getFilters())) {
                request.addQueryParam("filters", qo.getFilters());
            }
            if (Strings.isNotEmpty(qo.getJoins())) {
                request.addQueryParam("joins", qo.getJoins());
            }
        }
        return request;
    }
}
