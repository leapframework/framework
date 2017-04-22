/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.restd;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ds.DataSourceManager;
import leap.lang.Strings;
import leap.lang.path.AntPathPattern;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.mapping.EntityMapping;
import leap.web.App;
import leap.web.api.config.ApiConfigException;
import leap.web.api.config.ApiConfigProcessor;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.RestdConfig;
import leap.web.route.RouteManager;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RestdApiProcessor implements ApiConfigProcessor {

    protected @Inject App               app;
    protected @Inject BeanFactory       factory;
    protected @Inject RouteManager      rm;
    protected @Inject RestdApiCreator[] creators;

    @Override
    public void preProcess(ApiConfigurator api) {
        RestdConfig c = api.getRestdConfig();
        if(null == c) {
            return;
        }

        OrmContext  oc = lookupOrmContext(c);
        OrmMetadata om = oc.getMetadata();

        Set<EntityMapping> models = computeModels(c, om);

        RestdApiCreatorContext context = new RestdApiCreatorContext() {
            @Override
            public RestdConfig getConfig() {
                return c;
            }

            @Override
            public OrmContext getOrmContext() {
                return oc;
            }

            @Override
            public Set<EntityMapping> getIncludedModels() {
                return models;
            }
        };

        for(RestdApiCreator creator : creators) {
            creator.process(app, api, context);
        }
    }

    protected OrmContext lookupOrmContext(RestdConfig c) {
        String dataSourceName =
                Strings.firstNotEmpty(c.getDataSourceName(), DataSourceManager.DEFAULT_DATASOURCE_NAME);

        OrmContext oc = factory.tryGetBean(OrmContext.class, dataSourceName);

        if(null == oc) {
            throw new ApiConfigException("Can't create restd api , data source '" + dataSourceName + "' has not been configured!");
        }

        return oc;
    }

    protected Set<EntityMapping> computeModels(RestdConfig c, OrmMetadata om) {
        List<EntityMapping> ems = om.getEntityMappingSnapshotList();

        //computes inclusion first.
        Set<EntityMapping> includes = new LinkedHashSet<>();
        boolean includesAll = c.getIncludedModels().isEmpty();
        if(!includesAll) {
            ems.forEach(em -> {
                c.getIncludedModels().forEach(pattern -> {
                    if(matchesModel(pattern, em.getEntityName())) {
                        includes.add(em);
                    }
                });
            });
        }else{
            includes.addAll(ems);
        }

        //computes exclusion.
        if(!includes.isEmpty()) {
            Set<EntityMapping> excludes = new HashSet<>();

            includes.forEach(em -> {
                c.getExcludedModels().forEach(pattern -> {
                    if(matchesModel(pattern, em.getEntityName())) {
                        excludes.add(em);
                    }
                });
            });

            if(!excludes.isEmpty()) {
                excludes.forEach(includes::remove);
            }
        }

        return includes;
    }

    protected boolean matchesModel(String pattern, String name) {
        if(pattern.equals("*")) {
            return true;
        }else{
            return new AntPathPattern(pattern).matches(name);
        }
    }
}
