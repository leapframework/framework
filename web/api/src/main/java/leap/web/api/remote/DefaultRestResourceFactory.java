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
import leap.lang.path.Paths;
import leap.orm.OrmContext;
import leap.orm.mapping.EntityMapping;
import leap.web.api.remote.ds.RestDataSource;
import leap.web.api.remote.ds.RestDatasourceManager;

public class DefaultRestResourceFactory implements RestResourceFactory {

    protected @Inject HttpClient            httpClient;
    protected @Inject TokenFetcher          tokenFetcher;
    protected @Inject RestDatasourceManager dsm;

    @Override
    public RestResource createResource(OrmContext context, EntityMapping em) {
        if(!em.isRemote()) {
            return null;
        }

        RestResourceInfo rri = em.getExtension(RestResourceInfo.class);
        if(null == rri) {
            String basePath = em.getRemoteSettings().getEndpoint();
            if(Strings.isEmpty(basePath) && null != em.getRemoteSettings().getDataSource()) {
                RestDataSource ds = dsm.tryGetDataSource(em.getRemoteSettings().getDataSource());
                if(null == ds) {
                    throw new IllegalStateException("Remote dataSource '" + em.getRemoteSettings().getDataSource() +
                            "' not found, check entity '" + em.getEntityName() + "'");
                }
                basePath = ds.getEndpoint();
            }

            if(Strings.isEmpty(basePath)) {
                throw new IllegalStateException("Remote endpoint must be configured, check entity '" + em.getEntityName() + "'");
            }

            String endpoint = Paths.suffixWithSlash(basePath) + em.getRemoteSettings().getRelativePath();
            rri = new RestResourceInfo(RestResourceBuilder.formatApiEndPoint(endpoint));
            em.setExtension(RestResourceInfo.class, rri);
        }

        return doCreateResource(context, em, rri);
    }

    protected RestResource doCreateResource(OrmContext context, EntityMapping em, RestResourceInfo info) {
        DefaultRestResource restResource = new DefaultRestResource();
        restResource.setHttpClient(httpClient);
        restResource.setTokenFetcher(tokenFetcher);
        restResource.setEndpoint(info.getEndpoint());
        return restResource;
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
