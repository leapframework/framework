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
import leap.lang.Arrays2;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.orm.enums.RemoteType;
import leap.orm.mapping.*;
import leap.orm.query.CriteriaQuery;
import leap.orm.query.PageResult;
import leap.web.Params;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.meta.model.MApiProperty;
import leap.web.api.mvc.params.CountOptions;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.query.*;
import leap.web.exception.BadRequestException;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;

public class DefaultModelQueryExecutor extends ModelExecutorBase implements ModelQueryExecutor {

    protected final ModelAndMapping modelAndMapping;

    protected String[] excludedFields;

    public DefaultModelQueryExecutor(ModelExecutorContext context) {
        super(context);
        this.modelAndMapping = new ModelAndMapping(am, em);
    }

    @Override
    public ModelQueryExecutor selectExclude(String... names) {
        this.excludedFields = names;
        return this;
    }

    @Override
    public QueryOneResult queryOne(Object id, QueryOptionsBase options) {
        Record record;

        CriteriaQuery<Record> query = dao.createCriteriaQuery(em).whereById(id);
        if(null != options && !Strings.isEmpty(options.getSelect())) {
            applySelect(query, options.getSelect());
        }else{
            query.selectExclude(excludedFields);
        }

        record = query.firstOrNull();

        if(null != record && null != options) {
            Expand[] expands = ExpandParser.parse(options.getExpand());
            if (expands.length > 0) {
                for (Expand expand : expands) {
                    expand(expand,record);
                }
            }
        }

        return new QueryOneResult(record);
    }

    @Override
    public QueryListResult queryList(QueryOptions options, Map<String, Object> filters){
        return queryList(options,filters,null);
    }

    @Override
    public QueryListResult queryList(QueryOptions options, Map<String, Object> filters, Consumer<CriteriaQuery> callback) {
        //todo : validates the query options.

        CriteriaQuery<Record> query = dao.createCriteriaQuery(em);

        long count = -1;
        List<Record> list;
        if(null == options) {

            if(callback != null){
                callback.accept(query);
            }

            query.selectExclude(excludedFields);

            list = query.limit(ac.getMaxPageSize()).list();

        }else{
            if(!Strings.isEmpty(options.getOrderBy())) {
                applyOrderBy(query, options.getOrderBy());
            }

            if(!Strings.isEmpty(options.getSelect())) {
                applySelect(query, options.getSelect());
            }else{
                query.selectExclude(excludedFields);
            }

            Map<String, ModelAndMapping> joinedModels = null;

            if(!Strings.isEmpty(options.getJoins())) {
                Join[] joins = JoinParser.parse(options.getJoins());

                Set<String> relations = new HashSet<>();

                joinedModels = new HashMap<>(joins.length);

                for(Join join : joins) {

                    if(relations.contains(join.getRelation().toLowerCase())) {
                        throw new BadRequestException("Duplicated join relation '" + join.getRelation() + "'");
                    }

                    if(joinedModels.containsKey(join.getAlias().toLowerCase())) {
                        throw new BadRequestException("Duplicated join alias '" + join.getAlias() + "'");
                    }

                    if(join.getAlias().equalsIgnoreCase(query.alias())) {
                        throw new BadRequestException("Alias '" + query.alias() + "' is reserved, please use another one");
                    }

                    RelationProperty rp = em.tryGetRelationProperty(join.getRelation());
                    if(null == rp) {
                        throw new BadRequestException("No relation '" + join.getRelation() + "' in model '" + am.getName());
                    }

                    if(rp.isOptional()) {
                        query.leftJoin(rp.getTargetEntityName(), rp.getRelationName(), join.getAlias());
                    }else{
                        query.join(rp.getTargetEntityName(), rp.getRelationName(), join.getAlias());
                    }

                    relations.add(join.getRelation().toLowerCase());

                    ModelAndMapping joinedModel = lookupModelAndMapping(rp.getTargetEntityName());
                    if(null == joinedModel) {
                        throw new BadRequestException("The joined model '" + rp.getTargetEntityName() + "' of relation '" + join.getRelation() + "' not found");
                    }
                    joinedModels.put(join.getAlias().toLowerCase(), joinedModel);
                }
            }

            applyFilters(query, options.getParams(), options.getFilters(), joinedModels, filters);

            if(callback != null){
                callback.accept(query);
            }

            PageResult result = query.pageResult(options.getPage(ac.getDefaultPageSize()));

            list = result.list();

            if(!list.isEmpty()) {
                Expand[] expands = ExpandParser.parse(options.getExpand());
                if(expands.length > 0) {
                	for(Expand expand : expands) {
                        expand( expand,list);
                    }
                }
            }
        }

        if(options.isTotal()){
            count = query.count();
        }

        return new QueryListResult(list, count);
    }

    @Override
    public QueryListResult count(CountOptions options, Consumer<CriteriaQuery> callback) {

        CriteriaQuery<Record> query = dao.createCriteriaQuery(em);

        applyFilters(query, null, options.getFilters(), null, null);

        if(callback != null){
            callback.accept(query);
        }

        long count = query.count();

        return new QueryListResult(null, count);
    }

    protected void expand(Expand expand, List<Record> records) {
    	if(records==null || records.size()==0){
    		return;
    	}

    	String name = expand.getName();
        MApiProperty ap = am.tryGetProperty(name);
        if(null == ap) {
            throw new BadRequestException("The expand property '" + name + "' not exists!");
        }
        RelationProperty rp = em.tryGetRelationProperty(name);
        if(null == rp) {
            throw new BadRequestException("Property '" + name + "' cannot be expanded");
        }
        EntityMapping targetEm=dao.getOrmContext().getMetadata().tryGetEntityMapping(rp.getTargetEntityName());
        if(targetEm==null){
        	throw new BadRequestException("can't find relation entity:"+rp.getTargetEntityName());
        }
        if(RemoteType.rest.equals(targetEm.getRemoteType())){
        	expandByRest(expand, records, rp);
        }else{
        	expandByDb(expand, records, rp);
        }

    }

    private void expandByRest(Expand expand, List<Record> records, RelationProperty rp){

    }

	private void expandByDb(Expand expand, List<Record> records, RelationProperty rp) {
		RelationMapping rm = em.getRelationMapping(rp.getRelationName());
		CriteriaQuery<Record> expandQuery =dao.createCriteriaQuery(rp.getTargetEntityName());

        //根据不同类型的关系，计算引用的源字段、被引用字段
        String localFieldName="",referredFieldName="";
        if(rm.isOneToMany()){
        	RelationMapping inverseRm= this.md.getEntityMapping(rm.getJoinEntityName()).getRelationMapping(rm.getInverseRelationName());
        	localFieldName=inverseRm.getJoinFields()[0].getReferencedFieldName();
        	referredFieldName=inverseRm.getJoinFields()[0].getLocalFieldName();
        }else if(rm.isManyToMany()){
        	RelationMapping joinRm= this.md.getEntityMapping(rm.getJoinEntityName()).tryGetRelationMappingOfTargetEntity(em.getEntityName());
        	localFieldName=em.getKeyFieldNames()[0];
        	referredFieldName=joinRm.getJoinFields()[0].getLocalFieldName();
        	expandQuery.join(rm.getJoinEntityName(), "_jt_");
        }else{
            localFieldName= rm.getJoinFields()[0].getLocalFieldName();
            referredFieldName=rm.getJoinFields()[0].getReferencedFieldName();
        }

        //获取所有被引用记录的id
        Set<Object> fks=new HashSet<>();
        for (Record record : records) {
    		Object fk = record.get(localFieldName);
    		if(fk==null ||fks.contains(fk)){
    			continue;
    		}
    		fks.add(fk);
		}

        if(rm.isManyToMany()){
        	expandQuery.where(Strings.format("_jt_.{0} in :fks", referredFieldName))
            .param("fks", fks.toArray());
        }else{
        	expandQuery.where(Strings.format("{0} in :fks", referredFieldName))
            .param("fks", fks.toArray());
        }

        //构造expand时，要返回引用记录的字段
        if(Strings.isEmpty(expand.getSelect())) {
        	 if(rm.isManyToMany()){
        		 expandQuery.select("*",Strings.format("_jt_.{0} as {0}",referredFieldName));
        	 }
        }else{
        	if(rm.isManyToMany()){
       		 	applySelect(expandQuery, expand.getSelect(),Strings.format("_jt_.{0} as {0}",referredFieldName));
	       	 }else{
	       		 applySelect(expandQuery, expand.getSelect(),referredFieldName);
	       	 }
        }
        List<Record> resultList= expandQuery.list();

        //根据引用字段值，对所有查询出来的被引用数据，进行分组
        Map<Object,List<Record>> referredRecords=new HashMap<>();
        for (Record referred :resultList ) {
        	Object fkVal=null;
        	List<Record> fieldToValList=null;
        	if(rm.isManyToMany()){
        		fkVal=referred.remove(referredFieldName);
        		if(fkVal==null){
        			fkVal=referred.remove(referredFieldName.toUpperCase());
        		}
        		if(fkVal==null){
        			fkVal=referred.remove(referredFieldName.toLowerCase());
        		}
        	}else{
        		fkVal=referred.get(referredFieldName);
        	}

			if(referredRecords.containsKey(fkVal)){
				fieldToValList=referredRecords.get(fkVal);
			}else{
				fieldToValList=new ArrayList<>();
				referredRecords.put(fkVal, fieldToValList);
			}
			fieldToValList.add(referred);
		}

        //填充expand指定的属性
        for (Record record : records) {
        	Object fk = record.get(localFieldName);
        	List<Record> fieldToRecords=referredRecords.get(fk);
        	if(rp.isMany()) {
                record.put(rp.getName(),fieldToRecords);
            }else{
            	if(fieldToRecords!=null && fieldToRecords.size()>0){
            		record.put(rp.getName(),fieldToRecords.get(0));
            	}else{
            		record.put(rp.getName(), null);
            	}
            }
		}
	}

    /**
     * @see DefaultModelQueryExecutor#expand(Expand expand, Record... records)
     * @param record
     * @param id
     * @param expand
     */
    @Deprecated
    protected void expand(Record record, Object id, Expand expand) {
        String name = expand.getName();

        MApiProperty ap = am.tryGetProperty(name);
        if(null == ap) {
            throw new BadRequestException("The expand property '" + name + "' not exists!");
        }

        //todo : check expandable?

        RelationProperty rp = em.tryGetRelationProperty(name);
        if(null == rp) {
            throw new BadRequestException("Property '" + name + "' cannot be expanded");
        }

        RelationMapping rm = em.getRelationMapping(rp.getRelationName());

        CriteriaQuery expandQuery =
                dao.createCriteriaQuery(rp.getTargetEntityName())
                        .joinById(em.getEntityName(), rm.getInverseRelationName(), "t_" + em.getEntityName(), id);


        if(!Strings.isEmpty(expand.getSelect())) {
            applySelect(expandQuery, expand.getSelect());
        }

        if(rp.isMany()) {
            //todo : limit
            record.put(rp.getName(), expandQuery.list());
        }else{
            record.put(rp.getName(), expandQuery.firstOrNull());
        }
    }

    protected void expand(Expand expand, Record... records) {
    	expand(expand, Arrays.asList(records));
    }

    protected void applyOrderBy(CriteriaQuery query, String expr) {
        OrderBy orderBy = OrderByParser.parse(expr);

        OrderBy.Item[] items = orderBy.items();

        StringBuilder s = new StringBuilder();

        for(int i=0;i<items.length;i++) {

            if(i > 0) {
                s.append(',');
            }

            OrderBy.Item item = items[i];

            String name = item.name();

            MApiProperty ap = am.tryGetProperty(name);
            if(null == ap) {
                throw new BadRequestException("Property '" + name + "' not exists in model '" + am.getName() + "'");
            }

            if(ap.isNotSortableExplicitly()) {
                throw new BadRequestException("Property '" + name + "' is not sortable!");
            }
            if(Strings.isNotEmpty(query.alias())){
                s.append(query.alias() + "."+name);
            }else{
                s.append(name);
            }

            if(!item.isAscending()) {
                s.append(" desc");
            }
        }

        query.orderBy(s.toString());
    }

    /**
     * 设置查询字段
     * @param query 查询语句
     * @param select 待查询字段
     * @param requiredFields select中必须包含的字段，如果不包含，则自动添加该字段
     */
    protected void applySelect(CriteriaQuery query, String select,String... requiredFields) {
        if(Strings.equals("*", select)) {
            return;
        }

        EntityMapping em = query.getEntityMapping();

        String[] names = Strings.split(select, ',');

        List<String> fields = new ArrayList<>();

        for(String name : names) {

            FieldMapping p = em.tryGetFieldMapping(name);

            if(null == p) {
                throw new BadRequestException("Property '" + name + "' not exists, check the 'select' query param");
            }

            fields.add(p.getFieldName());
        }

        if(null != excludedFields) {
            for(String name : excludedFields) {
                fields.remove(name);
            }
        }
        if(requiredFields!=null && requiredFields.length>0){
        	for (String requiredField : requiredFields) {
				boolean isContain=false;
				for (String field : fields) {
					if(Strings.equalsIgnoreCase(requiredField, field)){
						isContain=true;
						break;
					}
				}
				if(isContain){
					continue;
				}
				fields.add(requiredField);
			}
        }
        query.select(fields.toArray(new String[fields.size()]));
    }

    protected void applySelect(CriteriaQuery query, String select) {
        if(Strings.equals("*", select)) {
            return;
        }

        EntityMapping em = query.getEntityMapping();

        String[] names = Strings.split(select, ',');

        List<String> fields = new ArrayList<>();

        for(String name : names) {

            FieldMapping p = em.tryGetFieldMapping(name);

            if(null == p) {
                throw new BadRequestException("Property '" + name + "' not exists, check the 'select' query param");
            }

            fields.add(p.getFieldName());
        }

        if(null != excludedFields) {
            for(String name : excludedFields) {
                fields.remove(name);
            }
        }

        query.select(fields.toArray(new String[fields.size()]));
    }

    protected void applyFilters(CriteriaQuery query, Params params, String expr, Map<String, ModelAndMapping> jms, Map<String, Object> fields) {
        StringBuilder where = new StringBuilder();
        List<Object> args = new ArrayList<>();

        //fields
        if(null != fields && !fields.isEmpty()) {
            int i=0;
            for(Map.Entry<String,Object> entry : fields.entrySet()) {
                if(i > 0) {
                    where.append(" and ");
                }
                i++;
                if(null != entry.getValue()&&entry.getValue().getClass().isArray()){
                    where.append(query.alias()).append('.').append(entry.getKey()).append(" in ?");
                }else {
                    where.append(query.alias()).append('.').append(entry.getKey()).append(" = ?");
                }
                args.add(entry.getValue());
            }
        }

        if(null != params) {
            for (String name : params.names()) {

                String alias;
                int dotIndex = name.indexOf('.');
                if(dotIndex > 0) {
                    alias = name.substring(0, dotIndex);
                    name = name.substring(dotIndex + 1);

                    if(null == jms || !jms.containsKey(alias.toLowerCase())) {
                        throw new BadRequestException("Unknown alias '" + alias + "' at param '" + (alias + "." + name) + "'");
                    }
                }else{
                    alias = query.alias();
                }

                ModelAndProp modelAndProp = lookupModelAndProp(jms, alias, name);
                if(null == modelAndProp.property) {
                    continue;
                }

                checkProperty(modelAndProp, name);

                String value = params.get(name);
                if (Strings.isEmpty(value)) {
                    continue;
                }

                if (!args.isEmpty()) {
                    where.append(" and ");
                }

                String[] a = params.getArray(name);
                if (a.length == 1) {
                    a = Strings.split(a[0], ',');
                }

                if (a.length > 1) {
                    applyFieldFilterIn(where, args, alias, modelAndProp.field, a);
                } else {
                    applyFieldFilter(where, args, alias, modelAndProp.field, value, "=");
                }

            }
        }

        //expr
        if(!Strings.isEmpty(expr)) {
            Filters filters = FiltersParser.parse(expr);

            FiltersParser.Node[] nodes = filters.nodes();
            if(nodes.length > 0) {

                boolean and = !args.isEmpty();
                if(and) {
                    where.append(" and (");
                }

                for(int i=0;i<nodes.length;i++) {
                    FiltersParser.Node node = nodes[i];

                    if(node.isParen()) {
                        where.append(node.literal());
                        continue;
                    }

                    if(node.isAnd()) {
                        where.append(" and ");
                        continue;
                    }

                    if(node.isOr()) {
                        where.append(" or ");
                        continue;
                    }

                    FiltersParser.Name nameNode = (FiltersParser.Name)nodes[i];

                    String alias = nameNode.alias();
                    String name  = nameNode.literal();
                    FiltersParser.Token op = nodes[++i].token();
                    String value = nodes[++i].literal();

                    if(null != alias) {
                        if(null == jms || !jms.containsKey(alias.toLowerCase())) {
                            throw new BadRequestException("Unknown alias '" + alias + "' at property '" + nameNode.toString() + "'");
                        }
                    }else{
                        alias = query.alias();
                    }

                    ModelAndProp modelAndProp = lookupModelAndProp(jms, alias, name);
                    checkProperty(modelAndProp, name);

                    String sqlOperator = toSqlOperator(op);

                    if(op == FiltersParser.Token.IS || op == FiltersParser.Token.NOT){
                        where.append(alias).append('.').append(name).append(' ').append(sqlOperator);
                        continue;
                    }

                    if(op == FiltersParser.Token.IN) {
                        applyFieldFilterIn(where, args, alias, modelAndProp.field, Strings.split(value, ','));
                    }else{
                        applyFieldFilter(where, args, alias, modelAndProp.field, value, sqlOperator);
                    }
                }

                if(and) {
                    where.append(")");
                }
            }
        }

        if(where.length() > 0) {
            query.where(where.toString(), args.toArray());
        }
    }

    protected void checkProperty(ModelAndProp modelAndProp, String name) {
        boolean joined = modelAndProp.model != this.am;

        String modelDesc = (joined ? "joined " : "") + "model '" + modelAndProp.model.getName() + "'";

        if(null == modelAndProp.property) {
            throw new BadRequestException("Property '" + name + "' not exists in " + modelDesc);
        }

        if(null == modelAndProp.field) {
            throw new BadRequestException("No mapping field '" + name + "' in " + modelDesc);
        }

        MApiProperty ap = modelAndProp.property;

        if(ap.isNotFilterableExplicitly()) {
            throw new BadRequestException("Property '" + name + "' is not filterable in " + modelDesc);
        }

        if(ap.isReference()) {
            throw new BadRequestException("Relation Property '" + name + "' is not filterable in " + modelDesc);
        }
    }

    protected ModelAndMapping lookupModelAndMapping(String entityName) {
        MApiModel model = amd.getModel(entityName);
        if(null == model) {
            return null;
        }

        EntityMapping mapping = md.getEntityMapping(entityName);
        if(null == mapping) {
            throw new IllegalStateException("Entity mapping '" + entityName + "' should be exists!");
        }

        return new ModelAndMapping(model, mapping);
    }

    protected ModelAndProp lookupModelAndProp(Map<String, ModelAndMapping> joinedModels, String alias, String propertyName) {
        ModelAndMapping modelAndMapping = null;
        if(null != joinedModels) {
            modelAndMapping = joinedModels.get(alias.toLowerCase());
        }

        if(null == modelAndMapping) {
            modelAndMapping = this.modelAndMapping;
        }

        MApiProperty property = modelAndMapping.model.tryGetProperty(propertyName);
        FieldMapping field    = null == property ? null : modelAndMapping.mapping.tryGetFieldMapping(property.getName());

        return new ModelAndProp(modelAndMapping, property, field);
    }

    protected void applyFieldFilter(StringBuilder where, List<Object> args, String alias, FieldMapping fm, Object value, String op) {
        where.append(alias).append('.').append(fm.getFieldName()).append(' ').append(op).append(" ?");
        args.add(Converts.convert(value, fm.getJavaType()));
    }

    protected void applyFieldFilterIn(StringBuilder where, List<Object> args, String alias, FieldMapping fm, String[] values) {
        where.append(alias).append('.').append(fm.getFieldName()).append(' ').append("in").append(" ?");
        args.add(Converts.convert(values, Array.newInstance(((FieldMapping)fm).getJavaType(), 0).getClass()));
    }

    protected String toSqlOperator(FiltersParser.Token op) {

        if(op == FiltersParser.Token.EQ) {
            return "=";
        }

        if(op == FiltersParser.Token.GE) {
            return ">=";
        }

        if(op == FiltersParser.Token.LE) {
            return "<=";
        }

        if(op == FiltersParser.Token.GT) {
            return ">";
        }

        if(op == FiltersParser.Token.LT) {
            return "<";
        }

        if(op == FiltersParser.Token.NE) {
            return "<>";
        }

        if(op == FiltersParser.Token.IN) {
            return "in";
        }

        if(op == FiltersParser.Token.LIKE) {
            return "like";
        }

        if(op == FiltersParser.Token.IS){
            return "is null";
        }

        if(op == FiltersParser.Token.NOT){
            return "is not null";
        }
        throw new IllegalStateException("Not supported operator '" + op + "'");
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
            this.field    = field;
        }
    }

}
