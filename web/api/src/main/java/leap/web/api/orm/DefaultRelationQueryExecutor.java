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
import leap.orm.mapping.Mappings;
import leap.orm.mapping.RelationMapping;
import leap.orm.mapping.RelationType;
import leap.orm.query.CriteriaQuery;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.remote.RestQueryListResult;
import leap.web.api.remote.RestResource;
import leap.web.exception.BadRequestException;

import java.util.Arrays;
import java.util.Map;

public class DefaultRelationQueryExecutor extends ModelExecutorBase implements RelationQueryExecutor {

    protected final EntityMapping        tem;
    protected final RelationMapping      rm;
    protected final String               rp;
    protected final RelationMapping      irm;
    protected final InverseQueryExecutor iqe;

    public DefaultRelationQueryExecutor(RelationExecutorContext context) {
        super(context);
        this.tem = context.getInverseEntityMapping();
        this.rm  = context.getRelation();
        this.rp  = !Strings.isEmpty(context.getRelationPath()) ? context.getRelationPath() : Strings.lowerUnderscore(rm.getName());
        this.irm = context.getInverseRelation();
        this.iqe = new InverseQueryExecutor(context.newInverseExecutorContext(), irm);
    }

    @Override
    public QueryOneResult queryOne(Object id, QueryOptionsBase options) {
        if (!rm.isManyToOne()) {
            throw new IllegalStateException("Relation '" + rm.getName() + "' must be " + RelationType.MANY_TO_ONE + "' for query one");
        }

        if (remoteRest) {
            return queryOneRemoteSource(id, options);
        }

        if (tem.isRemoteRest()) {
            return queryOneRemoteTarget(id, options);
        }

        return iqe.queryOneByRelation(id, options);
    }

    protected QueryOneResult queryOneRemoteSource(Object id, QueryOptionsBase options) {
        RestResource restResource = restResourceFactory.createResource(dao.getOrmContext(), em);

        Record record = restResource.findRelationOne(rp, id, options);

        return new QueryOneResult(record);
    }

    protected QueryOneResult queryOneRemoteTarget(Object id, QueryOptionsBase options) {
        String[] fields = Arrays.stream(rm.getJoinFields())
                .map(joinField -> joinField.getLocalFieldName())
                .toArray(String[]::new);

        Record record = dao.createCriteriaQuery(em).select(fields).whereById(id).firstOrNull();
        if (null == record) {
            throw new BadRequestException("Record " + em.getEntityName() + "(" + id + ") not found");
        }

        Object targetId = Mappings.getId(tem, record);
        if (null == targetId) {
            return null;
        }

        return iqe.queryOne(targetId, options);
    }

    @Override
    public QueryListResult queryList(Object id, QueryOptions options) {
        if (remoteRest) {
            return queryListRemoteSource(id, options);
        }

        if(tem.isRemoteRest()) {
            return queryListRemoteTarget(id, options);
        }

        return iqe.queryListByRelation(id, options);
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

    protected static class InverseQueryExecutor extends DefaultModelQueryExecutor{
        private final RelationMapping rm;

        public InverseQueryExecutor(ModelExecutorContext context, RelationMapping rm) {
            super(context);
            this.rm = rm;
        }

        public QueryOneResult queryOneByRelation(Object relatedId, QueryOptionsBase options) {
            CriteriaQuery<Record> query =
                    createCriteriaQuery().joinById(em.getEntityName(), rm.getName(), "j", relatedId);

            Map<String, ModelAndMapping> joinedModels = New.hashMap("j", new ModelAndMapping(am, em));

            applySelect(query, options, joinedModels);

            Record record = query.firstOrNull();

            expandOne(record, options);

            return new QueryOneResult(record);
        }

        public QueryListResult queryListByRelation(Object relatedId, QueryOptions options) {
            CriteriaQuery<Record> query =
                    createCriteriaQuery().joinById(em.getEntityName(), rm.getName(), "j", relatedId);

            Map<String, ModelAndMapping> joinedModels = New.hashMap("j", new ModelAndMapping(am, em));

            return doQueryListResult(query, joinedModels, options, null, null);
        }
    }
}
