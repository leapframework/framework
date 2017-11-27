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

import leap.core.annotation.Inject;
import leap.core.ds.DataSourceManager;
import leap.core.meta.MTypeContainer;
import leap.lang.Strings;
import leap.lang.meta.MComplexType;
import leap.lang.path.AntPathPattern;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.OrmRegistry;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.metadata.OrmMTypeFactory;
import leap.web.App;
import leap.web.api.Api;
import leap.web.api.config.ApiConfigException;
import leap.web.api.config.ApiConfigProcessor;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.RestdConfig;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.ApiMetadataContext;
import leap.web.api.meta.ApiMetadataFactory;
import leap.web.api.meta.ApiMetadataProcessor;

import java.util.*;

/**
 * Api configuration processor for restd.
 */
public class RestdApiConfigProcessor implements ApiConfigProcessor, ApiMetadataProcessor {

    protected @Inject App                app;
    protected @Inject RestdStrategy      strategy;
    protected @Inject RestdProcessor[]   processors;
    protected @Inject OrmMTypeFactory    omf;
    protected @Inject OrmRegistry        ormRegistry;
    protected @Inject ApiMetadataFactory amf;

    @Override
    public void preProcess(Api api) {
        RestdConfig rc = api.getConfig().getRestdConfig();
        if (null == rc) {
            return;
        }

        OrmContext  oc  = lookupOrmContext(rc);
        OrmMetadata om  = oc.getMetadata();
        Dao         dao = oc.getDao();

        Set<EntityMapping> ormModels   = computeOrmModels(rc, om);
        Set<RestdModel>    restdModels = createRestdModels(api, rc, oc, ormModels);

        SimpleRestdContext restdContext = new SimpleRestdContext(api, rc);
        restdContext.setDao(dao);
        restdContext.setModels(Collections.unmodifiableSet(restdModels));

        processRestdApi(api, restdContext);

        api.getConfig().setExtension(RestdContext.class, restdContext);
    }

    @Override
    public void preProcess(ApiMetadataContext context, ApiMetadataBuilder m) {
        RestdContext restdContext = context.getApi().getConfig().removeExtension(RestdContext.class);
        if(null == restdContext) {
            return;
        }

        OrmContext oc = restdContext.getDao().getOrmContext();

        //create models.
        MTypeContainer mtc = context.getMTypeContainer();
        for (RestdModel rm : restdContext.getModels()) {
            MComplexType mtype = omf.getMType(mtc, oc, rm.getEntityMapping());

            if(null == m.tryGetModel(rm.getEntityMapping().getEntityClass())) {
                amf.tryAddModel(context, m, mtype);
            }
        }

        for(MComplexType ct : mtc.getComplexTypes().values()) {
            amf.tryAddModel(context, m, ct);
        }
    }

    protected void processRestdApi(Api api, RestdContext context) {
        ApiConfigurator c = api.getConfigurator();

        for(RestdProcessor p : processors) {
            p.preProcessApi(c, context);
        }

        for(RestdModel model : context.getModels()) {
            for(RestdProcessor p : processors) {
                p.preProcessModel(c, context, model);
            }
        }

        for(RestdModel model : context.getModels()) {
            for(RestdProcessor p : processors) {
                p.postProcessModel(c, context, model);
            }
        }

        for(RestdProcessor p : processors) {
            p.postProcessApi(c, context);
        }

    }

    protected OrmContext lookupOrmContext(RestdConfig c) {
        OrmContext oc = c.getOrmContext();

        if(null == oc) {
            String dataSourceName =
                    Strings.firstNotEmpty(c.getDataSourceName(), DataSourceManager.DEFAULT_DATASOURCE_NAME);

            oc = ormRegistry.findContext(dataSourceName);
            if (null == oc) {
                throw new ApiConfigException("Can't create restd api , orm context '" + dataSourceName + "' has not been registered!");
            }
        }

        return oc;
    }

    protected Set<RestdModel> createRestdModels(Api api, RestdConfig c, OrmContext oc, Set<EntityMapping> ormModels) {
        Set<RestdModel> restdModels = new LinkedHashSet<>();

        for (EntityMapping em : ormModels) {
            RestdModel.Builder rm = new RestdModel.Builder(em);

            RestdConfig.Model cm = c.getModel(em.getEntityName());
            if(null != cm && !Strings.isEmpty(cm.getPath())) {
                rm.setPath(cm.getPath());
            }else {
                rm.setPath(strategy.getDefaultModelPath(em.getEntityName()));
            }

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
            	if(em.isRemote()){
            		excludes.add(em);
            		return;
            	}
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
