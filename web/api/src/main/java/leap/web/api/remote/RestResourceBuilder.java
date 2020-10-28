package leap.web.api.remote;

import leap.core.AppContext;
import leap.lang.Assert;
import leap.lang.Strings;
import leap.lang.http.client.HttpRequest;
import leap.lang.http.client.HttpResponse;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.oauth2.webapp.token.at.AccessToken;
import leap.orm.enums.RemoteType;
import leap.orm.mapping.EntityMapping;
import leap.web.Request;
import leap.web.api.mvc.ApiErrorHandler;
import leap.web.api.remote.ds.RestDataSource;
import leap.web.api.remote.ds.RestDatasourceManager;
import javax.servlet.http.HttpServletRequest;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.function.Consumer;

public class RestResourceBuilder {
    private static Log logger = LogFactory.get(RestResourceBuilder.class);

    private static String localIP;

    private String                 endpoint;
    private EntityMapping          entityMapping;
    private AccessToken            accessToken;
    private ApiErrorHandler        apiErrorHandler;
    private Consumer<HttpRequest>  preSendHandler;
    private Consumer<HttpResponse> postSendHandler;

    public static RestResourceBuilder newBuilder() {
        return new RestResourceBuilder();
    }

    public RestResource build() {
        DefaultRestResource res = AppContext.factory().inject(new DefaultRestResource(entityMapping));
        res.setPreSendHandler(this.preSendHandler);
        res.setPostSendHandler(this.postSendHandler);
        if(accessToken!=null){
            res.setAccessToken(accessToken);
        }
        if (entityMapping != null) {
            RestDatasourceManager manager  = getDataSourceManager();
            RestDataSource        ds       = manager.tryGetDataSource(entityMapping.getRemoteSettings().getDataSource());
            String                basePath = entityMapping.getRemoteSettings().getEndpoint();
            if (ds != null && Strings.isNotEmpty(ds.getEndpoint())) {
                basePath = ds.getEndpoint();
            }
            if (Strings.isNotEmpty(basePath)) {
                String url = Paths.suffixWithSlash(basePath) + entityMapping.getRemoteSettings().getRelativePath();
                res.setEndpoint(url);
            }
        }

        if (Strings.isNotBlank(endpoint)) {
            res.setEndpoint(endpoint);
        }

        if (Strings.isEmpty(res.getEndpoint())) {
            throw new RuntimeException("can't build rest resource,when endpoint or entityMapping is empty!");
        }
        res.setEndpoint(formatApiEndPoint(res.getEndpoint()));

        if (null != apiErrorHandler) {
            res.setApiErrorHandler(apiErrorHandler);
        }
        return res;
    }

    public static String formatApiEndPoint(String apiEndPoint) {
        if (apiEndPoint.contains("{context}")) {
            String contextPath = Request.tryGetCurrent().getServletRequest().getContextPath();
            apiEndPoint = apiEndPoint.replace("{context}", Strings.trimStart(contextPath, '/'));
            //apiEndPoint=apiEndPoint.replace("//", "/");
        }
        if (apiEndPoint.contains("~")) {
            HttpServletRequest request = Request.tryGetCurrent().getServletRequest();
            apiEndPoint = apiEndPoint.replace("~", Strings.format("{0}://{1}:{2}", request.getScheme(), curServerLocalIp(), request.getLocalPort()));
        }
        if (apiEndPoint.startsWith("/")) {
            HttpServletRequest request = Request.tryGetCurrent().getServletRequest();
            apiEndPoint = Strings.format("{0}://{1}:{2}", request.getScheme(), "127.0.0.1", request.getLocalPort()) + apiEndPoint;
        }

        if (Strings.endsWith(apiEndPoint, "/")) {
            apiEndPoint = Strings.trimEnd(apiEndPoint, '/');
        }
        return apiEndPoint;
    }

    private static String curServerLocalIp() {
        if (!Strings.isEmpty(localIP)) {
            return localIP;
        }

        String serverIP = null;
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();

                if (!ni.getInetAddresses().hasMoreElements()) {
                    continue;
                }
                //ip = (InetAddress) ni.getInetAddresses().nextElement();
                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                for (InetAddress ip : Collections.list(inetAddresses)) {
                    logger.info("ip info:{},,isLoop:{},hostAddr:{},isSiteLocal:{}", ip, ip.isLoopbackAddress(), ip.getHostAddress(), ip.isSiteLocalAddress());
                    if (ip instanceof Inet6Address || ip.isLoopbackAddress()) {
                        continue;
                    }
                    serverIP = ip.getHostAddress();
                    if (ip.isSiteLocalAddress()) {
                        break;
                    }
                }
                if (Strings.isNotBlank(serverIP)) {
                    break;
                }
            }
        } catch (SocketException ex) {
            logger.error(ex.getMessage(), ex);
        }

        localIP = serverIP;

        return localIP;
    }

    private RestDatasourceManager getDataSourceManager() {
        return AppContext.getBean(RestDatasourceManager.class);
    }

    public RestResourceBuilder setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public RestResourceBuilder setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public ApiErrorHandler getApiErrorHandler() {
        return apiErrorHandler;
    }

    public RestResourceBuilder setApiErrorHandler(ApiErrorHandler apiErrorHandler) {
        this.apiErrorHandler = apiErrorHandler;
        return this;
    }

    public Consumer<HttpRequest> getPreSendHandler() {
        return preSendHandler;
    }

    public RestResourceBuilder setPreSendHandler(Consumer<HttpRequest> preSendHandler) {
        this.preSendHandler = preSendHandler;
        return this;
    }

    public Consumer<HttpResponse> getPostSendHandler() {
        return postSendHandler;
    }

    public RestResourceBuilder setPostSendHandler(Consumer<HttpResponse> postSendHandler) {
        this.postSendHandler = postSendHandler;
        return this;
    }

    public RestResourceBuilder setEntityMapping(EntityMapping entityMapping) {
        Assert.notNull(entityMapping, "entity mapping can't be null.");
        Assert.isTrue(entityMapping.isRemote() && RemoteType.rest.equals(entityMapping.getRemoteSettings().getRemoteType())
                , "entity must be remote rest model.");
        this.entityMapping = entityMapping;
        return this;
    }
}
