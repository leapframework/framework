/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.orm.mapping;

import leap.db.model.DbSchemaObjectName;
import leap.db.model.DbTable;
import leap.db.model.DbTableBuilder;
import leap.lang.*;
import leap.lang.tostring.ToStringBuilder;
import leap.orm.domain.EntityDomain;
import leap.orm.interceptor.EntityExecutionInterceptor;
import leap.orm.model.Model;
import leap.orm.sharding.ShardingAlgorithm;
import leap.orm.validation.EntityValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntityMappingBuilder implements Buildable<EntityMapping> {
	 
	protected String					   entityName;
	protected Class<?>      	    	   entityClass;
	protected boolean					   _abstract;
	protected DbTableBuilder			   table;
	protected String					   tablePrefix;
	protected boolean					   tableNameDeclared;
	protected boolean					   idDeclared;
    protected boolean                      autoCreateTable;
	protected List<FieldMappingBuilder>    fieldMappings = new ArrayList<>();
	protected EntityExecutionInterceptor   insertInterceptor;
	protected EntityExecutionInterceptor   updateInterceptor;
	protected EntityExecutionInterceptor   deleteInterceptor;
	protected EntityExecutionInterceptor   findInterceptor;
	protected EntityDomain				   domain;
	protected Class<? extends Model>       modelClass;
	protected DbTable					   physicalTable;
	protected List<EntityValidator>        validators;
	protected List<RelationMappingBuilder> relationMappings = new ArrayList<>();
    protected List<RelationPropertyBuilder>relationProperties = new ArrayList<>();
    protected boolean                      sharding;
    protected boolean                      autoCreateShardingTable;
    protected ShardingAlgorithm            shardingAlgorithm;
	
	public Class<?> getSourceClass(){
		return null != entityClass ? entityClass : modelClass;
	}
	
	public Class<?> getEntityClass() {
		return entityClass;
	}

	@SuppressWarnings("unchecked")
    public EntityMappingBuilder setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
		
		if(null != entityClass && Model.class.isAssignableFrom(entityClass)){
			setModelClass((Class<? extends Model>)entityClass);
		}
		
		return this;
	}

	public String getEntityName() {
		return entityName;
	}

	public EntityMappingBuilder setEntityName(String entityName) {
		this.entityName = entityName;
		return this;
	}
	
	public boolean isAbstract(){
		return _abstract;
	}
	
	public EntityMappingBuilder setAbstract(boolean isAbstract){
		this._abstract = isAbstract;
		return this;
	}
	
	public DbTableBuilder getTable() {
		if(null == table){
			table = new DbTableBuilder();
		}
		return table;
	}

	public EntityMappingBuilder setTable(DbTableBuilder table) {
		this.table = table;
		return this;
	}

	public String getTableCatalog() {
		return getTable().getCatalog();
	}

	public EntityMappingBuilder setTableCatalog(String tableCatalog) {
		getTable().setCatalog(tableCatalog);
		return this;
	}

	public String getTableSchema() {
		return getTable().getSchema();
	}

	public EntityMappingBuilder setTableSchema(String tableSchema) {
		getTable().setSchema(tableSchema);
		return this;
	}
	
	public String getTablePrefix() {
		return tablePrefix;
	}

	public EntityMappingBuilder setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
		return this;
	}

	public String getTableName() {
		return getTable().getName();
	}
	
	public String getTableNameWithPrefix() {
		return Strings.concat(tablePrefix,getTableName());
	}

	public EntityMappingBuilder setTableName(String tableName) {
		getTable().setName(tableName);
		return this;
	}
	
	public boolean isTableNameDeclared() {
		return tableNameDeclared;
	}

	public EntityMappingBuilder setTableNameDeclared(boolean tableNameDeclared) {
		this.tableNameDeclared = tableNameDeclared;
		return this;
	}
	
	public boolean isIdDeclared() {
		return idDeclared;
	}

	public EntityMappingBuilder setIdDeclared(boolean idDeclared) {
		this.idDeclared = idDeclared;
		return this;
	}

    public boolean isAutoCreateTable() {
        return autoCreateTable;
    }

    public EntityMappingBuilder setAutoCreateTable(boolean autoCreateTable) {
        this.autoCreateTable = autoCreateTable;
        return this;
    }

    public List<FieldMappingBuilder> getFieldMappings() {
		return fieldMappings;
	}
	
	public List<FieldMappingBuilder> getKeyFieldMappings() {
		List<FieldMappingBuilder> keys = new ArrayList<FieldMappingBuilder>();
		for(FieldMappingBuilder fmb : getFieldMappings()){
			if(fmb.isId()){
				keys.add(fmb);
			}
		}
		return keys;
	}
	
	public FieldMappingBuilder findFieldMappingByName(String name){
		for(FieldMappingBuilder fmb : getFieldMappings()){
			if(Strings.equalsIgnoreCase(name, fmb.getFieldName())){
				return fmb;
			}
		}
		return null;
	}
	
	public FieldMappingBuilder findFieldMappingByColumn(String column){
		for(FieldMappingBuilder fmb : getFieldMappings()){
			if(Strings.equalsIgnoreCase(column, fmb.getColumn().getName())){
				return fmb;
			}
		}
		return null;
	}
	
	public FieldMappingBuilder findFieldMappingByMetaName(String name){
		for(FieldMappingBuilder fmb : getFieldMappings()){
			if(Strings.equalsIgnoreCase(name, fmb.getMetaFieldName())){
				return fmb;
			}
			if(null != fmb.getReservedMetaFieldName() && Strings.equalsIgnoreCase(name, fmb.getReservedMetaFieldName().getFieldName())) {
				return fmb;
			}
		}
		return null;
	}
	
	public EntityMappingBuilder addFieldMapping(FieldMappingBuilder fm){
		fieldMappings.add(fm);
		return this;
	}
	
	public EntityMappingBuilder addPrimaryKey(FieldMappingBuilder fm){
		fieldMappings.add(0,fm);
		return this;
	}
	
	public boolean hasPrimaryKey(){
		for(FieldMappingBuilder fm : this.fieldMappings){
			if(fm.isId()){
				return true;
			}
		}
		return false;
	}
	
	public List<FieldMappingBuilder> getIdFieldMappings(){
		List<FieldMappingBuilder> list = New.arrayList();
		for(FieldMappingBuilder fm : this.fieldMappings){
			if(fm.isId()){
				list.add(fm);
			}
		}
		return list;
	}

	public EntityExecutionInterceptor getInsertInterceptor() {
		return insertInterceptor;
	}

	public EntityMappingBuilder setInsertInterceptor(EntityExecutionInterceptor insertHandler) {
		this.insertInterceptor = insertHandler;
		return this;
	}

	public EntityExecutionInterceptor getUpdateInterceptor() {
		return updateInterceptor;
	}

	public EntityMappingBuilder setUpdateInterceptor(EntityExecutionInterceptor updateHandler) {
		this.updateInterceptor = updateHandler;
		return this;
	}

	public EntityExecutionInterceptor getDeleteInterceptor() {
		return deleteInterceptor;
	}

	public EntityMappingBuilder setDeleteInterceptor(EntityExecutionInterceptor deleteHandler) {
		this.deleteInterceptor = deleteHandler;
		return this;
	}

	public EntityExecutionInterceptor getFindInterceptor() {
		return findInterceptor;
	}

	public EntityMappingBuilder setFindInterceptor(EntityExecutionInterceptor findHandler) {
		this.findInterceptor = findHandler;
		return this;
	}

	public EntityDomain getDomain() {
		return domain;
	}

	public EntityMappingBuilder setDomain(EntityDomain domain) {
		this.domain = domain;
		return this;
	}
	
	public boolean isModel(){
		return null != modelClass; 
	}

	public Class<? extends Model> getModelClass() {
		return modelClass;
	}

	public EntityMappingBuilder setModelClass(Class<? extends Model> modelClass) {
		this.modelClass = modelClass;
		return this;
	}
	
	public DbTable getPhysicalTable() {
		return physicalTable;
	}

	public EntityMappingBuilder setPhysicalTable(DbTable physicalTable) {
		this.physicalTable = physicalTable;
		return this;
	}
	
	public List<EntityValidator> getValidators() {
		if(null == validators){
			validators = new ArrayList<EntityValidator>();
		}
		return validators;
	}

	public EntityMappingBuilder setValidators(List<EntityValidator> validators) {
		this.validators = validators;
		return this;
	}
	
	public EntityMappingBuilder addValidator(EntityValidator validator){
		getValidators().add(validator);
		return this;
	}
	
	public List<RelationMappingBuilder> getRelationMappings() {
		return relationMappings;
	}

	public EntityMappingBuilder addRelationMapping(RelationMappingBuilder relationMapping){
		getRelationMappings().add(relationMapping);
		return this;
	}

    public List<RelationPropertyBuilder> getRelationProperties() {
        return relationProperties;
    }

    public EntityMappingBuilder addRelationProperty(RelationPropertyBuilder p) {
        relationProperties.add(p);
        return this;
    }

    public boolean isSharding() {
        return sharding;
    }

    public EntityMappingBuilder setSharding(boolean sharding) {
        this.sharding = sharding;
        return this;
    }

    public boolean isAutoCreateShardingTable() {
        return autoCreateShardingTable;
    }

    public EntityMappingBuilder setAutoCreateShardingTable(boolean autoCreateShardingTable) {
        this.autoCreateShardingTable = autoCreateShardingTable;
        return this;
    }

    public ShardingAlgorithm getShardingAlgorithm() {
        return shardingAlgorithm;
    }

    public EntityMappingBuilder setShardingAlgorithm(ShardingAlgorithm shardingAlgorithm) {
        this.shardingAlgorithm = shardingAlgorithm;
        return this;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[entity=" + entityName + "]";
    }

    @Override
    public EntityMapping build() {
		Collections.sort(fieldMappings, Comparators.ORDERED_COMPARATOR);
		
		List<FieldMapping>    fields    = Builders.buildList(fieldMappings);
		List<RelationMapping> relations = Builders.buildList(relationMappings);
		DbTable			      table     = buildTable(fields,relations);

	    return new EntityMapping(entityName,entityClass,table,fields,
	    						 insertInterceptor,updateInterceptor,deleteInterceptor,findInterceptor,
	    						 domain,modelClass,validators,
                                 relations,
                                 Builders.buildArray(relationProperties, new RelationProperty[0]),
                                 autoCreateTable,
                                 sharding, autoCreateShardingTable, shardingAlgorithm);
    }
	
	public DbSchemaObjectName getTableSchemaObjectName() {
		return new DbSchemaObjectName(getTableCatalog(),getTableSchema(),getTableNameWithPrefix());
	}
	
	protected DbTable buildTable(List<FieldMapping> fieldMappings,List<RelationMapping> relations){
		DbTableBuilder table = getTable();

		if(!Strings.isEmpty(tablePrefix)){
			table.setName(getTableNameWithPrefix());
		}
	
		for(FieldMapping fm : fieldMappings){
			table.addColumn(fm.getColumn());
		}
		
		return table.build();
	}
}