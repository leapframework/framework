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

package leap.web.api.orm;

import leap.core.value.Record;
import leap.lang.New;
import leap.lang.Strings;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.RelationMapping;
import leap.orm.mapping.RelationType;
import leap.orm.query.CriteriaQuery;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.remote.RestQueryListResult;
import leap.web.api.remote.RestResource;
import leap.web.api.restd.CrudUtils;
import leap.web.exception.BadRequestException;

import java.util.*;

public class DefaultRelationQueryExecutor extends ModelExecutorBase<RelationExecutorContext> implements RelationQueryExecutor {

    protected final RelationQueryExtension ex;
    protected final EntityMapping          tem;
    protected final RelationMapping        rm;
    protected final String                 rp;
    protected final RelationMapping        irm;
    protected final InverseQueryExecutor   iqe;

    public DefaultRelationQueryExecutor(RelationExecutorContext context, RelationQueryExtension ex) {
        super(context);
        this.ex = ex;
        this.tem = context.getInverseEntityMapping();
        this.rm = context.getRelation();
        this.rp = !Strings.isEmpty(context.getRelationPath()) ? context.getRelationPath() : Strings.lowerUnderscore(rm.getName());
        this.irm = context.getInverseRelation();
        this.iqe = new InverseQueryExecutor(context.newInverseExecutorContext(), irm);
    }

    @Override
    public QueryOneResult queryOne(Object id, QueryOptionsBase options) {
        if (!rm.isManyToOne()) {
            throw new IllegalStateException("Relation '" + rm.getName() + "' must be " + RelationType.MANY_TO_ONE + "' for query one");
        }

        RelationExecutionContext context = new
                DefaultRelationExecutionContext(DefaultRelationQueryExecutor.this.context);

        ex.preRelateQueryOne(context, id, options);

        Record record;

        List<ExpandError> expandErrors = null;
        if (remoteRest) {
            record = queryOneRemoteSource(id, options);
        } else if (tem.isRemoteRest()) {
            record = queryOneRemoteTarget(id, options);
        } else {
            expandErrors = new ArrayList<>();
            record = iqe.queryOneByRelation(context, id, options, expandErrors);
        }

        ex.postRelateQueryOne(context, id, record);

        QueryOneResult result = new QueryOneResult(record, expandErrors);
        ex.completeRelateQueryOne(context, id, result);

        return result;
    }

    protected Record queryOneRemoteSource(Object id, QueryOptionsBase options) {
        RestResource restResource = restResourceFactory.createResource(dao.getOrmContext(), em);
        return restResource.findRelationOne(rp, id, options);
    }

    protected Record queryOneRemoteTarget(Object id, QueryOptionsBase options) {
        String[] fields = Arrays.stream(rm.getJoinFields())
                .map(joinField -> joinField.getLocalFieldName())
                .toArray(String[]::new);

        Record record = dao.createCriteriaQuery(em).select(fields).whereById(id).firstOrNull();
        if (null == record) {
            throw new BadRequestException("Record " + em.getEntityName() + "(" + id + ") not found");
        }

        Object targetId = CrudUtils.getSingleOrMap(record, fields);
        if (null == targetId) {
            return null;
        }

        return iqe.queryOne(targetId, options).record;
    }

    @Override
    public QueryListResult queryList(Object id, QueryOptions options) {
        RelationExecutionContext context = new
                DefaultRelationExecutionContext(DefaultRelationQueryExecutor.this.context);

        ex.preRelateQueryList(context, id, options);

        QueryListResult result;
        if (remoteRest) {
            result = queryListRemoteSource(id, options);
        } else if (rm.isEmbedded()) {
            result = queryListEmbedded(id, options);
        } else if (tem.isRemoteRest()) {
            result = queryListRemoteTarget(id, options);
        } else {
            result = iqe.queryListByRelation(id, options);
        }

        ex.postRelateQueryList(context, id, result.getList());
        ex.completeRelateQueryList(context, id, result);

        return result;
    }

    protected QueryListResult queryListEmbedded(Object id, QueryOptions options) {
        Record record = dao.createCriteriaQuery(em).whereById(id).select(rm.getEmbeddedFileName()).firstOrNull();
        if (null == record) {
            throw new BadRequestException("Record " + em.getEntityName() + "(" + id + ") not found");
        }

        Set<Object> embeddedIds = new HashSet<>();
        iqe.calcIdsByEmbeddedField(embeddedIds, record, rm.getEmbeddedFileName());
        if (embeddedIds.isEmpty()) {
            return QueryListResult.EMPTY;
        }

        return iqe.queryListByIds(embeddedIds, options);
    }

    protected QueryListResult queryListRemoteSource(Object id, QueryOptions options) {
        RestResource restResource = restResourceFactory.createResource(dao.getOrmContext(), em);

        RestQueryListResult<Record> result = restResource.queryRelationList(rp, id, options);

        return new QueryListResult(result.getList(), result.getCount());
    }

    protected QueryListResult queryListRemoteTarget(Object id, QueryOptions options) {
        //todo:
        throw new IllegalStateException("queryListRemoteTarget not implemented");
    }

    protected static class InverseQueryExecutor extends DefaultModelQueryExecutor {
        private final RelationMapping rm;

        public InverseQueryExecutor(ModelExecutorContext context, RelationMapping rm) {
            super(context);
            this.rm = rm;
        }

        public Record queryOneByRelation(ModelExecutionContext context, Object relatedId, QueryOptionsBase options, List<ExpandError> expandErrors) {
            CriteriaQuery<Record> query =
                    createCriteriaQuery().joinById(rm.getTargetEntityName(), rm.getName(), "j", relatedId);

            JoinModels joinModels = new JoinModels("j", new ModelAndMapping(am, em));
            applySelect(query, options, joinModels);

            Record record = query.firstOrNull();

            List<ExpandError> ees = expandOne(context, record, options);
            if (null != ees) {
                expandErrors.addAll(ees);
            }

            return record;
        }

        public QueryListResult queryListByRelation(Object relatedId, QueryOptions options) {
            CriteriaQuery<Record> query =
                    createCriteriaQuery().joinById(rm.getTargetEntityName(), rm.getName(), "j", relatedId);

            JoinModels joinModels = new JoinModels("j", new ModelAndMapping(am, em));
            return doQueryListResult(query, joinModels, options, null, null);
        }

        public QueryListResult queryListByIds(Set<Object> ids, QueryOptions options) {
            String idFieldName = em.getKeyFieldNames()[0];
            if (remoteRest) {
                String filter = idFieldName + " in (" + joinInIds(new ArrayList<>(ids)) + ")";
                if (Strings.isEmpty(options.getFilters())) {
                    options.setFilters(filter);
                } else {
                    options.setFilters("(" + options.getFilters() + ") and (" + filter + ")");
                }
                RestResource                restResource = restResourceFactory.createResource(dao.getOrmContext(), em);
                RestQueryListResult<Record> result       = restResource.queryList(options);
                return new QueryListResult(result.getList(), result.getCount());
            } else {
                CriteriaQuery<Record> query =
                        createCriteriaQuery().where(idFieldName + " in ?", ids);

                return doQueryListResult(query, new JoinModels(), options, null, null);
            }
        }
    }
}
