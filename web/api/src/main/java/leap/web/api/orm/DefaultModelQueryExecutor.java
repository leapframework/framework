/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.web.api.orm;

import leap.core.value.Record;
import leap.core.value.SimpleRecord;
import leap.lang.*;
import leap.lang.collection.SimpleCaseInsensitiveMap;
import leap.lang.convert.Converts;
import leap.lang.jdbc.SimpleWhereBuilder;
import leap.lang.jdbc.WhereBuilder;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.text.scel.ScelExpr;
import leap.lang.text.scel.ScelName;
import leap.lang.text.scel.ScelNode;
import leap.lang.text.scel.ScelToken;
import leap.orm.event.EntityListeners;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.mapping.RelationMapping;
import leap.orm.mapping.RelationProperty;
import leap.orm.query.CriteriaQuery;
import leap.orm.query.PageResult;
import leap.web.Params;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.meta.model.MApiProperty;
import leap.web.api.mvc.params.CountOptions;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.query.*;
import leap.web.api.remote.RestQueryListResult;
import leap.web.api.remote.RestResource;
import leap.web.exception.BadRequestException;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DefaultModelQueryExecutor extends ModelExecutorBase implements ModelQueryExecutor {

    private static final Log log = LogFactory.get(DefaultModelQueryExecutor.class);

    protected final ModelAndMapping     modelAndMapping;
    protected final ModelQueryExtension ex;

    protected FindHandler     findHandler;
    protected EntityListeners listeners;
    protected String          sqlView;
    protected String[]        excludedFields;
    protected boolean         filterByParams = true;

    public DefaultModelQueryExecutor(ModelExecutorContext context) {
        this(context, null);
    }

    public DefaultModelQueryExecutor(ModelExecutorContext context, ModelQueryExtension ex) {
        super(context);
        this.modelAndMapping = new ModelAndMapping(am, em);
        this.ex = null == ex ? ModelQueryExtension.EMPTY : ex;
    }

    @Override
    public ModelQueryExecutor withFindHandler(FindHandler handler) {
        this.findHandler = handler;
        return this;
    }

    @Override
    public ModelQueryExecutor withListeners(EntityListeners listeners) {
        this.listeners = listeners;
        return this;
    }

    @Override
    public ModelQueryExecutor fromSqlView(String sql) {
        this.sqlView = sql;
        return this;
    }

    @Override
    public ModelQueryExecutor selectExclude(String... names) {
        this.excludedFields = names;
        return this;
    }

    @Override
    public ModelQueryExecutor setFilterByParams(boolean filterByParams) {
        this.filterByParams = filterByParams;
        return this;
    }

    @Override
    public QueryOneResult queryOne(Object id, QueryOptionsBase options) {
        if (remoteRest) {
            RestResource restResource = restResourceFactory.createResource(dao.getOrmContext(), em);
            Record       record       = restResource.find(id, options);
            return new QueryOneResult(record);
        }

        ModelExecutionContext context = new DefaultModelExecutionContext(this.context);

        return em.withContextListeners(listeners, () -> {
            ex.processQueryOneOptions(context, options); //for compatibility only.
            if (null != ex.handler) {
                ex.handler.processQueryOneOptions(context, id, options);
            }
            try {
                ex.preQueryOne(context);
                Record record;

                if (null != findHandler) {
                    record = queryOneByHandler(context, id, options);
                } else {
                    CriteriaQuery<Record> query = createCriteriaQuery().whereById(id);
                    applySelect(query, options, new JoinModels());

                    ex.preQueryOne(context, id, query);
                    if (null != ex.handler) {
                        ex.handler.preQueryOne(context, id, query);
                    }
                    record = dao.withEvents(() -> query.firstOrNull());
                }

                List<ExpandError> expandErrors = expandOne(context, record, options);

                if (null != ex.handler && null != record) {
                    ex.handler.postQueryOne(context, id, record);
                }

                Object entity = ex.processQueryOneRecord(context, id, record);

                QueryOneResult result = new QueryOneResult(record, entity, expandErrors);

                ex.completeQueryOne(context, result, null);

                return result;
            } catch (Throwable e) {
                ex.completeQueryOne(context, null, e);
                throw e;
            }
        });
    }

    protected Record queryOneByHandler(ModelExecutionContext context, Object id, QueryOptionsBase options) {
        Object result = findHandler.findOrNull(context, id, options);
        if (null == result) {
            return null;
        }
        if (result instanceof Record) {
            return (Record) result;
        }
        if (result instanceof Map) {
            return new SimpleRecord((Map) result);
        }
        Map map = Beans.toMap(result);
        return new SimpleRecord(map);
    }

    protected List<ExpandError> expandOne(ModelExecutionContext context, Record record, QueryOptionsBase options) {
        if (null != record && null != options) {
            Expand[] expands = options.getResolvedExpands();
            if (!Arrays2.isEmpty(expands)) {
                final List<ExpandError> expandErrors    = new ArrayList<>();
                final List<Record>      list            = Arrays.asList(record);
                final ResolvedExpand[]  resolvedExpands = resolveExpands(expands);
                for (ResolvedExpand expand : resolvedExpands) {
                    try {
                        expand(context, expand, list);
                    } catch (ExpandException e) {
                        expandErrors.add(new ExpandError(expand.name, e.getMessage(), e));
                    }
                }
                return expandErrors;
            }
        }
        return null;
    }

    @Override
    public QueryListResult queryList(QueryOptions options, Map<String, Object> filters, Consumer<CriteriaQuery> callback) {
        return queryList(options, filters, callback, filterByParams);
    }

    @Override
    public QueryListResult queryList(QueryOptions options, Map<String, Object> filters, Consumer<CriteriaQuery> callback, boolean filterByParams) {
        //todo: review query remote entity.
        if (remoteRest) {
            RestResource restResource = restResourceFactory.createResource(dao.getOrmContext(), em);

            RestQueryListResult result;
            if (filterByParams) {
                result = restResource.queryList(SimpleRecord.class, options, filters);
            } else {
                result = restResource.queryList(options);
            }

            return new QueryListResult(result.getList(), result.getCount());
        }

        CriteriaQuery<Record> query = createCriteriaQuery();
        return doQueryListResult(query, new JoinModels(), options, filters, callback, filterByParams);
    }

    protected QueryListResult doQueryListResult(CriteriaQuery<Record> query,
                                                JoinModels joinModels,
                                                QueryOptions options,
                                                Map<String, Object> filters,
                                                Consumer<CriteriaQuery> callback) {
        return doQueryListResult(query, joinModels, options, filters, callback, filterByParams);
    }

    protected QueryListResult doQueryListResult(CriteriaQuery<Record> query,
                                                JoinModels joinModels,
                                                QueryOptions options,
                                                Map<String, Object> filters,
                                                Consumer<CriteriaQuery> callback, boolean filterByParams) {
        // todo: remove to jmms
        options.setAllowSingleExpr(true);

        ModelExecutionContext context = new DefaultModelExecutionContext(this.context);
        if (null == options) {
            options = new QueryOptions();
        }

        ex.processQueryListOptions(context, options);
        if (null != ex.handler) {
            ex.handler.processQueryListOptions(context, options);
        }

        if (!Strings.isEmpty(options.getSqlView())) {
            query.fromSqlView(options.getSqlView());
        }

        Map<String, Object> allParams = new HashMap<>();
        if (null != this.context.getActionContext()) {
            allParams.putAll(this.context.getActionContext().getMergedParameters());
        }
        if (null != options.getQueryParams()) {
            allParams.putAll(options.getQueryParams());
        }
        query.params(allParams);

        Join[] joins = options.getResolvedJoins();
        if (null != joins && joins.length > 0) {
            Set<String> relations = new HashSet<>();

            for (Join join : joins) {

                if (relations.contains(join.getRelation().toLowerCase())) {
                    throw new BadRequestException("Duplicated join relation '" + join.getRelation() + "'");
                }

                if (joinModels.contains(join.getAlias())) {
                    throw new BadRequestException("Duplicated join alias '" + join.getAlias() + "'");
                }

                if (join.getAlias().equalsIgnoreCase(query.alias())) {
                    throw new BadRequestException("Alias '" + query.alias() + "' is reserved, please use another one");
                }

                RelationProperty rp = em.tryGetRelationProperty(join.getRelation());
                if (null == rp) {
                    throw new BadRequestException("No relation '" + join.getRelation() + "' in model '" + am.getName() +
                            " or the relation is not joinable");
                }

                if (rp.isOptional()) {
                    query.leftJoin(rp.getTargetEntityName(), rp.getRelationName(), join.getAlias());
                } else {
                    query.join(rp.getTargetEntityName(), rp.getRelationName(), join.getAlias());
                }

                relations.add(join.getRelation().toLowerCase());

                ModelAndMapping joinModel = lookupModelAndMapping(rp.getTargetEntityName());
                if (null == joinModel) {
                    throw new BadRequestException("The joined model '" + rp.getTargetEntityName() + "' of relation '" + join.getRelation() + "' not found");
                }
                joinModels.add(join.getAlias(), joinModel);
            }
        }

        applyOrderBy(query, options, joinModels);
        applySelectOrAggregates(query, options, joinModels);
        applyFilters(context, query, options.getParams(), options, joinModels, filters, filterByParams);

        if (callback != null) {
            callback.accept(query);
        }

        final QueryOptions finalOptions = options;
        return em.withContextListeners(listeners, () -> {
            long         count = -1;
            List<Record> list;

            try {
                ex.preQueryList(context, query);
                if (null != ex.handler) {
                    ex.handler.preQueryList(context, query);
                }

                PageResult page = query.pageResult(finalOptions.getPage(ac.getDefaultPageSize()));
                list = ex.executeQueryList(context, finalOptions, query);
                if (null == list) {
                    list = dao.withEvents(() -> page.list());
                }

                if (null != ex.handler) {
                    ex.handler.postQueryList(context, list);
                }

                List<ExpandError> expandErrors = new ArrayList<>();
                if (!list.isEmpty()) {
                    Expand[] expands = ExpandParser.parse(finalOptions.getExpand());
                    if (expands.length > 0) {
                        ResolvedExpand[] resolvedExpands = resolveExpands(expands);

                        int maxPageSize = ac.getMaxPageSizeWithExpandOne();
                        for (ResolvedExpand expand : resolvedExpands) {
                            if (expand.isEmbedded()) {
                                continue;
                            }
                            if (expand.rm.isOneToMany() || expand.rm.isOneToMany()) {
                                maxPageSize = ac.getMaxPageSizeWithExpandMany();
                                break;
                            }
                        }

                        if (list.size() > maxPageSize) {
                            throw new BadRequestException("The result size " + list.size() + " exceed max expand " + maxPageSize + ", please decrease your page_size");
                        }

                        for (ResolvedExpand expand : resolvedExpands) {
                            try {
                                expand(context, expand, list);
                            } catch (ExpandException e) {
                                expandErrors.add(new ExpandError(expand.getName(), e.getMessage(), e.getCause()));
                            }
                        }
                    }
                }

                if (finalOptions.isTotal()) {
                    count = query.count();
                }

                Object entity = ex.processQueryListResult(context, page, count, list);

                QueryListResult result = new QueryListResult(list, count, entity, expandErrors);

                ex.completeQueryList(context, result, null);

                return result;
            } catch (Throwable e) {
                ex.completeQueryList(context, null, e);
                throw e;
            }
        });
    }

    @Override
    public QueryListResult count(CountOptions options, Consumer<CriteriaQuery> callback) {
        if (remoteRest) {
            RestResource restResource = restResourceFactory.createResource(dao.getOrmContext(), em);
            return new QueryListResult(null, restResource.count(options), null);
        }

        ModelExecutionContext context = new DefaultModelExecutionContext(this.context);

        CriteriaQuery<Record> query = createCriteriaQuery();

        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setFilters(options.getFilters());

        applyFilters(context, query, null, queryOptions, null, null);
        applyCount(context, query);

        if (callback != null) {
            callback.accept(query);
        }

        long count = query.count();

        return new QueryListResult(null, count, null);
    }

    protected CriteriaQuery<Record> createCriteriaQuery() {
        return dao.createCriteriaQuery(em).fromSqlView(sqlView);
    }

    protected void expand(ModelExecutionContext context, ResolvedExpand expand, List<Record> records) {
        if (records == null || records.size() == 0) {
            return;
        }
        if (expand.isRemoteRest()) {
            expandByRest(context, expand, records);
        } else {
            expandByDb(context, expand, records);
        }
    }

    protected void expandByRest(ModelExecutionContext context, ResolvedExpand expand, List<Record> records) {
        if (expand.isEmbedded()) {
            expandByRestEmbedded(expand, records);
            return;
        }

        QueryOptions opts = new QueryOptions();
        opts.setLimit(ac.getMaxRecordsPerExpand() + 1);

        RestResource resource = restResourceFactory.createResource(dao.getOrmContext(), expand.tem);
        if (expand.tem.getRemoteSettings().isExpandCanNewAccessToken()) {
            resource.setCanNewAccessToken(true);
        }

        //根据不同类型的关系，计算引用的源字段、被引用字段
        String localFieldName;
        String referredFieldName;

        final RelationMapping rm = expand.rm;
        if (rm.isOneToMany()) {
            RelationMapping inverseRm = this.md.getEntityMapping(rm.getJoinEntityName()).getRelationMapping(rm.getInverseRelationName());
            localFieldName = inverseRm.getJoinFields()[0].getReferencedFieldName();
            referredFieldName = inverseRm.getJoinFields()[0].getLocalFieldName();
        } else if (rm.isManyToMany()) {
            throw new BadRequestException("Unsupported remote entity expand when relation type is many-to-many");
        } else {
            localFieldName = rm.getJoinFields()[0].getLocalFieldName();
            referredFieldName = rm.getJoinFields()[0].getReferencedFieldName();
        }

        //获取所有被引用记录的id
        Set<Object> fks = new HashSet<>();
        for (Record record : records) {
            Object fk = record.get(localFieldName);
            if (fk == null || fks.contains(fk)) {
                continue;
            }
            fks.add(fk);
        }

        StringBuilder filters = new StringBuilder();
        int           i       = 0;
        for (Object fk : fks) {
            if (i > 0) {
                filters.append(',');
            }
            filters.append(fk.toString());
            i++;
        }
        opts.setFilters(Strings.format("{0} in ({1})", referredFieldName, filters.toString()));

        //构造expand时，要返回引用记录的字段
        if (!Strings.isEmpty(expand.getSelect())) {
            if (expand.getSelect().contains(referredFieldName)) {
                opts.setSelect(expand.getSelect());
            } else {
                opts.setSelect(expand.getSelect() + "," + referredFieldName);
            }
        }

        if (!Strings.isEmpty(expand.getFilters())) {
            opts.setFilters(expand.getFilters());
        }

        if (!Strings.isEmpty(expand.getOrderBy())) {
            if (rm.isManyToOne()) {
                throw new BadRequestException("Many-to-one relation expand is meaningless, check '" + rm.getName() + "' relation");
            }

            opts.setOrderBy(expand.getOrderBy());
        }

        RestQueryListResult<Map> resultList;
        try {
            resultList = resource.queryList(Map.class, opts);
        } catch (Exception e) {
            log.error("Expand by rest error, {}", e.getMessage(), e);
            throw new ExpandException(e);
        }
        if (resultList.getCount() > ac.getMaxRecordsPerExpand()) {
            throw new BadRequestException("Expanded records of '" + expand.getName() + "' exceed max limit " + ac.getMaxRecordsPerExpand());
        }

        //根据引用字段值，对所有查询出来的被引用数据，进行分组
        Map<Object, List<Record>> referredRecords = new HashMap<>();
        for (Map<String, Object> referred : resultList.getList()) {
            Object       fkVal          = null;
            List<Record> fieldToValList = null;
            if (rm.isManyToMany()) {
                fkVal = referred.remove(referredFieldName);
                if (fkVal == null) {
                    fkVal = referred.remove(referredFieldName.toUpperCase());
                }
                if (fkVal == null) {
                    fkVal = referred.remove(referredFieldName.toLowerCase());
                }
            } else {
                fkVal = referred.get(referredFieldName);
            }

            if (referredRecords.containsKey(fkVal)) {
                fieldToValList = referredRecords.get(fkVal);
            } else {
                fieldToValList = new ArrayList<>();
                referredRecords.put(fkVal, fieldToValList);
            }
            fieldToValList.add(new SimpleRecord(referred));
        }

        //填充expand指定的属性
        final RelationProperty rp = expand.rp;
        for (Record record : records) {
            Object       fk             = record.get(localFieldName);
            List<Record> fieldToRecords = referredRecords.get(fk);
            if (rp.isMany()) {
                record.put(rp.getName(), null == fieldToRecords ? Collections.emptyList() : fieldToRecords);
            } else {
                if (fieldToRecords != null && fieldToRecords.size() > 0) {
                    record.put(rp.getName(), fieldToRecords.get(0));
                } else {
                    record.put(rp.getName(), null);
                }
            }
        }
    }

    protected void expandByDb(ModelExecutionContext context, ResolvedExpand expand, List<Record> records) {
        if (expand.isEmbedded()) {
            expandByDbEmbedded(expand, records);
            return;
        }

        ex.preExpand(context);
        try {
            final RelationProperty rp          = expand.rp;
            final RelationMapping  rm          = expand.rm;
            CriteriaQuery<Record>  expandQuery = dao.createCriteriaQuery(rp.getTargetEntityName()).limit(ac.getMaxRecordsPerExpand() + 1);

            //根据不同类型的关系，计算引用的源字段、被引用字段
            String localFieldName;
            String referredFieldName;

            if (rm.isOneToMany()) {
                RelationMapping inverseRm = this.md.getEntityMapping(rm.getTargetEntityName()).getRelationMapping(rm.getInverseRelationName());
                localFieldName = inverseRm.getJoinFields()[0].getReferencedFieldName();
                referredFieldName = inverseRm.getJoinFields()[0].getLocalFieldName();
            } else if (rm.isManyToMany()) {
                RelationMapping joinRm = this.md.getEntityMapping(rm.getJoinEntityName()).tryGetRelationMappingOfTargetEntity(em.getEntityName());
                localFieldName = em.getKeyFieldNames()[0];
                referredFieldName = joinRm.getJoinFields()[0].getLocalFieldName();
                expandQuery.join(rm.getJoinEntityName(), "_jt_");
            } else {
                localFieldName = rm.getJoinFields()[0].getLocalFieldName();
                referredFieldName = rm.getJoinFields()[0].getReferencedFieldName();
            }

            String referredFieldAlias = rm.getTargetEntityName() + "_" + referredFieldName;

            //获取所有被引用记录的id
            Set<Object> fks = new HashSet<>();
            for (Record record : records) {
                Object fk = record.get(localFieldName);
                if (fk == null || fks.contains(fk)) {
                    continue;
                }
                fks.add(fk);
            }

            if (rm.isManyToMany()) {
                expandQuery.where(Strings.format("_jt_.{0} in :fks", referredFieldName))
                        .param("fks", fks.toArray());
            } else {
                expandQuery.where(Strings.format("{0} in :fks", referredFieldName))
                        .param("fks", fks.toArray());
            }

            //构造expand时，要返回引用记录的字段
            if (Strings.isEmpty(expand.getSelect())) {
                if (rm.isManyToMany()) {
                    expandQuery.select("*", Strings.format("_jt_.{0} as {1}", referredFieldName, referredFieldAlias));
                } else {
                    referredFieldAlias = referredFieldName;
                }
            } else {
                if (rm.isManyToMany()) {
                    applySelect(expandQuery, expand, Strings.format("_jt_.{0} as {1}", referredFieldName, referredFieldAlias));
                } else {
                    applySelect(expandQuery, expand, referredFieldName);
                    referredFieldAlias = referredFieldName;
                }
            }

            applyExpand(rm, expandQuery, expand, false);

            ex.preExpand(context, expandQuery);
            List<Record> resultList = expandQuery.list();
            if (resultList.size() > ac.getMaxRecordsPerExpand()) {
                throw new BadRequestException("Expanded records of '" + rp.getName() + "' exceed max limit " + ac.getMaxRecordsPerExpand());
            }

            //根据引用字段值，对所有查询出来的被引用数据，进行分组
            Map<Object, List<Record>> referredRecords = new HashMap<>();
            for (Record referred : resultList) {

                Object       fkVal;
                List<Record> fieldToValList;

                if (rm.isManyToMany()) {
                    fkVal = referred.remove(referredFieldAlias);
                    if (fkVal == null) {
                        fkVal = referred.remove(referredFieldAlias.toUpperCase());
                    }
                    if (fkVal == null) {
                        fkVal = referred.remove(referredFieldAlias.toLowerCase());
                    }
                } else {
                    fkVal = referred.get(referredFieldAlias);
                }

                if (referredRecords.containsKey(fkVal)) {
                    fieldToValList = referredRecords.get(fkVal);
                } else {
                    fieldToValList = new ArrayList<>();
                    referredRecords.put(fkVal, fieldToValList);
                }
                fieldToValList.add(referred);
            }

            //填充expand指定的属性
            for (Record record : records) {
                Object       fk             = record.get(localFieldName);
                List<Record> fieldToRecords = referredRecords.get(fk);
                if (rp.isMany()) {
                    record.put(rp.getName(), null == fieldToRecords ? Collections.emptyList() : fieldToRecords);
                } else {
                    if (fieldToRecords != null && fieldToRecords.size() > 0) {
                        record.put(rp.getName(), fieldToRecords.get(0));
                    } else {
                        record.put(rp.getName(), null);
                    }
                }
            }
        } finally {
            ex.completeExpand(context);
        }
    }

    protected void expandByRestEmbedded(ResolvedExpand expand, List<Record> records) {
        final RelationMapping  rm = expand.rm;
        final RelationProperty rp = expand.rp;

        Set<Object> ids = new HashSet<>();
        records.forEach(r -> calcIdsByEmbeddedField(ids, r, rm.getEmbeddedFileName()));
        if (ids.isEmpty()) {
            return;
        }

        EntityMapping targetEm    = md.getEntityMapping(rm.getTargetEntityName());
        String        idFieldName = targetEm.getKeyFieldNames()[0];

        RestResource restResource = restResourceFactory.createResource(dao.getOrmContext(), targetEm);
        if (targetEm.getRemoteSettings().isExpandCanNewAccessToken()) {
            restResource.setCanNewAccessToken(true);
        }

        List<Map> totalExpanded = new ArrayList<>();
        for (List<Object> partOfIds : split(ids, 50)) {
            String       filter  = idFieldName + " in (" + joinInIds(partOfIds) + ")";
            QueryOptions options = new QueryOptions();
            options.setFilters(filter);

            RestQueryListResult<Map> listResult;
            try {
                listResult = restResource.queryList(Map.class, options);
            } catch (Exception e) {
                log.error("Expand by reset error, {}", e.getMessage(), e);
                throw new ExpandException(e);
            }
            totalExpanded.addAll(listResult.getList());
        }

        Map<Object, Map> totalExpandedMap =
                totalExpanded.stream().collect(Collectors.toMap((r) -> r.get(idFieldName), r -> r));

        for (Record record : records) {
            Object embeddedIds  = record.get(rm.getEmbeddedFileName());
            List   expandedList = new ArrayList();
            if (null != embeddedIds) {
                for (Object embeddedId : Enumerables.of(embeddedIds)) {
                    Map expandedRecord = totalExpandedMap.get(embeddedId);
                    if (null != expandedRecord) {
                        expandedList.add(expandedRecord);
                    }
                }
            }
            record.put(rp.getName(), expandedList);
        }
    }

    protected String joinInIds(List<Object> ids) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                s.append(',');
            }
            s.append("'");
            s.append(ids.get(i));
            s.append("'");
        }
        return s.toString();
    }

    protected List<List<Object>> split(Set<Object> set, int num) {
        List<List<Object>> list = new ArrayList<>();

        int          j        = 0;
        List<Object> itemList = new ArrayList<>();
        list.add(itemList);
        for (Object item : set) {
            if (j == num) {
                j = 0;
                itemList = new ArrayList<>();
                list.add(itemList);
            } else {
                j++;
            }
            itemList.add(item);
        }

        return list;
    }

    protected void expandByDbEmbedded(ResolvedExpand expand, List<Record> records) {
        final RelationMapping rm = expand.rm;

        //calc target ids
        Set<Object> ids = new HashSet<>();
        records.forEach(r -> calcIdsByEmbeddedField(ids, r, rm.getEmbeddedFileName()));
        if (ids.isEmpty()) {
            return;
        }

        //expand by target ids
        EntityMapping targetEm    = md.getEntityMapping(rm.getTargetEntityName());
        String        idFieldName = targetEm.getKeyFieldNames()[0];

        CriteriaQuery<Record> expandQuery =
                dao.createCriteriaQuery(targetEm).where(idFieldName + " in ?", ids);

        applyExpand(rm, expandQuery, expand, true);

        List<Record> totalExpanded = expandQuery.limit(ac.getMaxRecordsPerExpand() + 1).list();
        if (totalExpanded.size() > ac.getMaxRecordsPerExpand()) {
            throw new BadRequestException("Expanded records of '" + expand.getName() + "' exceed max limit " + ac.getMaxRecordsPerExpand());
        }
        Map<Object, Record> totalExpandedMap =
                totalExpanded.stream().collect(Collectors.toMap((r) -> r.get(idFieldName), r -> r));

        for (Record record : records) {
            Object embeddedIds  = record.get(rm.getEmbeddedFileName());
            List   expandedList = new ArrayList();
            if (null != embeddedIds) {
                for (Object embeddedId : Enumerables.of(embeddedIds)) {
                    Record expandedRecord = totalExpandedMap.get(embeddedId);
                    if (null != expandedRecord) {
                        expandedList.add(expandedRecord);
                    }
                }
            }
            record.put(expand.rp.getName(), expandedList);
        }
    }

    public void calcIdsByEmbeddedField(Set<Object> ids, Record record, String embeddedFieldName) {
        Object embeddedIds = record.get(embeddedFieldName);
        if (null != embeddedIds) {
            Enumerable<Object> enumerable = Enumerables.tryOf(embeddedIds);
            if (null == enumerable) {
                throw new BadRequestException("The embedded ids must be array at field '" + embeddedFieldName + "'");
            }
            for (Object item : enumerable) {
                if (!ids.contains(item)) {
                    ids.add(item);
                }
            }
        }
    }

    @Deprecated
    protected void expand(Record record, Object id, ResolvedExpand expand) {
        String name = expand.getName();

        MApiProperty ap = am.tryGetProperty(name);
        if (null == ap) {
            throw new BadRequestException("The expand property '" + name + "' not exists!");
        }

        //todo : check expandable?

        RelationProperty rp = em.tryGetRelationProperty(name);
        if (null == rp) {
            throw new BadRequestException("Property '" + name + "' cannot be expanded");
        }

        RelationMapping rm = em.getRelationMapping(rp.getRelationName());

        CriteriaQuery expandQuery =
                dao.createCriteriaQuery(rp.getTargetEntityName())
                        .joinById(em.getEntityName(), rm.getInverseRelationName(), "t_" + em.getEntityName(), id);

        applyExpand(rm, expandQuery, expand, true);

        if (rp.isMany()) {
            //todo : limit
            record.put(rp.getName(), expandQuery.list());
        } else {
            record.put(rp.getName(), expandQuery.firstOrNull());
        }
    }

    protected void applyExpand(RelationMapping rm, CriteriaQuery query, ResolvedExpand expand, Boolean applySelect) {
        if (applySelect && !Strings.isEmpty(expand.getSelect())) {
            applySelect(query, expand, null);
        }

        if (!Strings.isEmpty(expand.getFilters())) {
            applyFilters(query, expand);
        }

        if (!Strings.isEmpty(expand.getOrderBy())) {
            if (rm.isManyToOne()) {
                throw new BadRequestException("Many-to-one relation expand is meaningless, check '" + rm.getName() + "' relation");
            }
            applyOrderBy(query, expand);
        }
    }

    protected void applyOrderBy(CriteriaQuery query, QueryOptions options, JoinModels joinModels) {
        OrderBy orderBy = options.getResolvedOrderBy();

        if (null == orderBy) {
            return;
        }

        OrderBy.Item[] items = orderBy.items();

        StringBuilder s = new StringBuilder();

        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                s.append(',');
            }

            OrderBy.Item item = items[i];

            String expr = em.getOrderByExprs().get(item.name());
            if (null != expr) {
                s.append(expr);
            } else {
                MApiModel model;

                if (item.hasAlias() && !item.alias().equalsIgnoreCase(query.alias())) {
                    ModelAndMapping jm = joinModels.get(item.alias());
                    if (null == jm) {
                        throw new BadRequestException("Can't found join alias '" + item.alias() + "', check order by");
                    }
                    model = jm.model;
                } else {
                    model = am;
                }

                String       name    = item.name();
                MApiProperty ap      = model.tryGetProperty(name);
                boolean      isAlias = false;

                if (null == ap) {
                    if (null != options.getResolvedSelect() && options.getResolvedSelect().containAlias(name)) {
                        isAlias = true;
                    } else if (null != options.getResolvedGroupBy() && options.getResolvedGroupBy().aliasContain(name)) {
                        isAlias = true;
                    } else if (null != options.getResolvedAggregate() && options.getResolvedAggregate().aliasContain(name)) {
                        isAlias = true;
                    }
                    if (!isAlias) {
                        throw new BadRequestException("Property '" + name + "' not exists in model '" + model.getName() + "'");
                    }
                } else if (ap.isNotSortableExplicitly()) {
                    throw new BadRequestException("Property '" + name + "' is not sortable in model '" + model.getName() + "'");
                }

                if (isAlias) {
                    s.append(item.name());
                } else if (item.hasAlias()) {
                    s.append(item.alias()).append('.').append(item.name());
                } else if (Strings.isNotEmpty(query.alias())) {
                    s.append(query.alias()).append('.').append(item.name());
                } else {
                    s.append(item.name());
                }
            }

            if (!item.isAscending()) {
                s.append(" desc");
            }
        }

        query.orderBy(s.toString());
    }

    protected void applyOrderBy(CriteriaQuery query, ResolvedExpand expand) {
        OrderBy orderBy = expand.getResolvedOrderBy();

        if (null == orderBy) {
            return;
        }

        EntityMapping em = query.getEntityMapping();
        OrderBy.Item[] items = orderBy.items();
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                s.append(',');
            }

            OrderBy.Item item = items[i];

            String expr = em.getOrderByExprs().get(item.name());
            if (null != expr) {
                s.append(expr);
            } else {
                MApiModel    model      = amd.getModel(em.getEntityName());
                String       name    = item.name();
                MApiProperty ap      = model.tryGetProperty(name);
                boolean      isAlias = false;

                if (null == ap) {
                    if (null != expand.getResolvedSelect() && expand.getResolvedSelect().containAlias(name)) {
                        isAlias = true;
                    }
                    if (!isAlias) {
                        throw new BadRequestException("Expand property '" + name + "' not exists in model '" + model.getName() + "'");
                    }
                } else if (ap.isNotSortableExplicitly()) {
                    throw new BadRequestException("Expand property '" + name + "' is not sortable in model '" + model.getName() + "'");
                }

                if (isAlias) {
                    s.append(item.name());
                } else if (item.hasAlias()) {
                    s.append(item.alias()).append('.').append(item.name());
                } else if (Strings.isNotEmpty(query.alias())) {
                    s.append(query.alias()).append('.').append(item.name());
                } else {
                    s.append(item.name());
                }
            }

            if (!item.isAscending()) {
                s.append(" desc");
            }
        }
        query.orderBy(s.toString());
    }

    protected void applySelect(CriteriaQuery query, ResolvedExpand expand, String... requiredFields) {
        if (Strings.equals("*", expand.getSelect())) {
            return;
        }

        Select select = expand.getResolvedSelect();
        if (null == select) {
            return;
        }

        EntityMapping em = query.getEntityMapping();
        MApiModel am = amd.getModel(em.getEntityName());
        Select.Item[] items = select.items();
        List<String> fields = new ArrayList<>();

        for (Select.Item item : items) {
            String expr = em.getSelectExprs().get(item.name());

            if (null != expr) {
                if (Strings.isEmpty(item.alias())) {
                    fields.add("(" + expr + ") as " + item.name());
                } else {
                    fields.add("(" + expr + ") as" + item.alias());
                }
            } else {
                MApiProperty p = am.tryGetProperty(item.name());

                if (null == p) {
                    throw new BadRequestException("Expand property '" + item.name() + "' not exists, check the 'select' query param");
                }
                if (!p.isSelectableExplicitly()) {
                    throw new BadRequestException("Expand property '" + item.name() + "' is not selectable, check the 'select' query param");
                }
                if (Strings.isEmpty(item.alias())) {
                    fields.add(p.getName());
                } else {
                    fields.add(p.getName() + " as " + item.alias());
                }
            }
        }

        if (null != excludedFields) {
            for (String name : excludedFields) {
                fields.remove(name);
            }
        }
        if (requiredFields != null && requiredFields.length > 0) {
            for (String requiredField : requiredFields) {
                boolean isContain = false;
                for (String field : fields) {
                    if (Strings.equalsIgnoreCase(requiredField, field)) {
                        isContain = true;
                        break;
                    }
                }
                if (isContain) {
                    continue;
                }
                fields.add(requiredField);
            }
        }

        query.select(fields.toArray(new String[fields.size()]));
    }

    protected void applySelect(CriteriaQuery query, QueryOptionsBase options, JoinModels joins) {
        List<String> fields = new ArrayList<>();
        applySelectItems(options, joins, fields);
        query.select(fields.toArray(new String[fields.size()]));
    }

    protected void applySelectItems(QueryOptionsBase options, JoinModels joins, List<String> items) {
        String select = null == options ? null : options.getSelect();

        if (Strings.isEmpty(select) || "*".equals(select)) {
            for (MApiProperty p : am.getProperties()) {
                if (p.isReference()) {
                    continue;
                }
                if (p.isSelectableExplicitly()) {
                    items.add(p.getName());
                }
            }
        } else {
            Select.Item[] selectItems;
            if (options instanceof QueryOptions) {
                selectItems = ((QueryOptions) options).getResolvedSelect().items();
            } else {
                selectItems = SelectParser.parse(options.getSelect()).items();
            }
            for (Select.Item selectItem : selectItems) {
                if (Strings.isEmpty(selectItem.joinAlias())) {
                    if (selectItem.name().equals("*")) {
                        for (MApiProperty p : am.getProperties()) {
                            if (p.isReference()) {
                                continue;
                            }
                            if (p.isSelectableExplicitly()) {
                                items.add(p.getName());
                            }
                        }
                        continue;
                    } else {
                        String expr = em.getSelectExprs().get(selectItem.name());
                        if (null != expr) {
                            if (Strings.isEmpty(selectItem.alias())) {
                                items.add("(" + expr + ") as " + selectItem.name());
                            } else {
                                items.add("(" + expr + ") as " + selectItem.alias());
                            }
                        } else {
                            MApiProperty p = am.tryGetProperty(selectItem.name());
                            if (null == p) {
                                throw new BadRequestException("Property '" + selectItem.name() + "' not exists, check the 'select' query param");
                            }
                            if (!p.isSelectableExplicitly()) {
                                throw new BadRequestException("Property '" + selectItem.name() + "' is not selectable");
                            }
                            if (Strings.isEmpty(selectItem.alias())) {
                                items.add(p.getName());
                            } else {
                                items.add(p.getName() + " as " + selectItem.alias());
                            }
                        }
                    }
                } else if (Strings.isNotEmpty(selectItem.joinAlias())) {
                    ModelAndMapping join = joins.get(selectItem.joinAlias());
                    if (null == join) {
                        throw new BadRequestException("The join alias '" + selectItem.joinAlias() + "' not exists, check '" + selectItem.joinAlias() + "." + selectItem.name() + "'");
                    }
                    MApiProperty p = join.model.tryGetProperty(selectItem.name());
                    if (null == p) {
                        throw new BadRequestException("Join property '" + selectItem.joinAlias() + "." + selectItem.name() + "' not exists, check the 'select' query param");
                    }
                    if (!p.isSelectableExplicitly()) {
                        throw new BadRequestException("Join Property '" + selectItem.joinAlias() + "." + selectItem.name() + "' is not selectable");
                    }
                    FieldMapping fm = join.mapping.getFieldMapping(p.getName());

                    if (Strings.isEmpty(selectItem.alias())) {
                        items.add(selectItem.joinAlias() + "." + fm.getColumnName());
                    } else {
                        items.add(selectItem.joinAlias() + "." + fm.getColumnName() + " " + selectItem.alias());
                    }
                }
            }
        }

        if (null != excludedFields && excludedFields.length > 0) {
            for (String name : excludedFields) {
                items.remove(name);
            }
        }
    }

    protected void applySelectOrAggregates(CriteriaQuery query, QueryOptions options, JoinModels joins) {
        if (Strings.isEmpty(options.getAggregates()) && Strings.isEmpty(options.getGroupBy())) {
            applySelect(query, options, joins);
            return;
        }

        if (!Strings.isEmpty(options.getSelect())) {
            throw new BadRequestException("Can't use 'select' for aggregation or groupby query");
        }

        List<String> select = new ArrayList<>();

        if (!Strings.isEmpty(options.getGroupBy())) {
            if (Strings.isEmpty(options.getAggregates())) {
                throw new BadRequestException("Must use groupby with aggregates");
            }

            GroupBy.Item[] items   = options.getResolvedGroupBy().items();
            StringBuilder  groupBy = new StringBuilder();

            for (int i = 0; i < items.length; i++) {
                if (i > 0) {
                    groupBy.append(',');
                }

                GroupBy.Item item = items[i];

                String expr = em.getGroupByExprs().get(item.name());
                if (null != expr) {
                    if (Strings.isEmpty(item.alias())) {
                        select.add("(" + expr + ") as " + item.name());
                    } else {
                        select.add("(" + expr + ") as " + item.alias());
                    }
                    groupBy.append("(" + expr + ")");
                } else {
                    MApiModel m;

                    if (Strings.isNotEmpty(item.joinAlias())) {
                        ModelAndMapping join = joins.get(item.joinAlias());
                        if (null == join) {
                            throw new BadRequestException("Can't found join alias '" + item.joinAlias() + "', check group by");
                        }
                        m = join.model;
                    } else {
                        m = am;
                    }

                    MApiProperty p = m.tryGetProperty(item.name());
                    if (null == p) {
                        throw new BadRequestException("Property '" + m.getName() + "." + item.name() + "' not exists, check the 'groupby'");
                    }
                    if (!p.isSelectableExplicitly()) {
                        throw new BadRequestException("Property '" + m.getName() + "." + item.name() + "' is not groupable");
                    }

                    StringBuffer sql = new StringBuffer();
                    if (null != item.joinAlias()) {
                        sql.append(item.joinAlias() + "." + p.getName());
                    } else {
                        sql.append(p.getName());
                    }
                    if (null != item.alias()) {
                        sql.append(" as " + item.alias());
                        groupBy.append(item.alias());
                    } else {
                        groupBy.append(sql);
                    }
                    select.add(sql.toString());
                }
            }
            query.groupBy(groupBy.toString());
        }

        for (Aggregate.Item item : options.getResolvedAggregate().items()) {
            String expr = em.getAggregatesExprs().get(item.name());
            if (Strings.isEmpty(expr)) {
                if (Strings.isEmpty(item.function())) {
                    throw new BadRequestException("Not find aggregation function in '" + item.name() + "'");
                }
                if (item.name().equals("*")) {
                    select.add(item.function() + "(" + item.name() + ") as " + item.alias());
                } else {
                    MApiProperty p = am.tryGetProperty(item.name());
                    if (null == p) {
                        throw new BadRequestException("Property '" + item.name() + "' not exists, check the 'aggregates' param");
                    }
                    if (!p.isAggregatableExplicitly()) {
                        throw new BadRequestException("Property '" + item.name() + "' is not aggregatable");
                    }
                    select.add(item.function() + "(" + query.alias() + "." + item.name() + ") as " + item.alias());
                }
            } else {
                select.add(expr + " as " + item.name());
            }
        }

        query.select(select.toArray(new String[select.size()]));
    }

    protected void applyCount(ModelExecutionContext context, CriteriaQuery query) {
        if (null != ex.handler) {
            ex.handler.preCount(context, query);
        }
        ex.preCount(context, query);
    }


    protected void applyFilters(ModelExecutionContext context, CriteriaQuery query, Params params,
                                QueryOptions options, JoinModels jms, Map<String, Object> fields) {
        applyFilters(context, query, params, options, jms, fields, filterByParams);
    }

    protected void applyFilters(ModelExecutionContext context, CriteriaQuery query, Params params,
                                QueryOptions options, JoinModels jms, Map<String, Object> fields, boolean filterByParams) {
        SimpleWhereBuilder where = new SimpleWhereBuilder();

        if (null != ex.handler) {
            ex.handler.preProcessQueryListWhere(context, options, where);
        }
        ex.preProcessQueryListWhere(context, options, where);

        //view
        if (!Strings.isEmpty(options.getViewId()) && null == ex.handler) {
            throw new BadRequestException("'viewId' not supported");
        }
        if (null != ex.handler) {
            ex.handler.handleQueryListView(context, options.getViewId(), where);
        }

        //fields
        if (null != fields && !fields.isEmpty()) {
            where.and((expr) -> {
                int i = 0;
                for (Map.Entry<String, Object> entry : fields.entrySet()) {
                    if (i > 0) {
                        expr.append(" and ");
                    }
                    i++;
                    if (null != entry.getValue() && entry.getValue().getClass().isArray()) {
                        expr.append(query.alias()).append('.').append(entry.getKey()).append(" in ?");
                    } else {
                        expr.append(query.alias()).append('.').append(entry.getKey()).append(" = ?");
                    }
                    expr.arg(entry.getValue());
                }
            });
        }

        if (null != params && filterByParams) {
            where.and((expr) -> {
                for (String name : params.names()) {

                    String alias;
                    int    dotIndex = name.indexOf('.');
                    if (dotIndex > 0) {
                        alias = name.substring(0, dotIndex);
                        name = name.substring(dotIndex + 1);

                        if (null == jms || !jms.contains(alias.toLowerCase())) {
                            throw new BadRequestException("Unknown alias '" + alias + "' at param '" + (alias + "." + name) + "'");
                        }
                    } else {
                        alias = query.alias();
                    }

                    ModelAndProp modelAndProp = lookupModelAndProp(jms, alias, name);
                    if (null == modelAndProp.property) {
                        continue;
                    }

                    checkProperty(modelAndProp, name);

                    String value = params.get(name);
                    if (Strings.isEmpty(value)) {
                        continue;
                    }

                    //                    if (!whereArgs.isEmpty()) {
                    //                        whereExpr.append(" and ");
                    //                    }
                    //
                    String[] a = params.getArray(name);
                    if (a.length == 1) {
                        a = Strings.split(a[0], ',');
                    }

                    if (a.length > 1) {
                        applyFieldFilterIn(expr, alias, modelAndProp.field, a);
                    } else {
                        applyFieldFilter(expr, alias, modelAndProp.field, value, "=");
                    }
                }
            });
        }

        //filters
        ScelExpr filters = options.getResolvedFilters();
        if (null != filters) {
            ScelNode[] nodes = filters.nodes();
            if (nodes.length > 0) {
                where.and((expr) -> {
                    for (int i = 0; i < nodes.length; i++) {
                        ScelNode node = nodes[i];

                        if (node.isParen()) {
                            expr.append(node.literal());
                            continue;
                        }

                        if (node.isAnd()) {
                            expr.append(" and ");
                            continue;
                        }

                        if (node.isOr()) {
                            expr.append(" or ");
                            continue;
                        }

                        ScelName nameNode = (ScelName) nodes[i];


                        String name        = nameNode.literal();
                        String filtersExpr = em.getFiltersExprs().get(name);
                        if (!Strings.isEmpty(filtersExpr)) {
                            expr.append(filtersExpr);
                        } else {
                            String    alias = nameNode.alias();
                            ScelToken op    = nodes[++i].token();
                            String    value = nodes[++i].literal();

                            if (null == op && Strings.isEmpty(value)) {
                                throw new BadRequestException("Invalid filter expr in '" + name + "'");
                            }

                            if (null != alias) {
                                if (null == jms || !jms.contains(alias)) {
                                    throw new BadRequestException("Unknown alias '" + alias + "' at property '" + nameNode.toString() + "'");
                                }
                            } else {
                                alias = query.alias();
                            }

                            ModelAndProp modelAndProp = lookupModelAndProp(jms, alias, name);
                            checkProperty(modelAndProp, name);

                            String sqlOperator = toSqlOperator(op);

                            if (op == ScelToken.IS || op == ScelToken.IS_NOT) {
                                expr.append(alias).append('.').append(name).append(' ').append(sqlOperator);
                                continue;
                            }

                            if (op == ScelToken.SW) {
                                value = "%" + value;
                            } else if (op == ScelToken.EW) {
                                value = value + "%";
                            } else if (op == ScelToken.CO) {
                                value = "%" + value + "%";
                            }

                            //env
                            if (op == ScelToken.IN || op == ScelToken.NOT_IN) {
                                applyFieldFilterIn(expr, alias, modelAndProp.field, nodes[i].values(), sqlOperator);
                            } else if (value.endsWith("()") && value.length() > 2) {
                                String envName = value.substring(0, value.length() - 2);
                                //todo: check env is valid or allowed?
                                String valueExpr = "#{env." + envName + "}";
                                applyFieldFilterExpr(expr, alias, modelAndProp.field, valueExpr, sqlOperator);
                            } else if (value.startsWith("env.")) {
                                //todo: check env is valid or allowed?
                                String valueExpr = "#{" + value + "}";
                                applyFieldFilterExpr(expr, alias, modelAndProp.field, valueExpr, sqlOperator);
                            } else {
                                applyFieldFilter(expr, alias, modelAndProp.field, value, sqlOperator);
                            }
                        }
                    }
                });
            }
        }

        if (null != ex.handler) {
            ex.handler.postProcessQueryListWhere(context, options, where);
        }
        ex.postProcessQueryListWhere(context, options, where);

        if (!where.isEmpty()) {
            query.where(where.getWhere().toString(), where.getArgs().toArray());
        }
    }

    protected void applyFilters(CriteriaQuery query, ResolvedExpand expand) {
        ScelExpr filters = expand.getResolvedFilters();

        if (null == filters) {
            return;
        }

        EntityMapping em = query.getEntityMapping();
        ScelNode[] nodes = filters.nodes();
        SimpleWhereBuilder where = new SimpleWhereBuilder();

        if (nodes.length > 0) {
            where.and((expr) -> {
                for (int i = 0; i < nodes.length; i++) {
                    ScelNode node = nodes[i];

                    if (node.isParen()) {
                        expr.append(node.literal());
                        continue;
                    }

                    if (node.isAnd()) {
                        expr.append(" and ");
                        continue;
                    }

                    if (node.isOr()) {
                        expr.append(" or ");
                        continue;
                    }

                    ScelName nameNode = (ScelName) nodes[i];

                    String name        = nameNode.literal();
                    String filtersExpr = em.getFiltersExprs().get(name);
                    if (!Strings.isEmpty(filtersExpr)) {
                        expr.append(filtersExpr);
                    } else {
                        FieldMapping field = em.getFieldMapping(name);
                        String    alias = nameNode.alias();
                        ScelToken op    = nodes[++i].token();
                        String    value = nodes[++i].literal();

                        if (null == op && Strings.isEmpty(value)) {
                            throw new BadRequestException("Invalid filter expr in '" + name + "'");
                        }

                        if (null != alias) {
                            throw new BadRequestException("Unknown alias '" + alias + "' at property '" + nameNode.toString() + "'");
                        } else {
                            alias = query.alias();
                        }

                        String sqlOperator = toSqlOperator(op);

                        if (op == ScelToken.IS || op == ScelToken.IS_NOT) {
                            expr.append(alias).append('.').append(name).append(' ').append(sqlOperator);
                            continue;
                        }

                        if (op == ScelToken.SW) {
                            value = "%" + value;
                        } else if (op == ScelToken.EW) {
                            value = value + "%";
                        } else if (op == ScelToken.CO) {
                            value = "%" + value + "%";
                        }

                        //env
                        if (op == ScelToken.IN || op == ScelToken.NOT_IN) {
                            applyFieldFilterIn(expr, alias, field, nodes[i].values(), sqlOperator);
                        } else if (value.endsWith("()") && value.length() > 2) {
                            String envName = value.substring(0, value.length() - 2);
                            //todo: check env is valid or allowed?
                            String valueExpr = "#{env." + envName + "}";
                            applyFieldFilterExpr(expr, alias, field, valueExpr, sqlOperator);
                        } else if (value.startsWith("env.")) {
                            //todo: check env is valid or allowed?
                            String valueExpr = "#{" + value + "}";
                            applyFieldFilterExpr(expr, alias, field, valueExpr, sqlOperator);
                        } else {
                            applyFieldFilter(expr, alias, field, value, sqlOperator);
                        }
                    }
                }
            });
        }

        if (!where.isEmpty()) {
            query.where(where.getWhere().toString(), where.getArgs().toArray());
        }
    }

    protected void checkProperty(ModelAndProp modelAndProp, String name) {
        boolean joined = modelAndProp.model != this.am;

        String modelDesc = (joined ? "joined " : "") + "model '" + modelAndProp.model.getName() + "'";

        if (null == modelAndProp.property) {
            throw new BadRequestException("Property '" + name + "' not exists in " + modelDesc);
        }

        if (null == modelAndProp.field) {
            throw new BadRequestException("No mapping field '" + name + "' in " + modelDesc);
        }

        MApiProperty ap = modelAndProp.property;

        if (ap.isNotFilterableExplicitly()) {
            throw new BadRequestException("Property '" + name + "' is not filterable in " + modelDesc);
        }

        if (ap.isReference()) {
            throw new BadRequestException("Relation Property '" + name + "' is not filterable in " + modelDesc);
        }
    }

    protected ModelAndMapping lookupModelAndMapping(String entityName) {
        MApiModel model = amd.getModel(entityName);
        if (null == model) {
            return null;
        }

        EntityMapping mapping = md.getEntityMapping(entityName);
        if (null == mapping) {
            throw new IllegalStateException("Entity mapping '" + entityName + "' should be exists!");
        }

        return new ModelAndMapping(model, mapping);
    }

    protected ModelAndProp lookupModelAndProp(JoinModels joinedModels, String alias, String propertyName) {
        ModelAndMapping modelAndMapping = null;
        if (null != joinedModels) {
            modelAndMapping = joinedModels.get(alias.toLowerCase());
        }

        if (null == modelAndMapping) {
            modelAndMapping = this.modelAndMapping;
        }

        MApiProperty property = modelAndMapping.model.tryGetProperty(propertyName);
        FieldMapping field    = null == property ? null : modelAndMapping.mapping.tryGetFieldMapping(property.getName());

        return new ModelAndProp(modelAndMapping, property, field);
    }

    protected void applyFieldFilter(WhereBuilder.Expr expr, String alias, FieldMapping fm, Object value, String op) {
        expr.append(alias).append('.').append(fm.getFieldName()).append(' ').append(op).append(" ?");
        expr.arg(Converts.convert(value, fm.getJavaType()));
    }

    protected void applyFieldFilterExpr(WhereBuilder.Expr expr, String alias, FieldMapping fm, String filterExpr, String op) {
        expr.append(alias).append('.').append(fm.getFieldName()).append(' ').append(op).append(" ").append(filterExpr);
    }

    protected void applyFieldFilterIn(WhereBuilder.Expr expr, String alias, FieldMapping fm, String[] values) {
        expr.append(alias).append('.').append(fm.getFieldName()).append(' ').append("in").append(" ?");
        expr.arg(Converts.convert(values, Array.newInstance(((FieldMapping) fm).getJavaType(), 0).getClass()));
    }

    protected void applyFieldFilterIn(WhereBuilder.Expr expr, String alias, FieldMapping fm, List<ScelNode> values, String sqlOperator) {
        expr.append(alias).append('.').append(fm.getFieldName()).append(' ').append(sqlOperator).append(" ?");

        final Class<?> type = ((FieldMapping) fm).getJavaType();

        Object[] args = new Object[values.size()];
        for (int i = 0; i < args.length; i++) {
            ScelNode value = values.get(i);
            if (ScelToken.NULL == value.token()) {
                args[i] = null;
            } else {
                args[i] = Converts.convert(value.literal(), type);
            }
        }

        expr.arg(args);
    }

    protected String toSqlOperator(ScelToken op) {

        if (op == ScelToken.EQ) {
            return "=";
        }

        if (op == ScelToken.GE) {
            return ">=";
        }

        if (op == ScelToken.LE) {
            return "<=";
        }

        if (op == ScelToken.GT) {
            return ">";
        }

        if (op == ScelToken.LT) {
            return "<";
        }

        if (op == ScelToken.NE) {
            return "<>";
        }

        if (op == ScelToken.NOT) {
            return "not";
        }

        if (op == ScelToken.IN) {
            return "in";
        }

        if (op == ScelToken.NOT_IN) {
            return "not in";
        }

        if (op == ScelToken.LIKE || op == ScelToken.CO || op == ScelToken.SW || op == ScelToken.EW) {
            return "like";
        }

        if (op == ScelToken.IS) {
            return "is null";
        }

        if (op == ScelToken.IS_NOT || op == ScelToken.PR) {
            return "is not null";
        }

        throw new IllegalStateException("Not supported operator '" + op + "'");
    }

    protected ResolvedExpand[] resolveExpands(Expand[] expands) {
        if (null == expands) {
            return null;
        }

        ResolvedExpand[] resolvedExpands = new ResolvedExpand[expands.length];

        for (int i = 0; i < expands.length; i++) {
            Expand expand = expands[i];

            String       name = expand.getName();
            MApiProperty ap   = am.tryGetProperty(name);
            if (null == ap) {
                throw new BadRequestException("The expand property '" + name + "' not exists!");
            }

            RelationProperty rp = em.tryGetRelationProperty(name);
            if (null == rp) {
                throw new BadRequestException("Property '" + name + "' cannot be expanded");
            }
            RelationMapping rm = em.getRelationMapping(rp.getRelationName());

            EntityMapping tem = dao.getOrmContext().getMetadata().tryGetEntityMapping(rp.getTargetEntityName());
            if (tem == null) {
                throw new IllegalStateException("Can't find target entity '" + rp.getTargetEntityName() + "'");
            }

            resolvedExpands[i] = new ResolvedExpand(expand, rp, rm, tem);
        }

        return resolvedExpands;
    }

    protected static class JoinModels {
        private final Map<String, ModelAndMapping> m = new SimpleCaseInsensitiveMap<>();

        public JoinModels() {

        }

        public JoinModels(String alias, ModelAndMapping join) {
            add(alias, join);
        }

        public void add(String alias, ModelAndMapping join) {
            m.put(alias, join);
        }

        public ModelAndMapping get(String alias) {
            return m.get(alias);
        }

        public boolean isEmpty() {
            return !m.isEmpty();
        }

        public boolean contains(String alias) {
            return m.containsKey(alias);
        }
    }

    protected static class ModelAndMapping {

        public final MApiModel     model;
        public final EntityMapping mapping;

        public ModelAndMapping(MApiModel model, EntityMapping mapping) {
            this.model = model;
            this.mapping = mapping;
        }
    }

    protected static class ModelAndProp extends ModelAndMapping {
        final MApiProperty property;
        final FieldMapping field;

        public ModelAndProp(ModelAndMapping mm, MApiProperty property, FieldMapping field) {
            super(mm.model, mm.mapping);
            this.property = property;
            this.field = field;
        }
    }

    protected static class ResolvedExpand {
        protected final String           name;
        protected final RelationProperty rp;
        protected final RelationMapping  rm;
        protected final EntityMapping    tem;

        protected String select;
        protected String filters;
        protected String orderBy;

        protected Select   resolvedSelect;
        protected ScelExpr resolvedFilters;
        protected OrderBy  resolvedOrderBy;

        public ResolvedExpand(Expand expand, RelationProperty rp, RelationMapping rm, EntityMapping tem) {
            this.name = expand.getName();
            this.select = expand.getSelect();
            this.filters = expand.getFilters();
            this.orderBy = expand.getOrderBy();
            this.rp = rp;
            this.rm = rm;
            this.tem = tem;
        }

        public String getName() {
            return name;
        }

        public String getSelect() {
            return select;
        }

        public void setSelect(String select) {
            this.select = select;
        }

        public String getFilters() {
            return filters;
        }

        public void setFilters(String filters) {
            this.filters = filters;
        }

        public String getOrderBy() {
            return orderBy;
        }

        public void setOrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public boolean isRemoteRest() {
            return tem.isRemoteRest();
        }

        public boolean isEmbedded() {
            return rm.isEmbedded();
        }

        public Select getResolvedSelect() {
            if (null == resolvedSelect && Strings.isNotEmpty(select)) {
                resolvedSelect = SelectParser.parse(select);
            }
            return resolvedSelect;
        }

        public void setResolvedSelect(Select resolvedSelect) {
            this.resolvedSelect = resolvedSelect;
        }

        public ScelExpr getResolvedFilters() {
            if (null == resolvedFilters && !Strings.isEmpty(filters)) {
                try {
                    resolvedFilters = FiltersParser.parse(filters, true);
                } catch (Exception e) {
                    throw new BadRequestException("Invalid filter expr '" + filters + "', " + e.getMessage(), e);
                }
            }
            return resolvedFilters;
        }

        public void setResolvedFilters(ScelExpr resolvedFilters) {
            this.resolvedFilters = resolvedFilters;
        }

        public OrderBy getResolvedOrderBy() {
            if (null == resolvedOrderBy && !Strings.isEmpty(orderBy)) {
                resolvedOrderBy = OrderByParser.parse(orderBy);
            }
            return resolvedOrderBy;
        }

        public void setResolvedOrderBy(OrderBy resolvedOrderBy) {
            this.resolvedOrderBy = resolvedOrderBy;
        }

    }
}
