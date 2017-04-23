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
import leap.core.meta.MTypeContainer;
import leap.lang.Strings;
import leap.lang.meta.MComplexType;
import leap.lang.path.AntPathPattern;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.mapping.EntityMapping;
import leap.orm.metadata.OrmMTypeFactory;
import leap.web.App;
import leap.web.api.config.ApiConfigException;
import leap.web.api.config.ApiConfigProcessor;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.RestdConfig;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.ApiMetadataContext;
import leap.web.api.meta.ApiMetadataProcessor;
import leap.web.api.meta.model.MApiModelBuilder;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RestdApiProcessor implements ApiConfigProcessor, ApiMetadataProcessor {

    protected @Inject App               app;
    protected @Inject BeanFactory       factory;
    protected @Inject RestdStrategy     strategy;
    protected @Inject RestdApiCreator[] creators;
    protected @Inject OrmMTypeFactory   omf;

    @Override
    public void preProcess(ApiConfigurator api) {
        RestdConfig c = api.getRestdConfig();
        if (null == c) {
            return;
        }

        OrmContext oc = lookupOrmContext(c);
        OrmMetadata om = oc.getMetadata();

        Set<EntityMapping> ormModels = computeOrmModels(c, om);
        Set<RestdModel> restdModels = createRestdModels(api, c, oc, ormModels);

        RestdApiConfigContext context = new RestdApiConfigContext() {
            @Override
            public RestdConfig getConfig() {
                return c;
            }

            @Override
            public OrmContext getOrmContext() {
                return oc;
            }

            @Override
            public Set<RestdModel> getModels() {
                return restdModels;
            }
        };

        for (RestdApiCreator creator : creators) {
            creator.process(app, api, context);
        }

        api.setExtension(RestdApiConfigContext.class, context);
    }

    @Override
    public void preProcess(ApiMetadataContext context, ApiMetadataBuilder m) {
        RestdApiConfigContext cc = context.getConfig().removeExtension(RestdApiConfigContext.class);
        if (null != cc) {
            MTypeContainer mtc = context.getMTypeContainer();
            for (RestdModel rm : cc.getModels()) {

                MComplexType mtype = omf.getMType(mtc, cc.getOrmContext(), rm.getEntityMapping());
                MApiModelBuilder model = new MApiModelBuilder(mtype);

                m.addModel(model);
            }
        }
    }

    protected OrmContext lookupOrmContext(RestdConfig c) {
        String dataSourceName =
                Strings.firstNotEmpty(c.getDataSourceName(), DataSourceManager.DEFAULT_DATASOURCE_NAME);

        OrmContext oc = factory.tryGetBean(OrmContext.class, dataSourceName);

        if (null == oc) {
            throw new ApiConfigException("Can't create restd api , data source '" + dataSourceName + "' has not been configured!");
        }

        return oc;
    }

    protected void registerApiModel(ApiConfigurator api, RestdModel rm) {

    }

    protected Set<RestdModel> createRestdModels(ApiConfigurator api, RestdConfig c, OrmContext oc, Set<EntityMapping> ormModels) {
        Set<RestdModel> restdModels = new LinkedHashSet<>();

        for (EntityMapping em : ormModels) {

            RestdModel.Builder rm = new RestdModel.Builder(em);

            rm.setPath(strategy.getDefaultModelPath(em.getEntityName()));

            restdModels.add(rm.build());
        }

        return restdModels;
    }

    protected Set<EntityMapping> computeOrmModels(RestdConfig c, OrmMetadata om) {
        List<EntityMapping> ems = om.getEntityMappingSnapshotList();

        //computes inclusion first.
        Set<EntityMapping> includes = new LinkedHashSet<>();
        boolean includesAll = c.getIncludedModels().isEmpty();
        if (!includesAll) {
            ems.forEach(em -> {
                c.getIncludedModels().forEach(pattern -> {
                    if (matchesModel(pattern, em.getEntityName())) {
                        includes.add(em);
                    }
                });
            });
        } else {
            includes.addAll(ems);
        }

        //computes exclusion.
        if (!includes.isEmpty()) {
            Set<EntityMapping> excludes = new HashSet<>();

            includes.forEach(em -> {
                c.getExcludedModels().forEach(pattern -> {
                    if (matchesModel(pattern, em.getEntityName())) {
                        excludes.add(em);
                    }
                });
            });

            if (!excludes.isEmpty()) {
                excludes.forEach(includes::remove);
            }
        }

        return includes;
    }

    protected boolean matchesModel(String pattern, String name) {
        if (pattern.equals("*")) {
            return true;
        } else {
            return new AntPathPattern(pattern).matches(name);
        }
    }
}
