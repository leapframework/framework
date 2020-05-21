/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.remote;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.http.client.HttpClient;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.orm.OrmContext;
import leap.orm.mapping.EntityMapping;
import leap.web.Request;
import leap.web.api.remote.ds.RestDataSource;
import leap.web.api.remote.ds.RestDatasourceManager;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public class DefaultRestResourceFactory implements RestResourceFactory {

    private static Log    logger = LogFactory.get(DefaultRestResourceFactory.class);
    private static String localIP;

    protected @Inject OrmContext            ormContext;
    protected @Inject HttpClient            httpClient;
    protected @Inject TokenFetcher          tokenFetcher;
    protected @Inject RestDatasourceManager dsm;

    @Override
    public RestResource createResource(Class<?> entityClass) {
        return createResource(ormContext, ormContext.getMetadata().getEntityMapping(entityClass));
    }

    @Override
    public RestResource createResource(Class<?> entityClass, String endpointUrl) {
        return createResource(ormContext, ormContext.getMetadata().getEntityMapping(entityClass), endpointUrl);
    }

    @Override
    public RestResource createResource(OrmContext context, EntityMapping em) {
        if (!em.isRemoteRest()) {
            return null;
        }

        RestResourceInfo rri = em.getExtension(RestResourceInfo.class);
        if (null == rri) {
            String basePath = em.getRemoteSettings().getEndpoint();
            if (Strings.isEmpty(basePath) && null != em.getRemoteSettings().getDataSource()) {
                RestDataSource ds = dsm.tryGetDataSource(em.getRemoteSettings().getDataSource());
                if (null == ds) {
                    throw new IllegalStateException("Remote dataSource '" + em.getRemoteSettings().getDataSource() +
                            "' not found, check entity '" + em.getEntityName() + "'");
                }
                basePath = ds.getEndpoint();
            }

            if (Strings.isEmpty(basePath)) {
                throw new IllegalStateException("Remote endpoint must be configured, check entity '" + em.getEntityName() + "'");
            }

            String endpoint = Paths.suffixWithSlash(basePath) + em.getRemoteSettings().getRelativePath();
            rri = new RestResourceInfo(formatApiEndPoint(endpoint));
            em.setExtension(RestResourceInfo.class, rri);
        }

        return doCreateResource(context, em, rri);
    }

    @Override
    public RestResource createResource(OrmContext context, EntityMapping em, String endpointUrl) {
        return doCreateResource(context, em, new RestResourceInfo(endpointUrl));
    }

    protected RestResource doCreateResource(OrmContext context, EntityMapping em, RestResourceInfo info) {
        DefaultRestResource restResource = new DefaultRestResource(em);
        restResource.setHttpClient(httpClient);
        restResource.setTokenFetcher(tokenFetcher);
        restResource.setEndpoint(info.getEndpoint());
        return restResource;
    }

    private static String formatApiEndPoint(String apiEndPoint) {
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

    public static final class RestResourceInfo {
        protected final String endpoint;

        public RestResourceInfo(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getEndpoint() {
            return endpoint;
        }
    }
}
