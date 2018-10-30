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
import leap.lang.Strings;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.Mappings;
import leap.orm.mapping.RelationMapping;
import leap.orm.mapping.RelationType;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.remote.RestQueryListResult;
import leap.web.api.remote.RestResource;
import leap.web.exception.BadRequestException;

import java.util.Arrays;

public class DefaultRelationQueryExecutor extends ModelExecutorBase implements RelationQueryExecutor {

    protected final ModelQueryExecutor tqe;
    protected final EntityMapping      tem;
    protected final RelationMapping    rm;
    protected final String             rp;
    protected final RelationMapping    irm;

    public DefaultRelationQueryExecutor(RelationExecutorContext context, ModelQueryExecutor targetQueryExecutor) {
        super(context);
        this.tqe = targetQueryExecutor;
        this.tem = context.getTargetEntityMapping();
        this.rm  = context.getRelation();
        this.rp  = !Strings.isEmpty(context.getRelationPath()) ? context.getRelationPath() : Strings.lowerUnderscore(rm.getName());
        this.irm = context.getInverseRelation();
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

        return queryOneAllLocals(id, options);
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

        return tqe.queryOne(targetId, options);
    }

    protected QueryOneResult queryOneAllLocals(Object id, QueryOptionsBase options) {
        //TODO: optimize use local join.
        return queryOneRemoteTarget(id, options);
    }

    @Override
    public QueryListResult queryList(Object id, QueryOptions options) {
        if (remoteRest) {
            return queryListRemoteSource(id, options);
        }
        return null;
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

    protected QueryListResult queryListAllLocals(Object id, QueryOptions options) {
        return null;
    }
}
