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

import leap.core.metamodel.ReservedMetaFieldName;
import leap.db.model.DbColumn;
import leap.db.model.DbTable;
import leap.lang.*;
import leap.lang.beans.BeanType;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.event.EntityListeners;
import leap.orm.interceptor.EntityExecutionInterceptor;
import leap.orm.model.Model;
import leap.orm.sharding.ShardingAlgorithm;
import leap.orm.validation.EntityValidator;

import java.util.*;

public class EntityMapping extends ExtensibleBase {
	private static final Log log = LogFactory.get(EntityMapping.class);

    protected final String                     entityName;
    protected final String                     dynamicTableName;
    protected final Class<?>                   entityClass;
    protected final BeanType                   beanType;
    protected final DbTable                    table;
    protected final DbTable                    secondaryTable;
    protected final FieldMapping[]             fieldMappings;
    protected final FieldMapping[]             filterFieldMappings;
    protected final FieldMapping[]             keyFieldMappings;
    protected final String[]                   keyFieldNames;
    protected final String[]                   keyColumnNames;
    protected final boolean                    autoIncrementKey;
    protected final DbColumn                   autoIncrementKeyColumn;
    protected final FieldMapping               autoIncrementKeyField;
    protected final FieldMapping               optimisticLockField;
    protected final EntityExecutionInterceptor insertInterceptor;
    protected final EntityExecutionInterceptor updateInterceptor;
    protected final EntityExecutionInterceptor deleteInterceptor;
    protected final EntityExecutionInterceptor findInterceptor;
    protected final Class<? extends Model>     modelClass;
    protected final EntityValidator[]          validators;
    protected final RelationMapping[]          relationMappings;
    protected final RelationProperty[]         relationProperties;
    protected final boolean                    autoCreateTable;
    protected final boolean                    sharding;
    protected final boolean                    autoCreateShardingTable;
    protected final ShardingAlgorithm          shardingAlgorithm;
    protected final boolean                    selfReferencing;
    protected final RelationMapping[]          selfReferencingRelations;
    protected final EntityListeners            listeners;
    protected final boolean                    queryFilterEnabled;

    private final Map<String,FieldMapping>    columnNameToFields;
	private final Map<String,FieldMapping>    fieldNameToFields;
	private final Map<String,FieldMapping>    metaNameToFields;
    private final Map<String,RelationMapping> nameToRelations;
    private final Map<String,RelationMapping> primaryKeyRelations;
    private final Map<String,RelationMapping> targetEntityRelations;
    private final Map<String,RelationMapping> referenceToRelations;
    private final FieldMapping                shardingField;


	public EntityMapping(String entityName, String dynamicTableName,
                         Class<?> entityClass, DbTable table, DbTable secondaryTable, List<FieldMapping> fieldMappings,
                         EntityExecutionInterceptor insertInterceptor, EntityExecutionInterceptor updateInterceptor,
                         EntityExecutionInterceptor deleteInterceptor, EntityExecutionInterceptor findIncerceptor,
                         Class<? extends Model> modelClass,
                         List<EntityValidator> validators,
                         List<RelationMapping> relationMappings,
                         RelationProperty[] relationProperties,
                         boolean autoCreateTable,
                         boolean queryFilterEnabled,
                         boolean sharding, boolean autoCreateShardingTable, ShardingAlgorithm shardingAlgorithm,
                         EntityListeners listeners) {
		
		Args.notEmpty(entityName,"entity name");
		Args.notNull(table,"table");
		Args.notEmpty(fieldMappings,"field mappings");
        Args.notNull(listeners);

        if(sharding) {
            Args.notNull(shardingAlgorithm, "The sharding algorithm must not be null in sharding entity");
        }
		
		this.entityName		   = entityName;
        this.dynamicTableName  = dynamicTableName;
	    this.entityClass       = entityClass;
	    this.beanType          = null == entityClass ? null : BeanType.of(entityClass);
	    this.table             = table;
        this.secondaryTable    = secondaryTable;
	    this.insertInterceptor = insertInterceptor;
	    this.updateInterceptor = updateInterceptor;
	    this.deleteInterceptor = deleteInterceptor;
	    this.findInterceptor   = findIncerceptor;
	    this.modelClass        = modelClass;
	    this.validators        = null == validators ? new EntityValidator[]{} : validators.toArray(new EntityValidator[validators.size()]);
	    this.relationMappings  = null == relationMappings ? new RelationMapping[]{} : relationMappings.toArray(new RelationMapping[relationMappings.size()]);
        this.relationProperties = relationProperties;
	    
	    this.fieldMappings          = fieldMappings.toArray(new FieldMapping[fieldMappings.size()]);
	    this.columnNameToFields     = createColumnNameToFieldsMap();
	    this.fieldNameToFields      = createFieldNameToFieldsMap();
	    this.metaNameToFields		= createMetaNameToFieldsMap();
        this.nameToRelations        = createNameToRelationsMap();
        this.primaryKeyRelations    = createPrimaryKeyRelations();
        this.targetEntityRelations  = createTargetEntityRelations();
        this.referenceToRelations   = createReferenceToRelations();
        this.filterFieldMappings    = evalFilterFieldMappings();
	    this.keyFieldMappings       = evalKeyFieldMappings();
	    this.keyFieldNames          = evalKeyFieldNames();
	    this.keyColumnNames			= evalKeyColumnNames();
	    this.autoIncrementKey       = table.getPrimaryKeyColumnNames().length == 1 && table.getPrimaryKeyColumns()[0].isAutoIncrement();
	    this.autoIncrementKeyColumn = autoIncrementKey ? table.getPrimaryKeyColumns()[0] : null;
	    this.autoIncrementKeyField  = autoIncrementKey ? keyFieldMappings[0] : null;
	    this.optimisticLockField    = findOptimisticLockField();
        this.autoCreateTable        = autoCreateTable;
        this.queryFilterEnabled     = queryFilterEnabled;
        this.sharding               = sharding;
        this.autoCreateShardingTable= autoCreateShardingTable;
        this.shardingField          = Iterables.firstOrNull(fieldMappings, (f) -> f.isSharding());
        this.shardingAlgorithm      = shardingAlgorithm;

        this.selfReferencingRelations = evalSelfReferencingRelations();
        this.selfReferencing = selfReferencingRelations.length > 0;

        this.listeners = listeners;

        if(filterFieldMappings.length > 1) {
            throw new IllegalStateException("Two or more filter columns in an entity is not supported yet!");
        }

        if(null != secondaryTable && keyFieldNames.length != 1) {
            throw new IllegalStateException("Entity with secondary table must has one key field only");
        }
    }

    /**
     * Returns the entity name.
     */
	public String getEntityName(){
		return entityName;
	}

    /**
     * Returns the dynamic table name.
     */
	public String getDynamicTableName(){
		return dynamicTableName;
	}

    /**
     * Returns the table name of entity.
     */
	public String getTableName(){
		return table.getName();
	}

    /**
     * Optional. Returns the mapping java class of entity.
     */
	public Class<?> getEntityClass() {
		return entityClass;
	}

    /**
     * Optional. Returns the mapping {@link Model} class of entity.
     */
    public Class<? extends Model> getModelClass() {
        return modelClass;
    }

    /**
     * Returns the db table.
     */
	public DbTable getTable() {
		return table;
	}

    /**
     * Returns the secondary table or null/
     */
    public DbTable getSecondaryTable() {
        return secondaryTable;
    }

    /**
     * Returns the secondary table name or null
     */
    public String getSecondaryTableName() {
        return null == secondaryTable ? null : secondaryTable.getName();
    }

    /**
     * Returns true if the secondary table is exists.
     */
    public boolean hasSecondaryTable() {
        return null != secondaryTable;
    }

    /**
     * Optional. Returns {@link BeanType} of the mapping java class of entity.
     */
	public BeanType getBeanType() {
		return beanType;
	}

    /**
     * Returns all the {@link RelationMapping}.
     */
    public RelationMapping[] getRelationMappings() {
        return relationMappings;
    }

    /**
     * Returns the relation of the given name.
     */
    public RelationMapping getRelationMapping(String name) throws ObjectNotFoundException {
        RelationMapping r = nameToRelations.get(name);
        if(null == r) {
            throw new ObjectNotFoundException("Relation '" + name + "' not exists in entity '" + entityName + "'");
        }
        return r;
    }

    /**
     * Returns the relation of the given name or null if not exists.
     */
    public RelationMapping tryGetRelationMapping(String name) throws ObjectNotFoundException {
        return nameToRelations.get(name);
    }

    /**
     * Returns the primary key {@link RelationMapping} of the target entity name.
     *
     * <p/>
     * Returns null if no relation or multi relations has been found fo the target entity name.
     */
    public RelationMapping tryGetKeyRelationMappingOfTargetEntity(String entityName) {
        return primaryKeyRelations.get(entityName);
    }

    /**
     * Returns the unique {@link RelationMapping} of the target entity name.
     *
     * <p/>
     * Returns null if no relation or multi relations has been found fo the target entity name.
     */
    public RelationMapping tryGetRelationMappingOfTargetEntity(String entityName) {
        return targetEntityRelations.get(entityName);
    }

    /**
     * Returns the unique many-to-one {@link RelationMapping} reference to the target entity name.
     *
     * <p/>
     * Returns null if no relation or multi relations has been found fo the target entity name.
     */
    public RelationMapping tryGetRefRelationMappingOfTargetEntity(String entityName) {
        return referenceToRelations.get(entityName);
    }

    /**
     * Returns all the {@link RelationProperty}.
     */
    public RelationProperty[] getRelationProperties() {
        return relationProperties;
    }

    public RelationProperty getRelationProperty(String name) {
        RelationProperty p = tryGetRelationProperty(name);
        if(null == p) {
            throw new ObjectNotFoundException("Relation Property '" + name + "' not exists!");
        }
        return p;
    }

    /**
     * Returns the {@link RelationProperty} matches the given name.
     *
     * <p/>
     * Returns null if not exists.
     */
    public RelationProperty tryGetRelationProperty(String name) {
        for(RelationProperty p : relationProperties) {
            if(p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Returns all the fields of entity.
     */
	public FieldMapping[] getFieldMappings() {
		return fieldMappings;
	}

    /**
     * Returns all the primary key fields of entity.
     */
	public FieldMapping[] getKeyFieldMappings() {
		return keyFieldMappings;
	}

    /**
     * Returns the field name if only one key field
     */
    public String idFieldName() throws IllegalStateException {
        if(keyFieldNames.length == 1) {
            return keyFieldNames[0];
        }
        throw new IllegalStateException("not the one key field only");
    }

    /**
     * Returns the column name if only one key column
     */
    public String idColumnName() throws IllegalStateException {
        if(keyColumnNames.length == 1) {
            return keyColumnNames[0];
        }
        throw new IllegalStateException("not the one key column only");
    }

    /**
     * Returns all the names of primary key fields of entity.
     */
	public String[] getKeyFieldNames() {
		return keyFieldNames;
	}

    /**
     * Returns all the names of primary key columns of entity.
     */
	public String[] getKeyColumnNames() {
		return keyColumnNames;
	}

    /**
     * Returns true if auto create the table.
     */
    public boolean isAutoCreateTable() {
        return autoCreateTable;
    }

    public boolean isQueryFilterEnabled() {
        return queryFilterEnabled;
    }

    /**
     * Returns true if the entity is a sharding entity.
     */
    public boolean isSharding() {
        return sharding;
    }

    /**
     * Returns true if create the sharding table if not exists.
     */
    public boolean isAutoCreateShardingTable() {
        return autoCreateShardingTable;
    }

    /**
     * Returns the sharding field or null.
     */
    public FieldMapping getShardingField() {
        return shardingField;
    }

    /**
     * Returns the {@link ShardingAlgorithm} or null.
     */
    public ShardingAlgorithm getShardingAlgorithm() {
        return shardingAlgorithm;
    }

    /**
     * Returns true if the given table name is the sharding table of the entity.
     */
    public boolean isShardingTable(String tableName) {
        return sharding ? shardingAlgorithm.isShardingTable(this, tableName) : false;
    }

    /**
     * Returns the validators for validating the entity.
     */
	public EntityValidator[] getValidators() {
		return validators;
	}

	public FieldMapping getFieldMapping(String fieldName) throws ObjectNotFoundException {
		FieldMapping fm = tryGetFieldMapping(fieldName);
		
		if(null == fm){
			throw new ObjectNotFoundException("Field mapping '" + fieldName + "' not exists in entity '" + getEntityName() + "'");
		}
		
		return fm;
	}
	
	public FieldMapping tryGetFieldMapping(String fieldName) {
		Args.notNull(fieldName,"field name");
		return fieldNameToFields.get(fieldName.toLowerCase());
	}
	
	/**
	 * Returns the {@link FieldMapping} object mapping to the given column (ignore case).
	 * 
	 * @throws ObjectNotFoundException if no {@link FieldMapping} mapping to the given column.
	 */
	public FieldMapping getFieldMappingByColumn(String columnName) throws ObjectNotFoundException {
		FieldMapping fm = tryGetFieldMappingByColumn(columnName);
		
		if(null == fm){
			throw new ObjectNotFoundException("No field mapped to the column '" + columnName + "' in entity '" + getEntityName() + "'");
		}
		
		return fm;
	}
	
	public FieldMapping tryGetFieldMappingByColumn(String columnName) {
		Args.notNull(columnName,"column name");
		return columnNameToFields.get(columnName.toLowerCase());
	}
	
	public FieldMapping getFieldMappingByMetaName(ReservedMetaFieldName metaFieldName) throws ObjectNotFoundException {
		Args.notNull(metaFieldName,"metaFieldName");
		return getFieldMappingByMetaName(metaFieldName.getFieldName());
	}
	
	public FieldMapping getFieldMappingByMetaName(String metaFieldName) throws ObjectNotFoundException {
		FieldMapping fm = tryGetFieldMappingByMetaName(metaFieldName);
		if(null == fm){
			throw new ObjectNotFoundException("No meta field '" + metaFieldName + "' in entity '" + getEntityName() + "'");
		}
		return fm;
	}
	
	public FieldMapping tryGetFieldMappingByMetaName(ReservedMetaFieldName metaFieldName) {
		if(null == metaFieldName){
			return null;
		}
		return metaNameToFields.get(metaFieldName.getFieldName().toLowerCase());
	}
	
	public FieldMapping tryGetFieldMappingByMetaName(String metaFieldName) {
		if(null == metaFieldName){
			return null;
		}
		return metaNameToFields.get(metaFieldName.toLowerCase());
	}
	
	public boolean isAutoIncrementKey() {
		return autoIncrementKey;
	}
	
	public boolean isCompositeKey() {
		return keyColumnNames.length > 1;
	}
	
	public DbColumn getAutoIncrementKeyColumn() {
		return autoIncrementKeyColumn;
	}
	
	public FieldMapping getAutoIncrementKeyField() {
		return autoIncrementKeyField;
	}
	
	public boolean hasOptimisticLock(){
		return null != optimisticLockField;
	}

    public boolean hasFilterFields() {
        return filterFieldMappings.length > 0;
    }

    public FieldMapping[] getFilterFieldMappings() {
        return filterFieldMappings;
    }

    public FieldMapping getOptimisticLockField() {
		return optimisticLockField;
	}

	public EntityExecutionInterceptor getInsertInterceptor() {
		return insertInterceptor;
	}

	public EntityExecutionInterceptor getUpdateInterceptor() {
		return updateInterceptor;
	}

	public EntityExecutionInterceptor getDeleteInterceptor() {
		return deleteInterceptor;
	}

	public EntityExecutionInterceptor getFindInterceptor() {
		return findInterceptor;
	}

    public RelationMapping[] getSelfReferencingRelations() {
        return selfReferencingRelations;
    }

    public boolean isSelfReferencing() {
        return selfReferencing;
    }

    public boolean isReferenceTo(String entityName) {
        for(RelationMapping rm : relationMappings) {
            if(rm.isManyToOne() && rm.getTargetEntityName().equalsIgnoreCase(entityName)) {
                return true;
            }
        }
        return false;
    }

    public EntityListeners getListeners() {
        return listeners;
    }

    private FieldMapping[] evalKeyFieldMappings(){
		List<FieldMapping> list = New.arrayList();
		
		for(FieldMapping fm : this.fieldMappings){
			if(fm.isPrimaryKey()){
				list.add(fm);
			}
		}
		
		return list.toArray(new FieldMapping[list.size()]);
	}

    private FieldMapping[] evalFilterFieldMappings(){
        List<FieldMapping> list = New.arrayList();

        for(FieldMapping fm : this.fieldMappings){
            if(fm.isFiltered()){
                Assert.isTrue(null != fm.getFilteredValue(),
                             "There filter value expression must not be null of filter field '" + fm.getFieldName() + "'");
                list.add(fm);
            }
        }

        return list.toArray(new FieldMapping[list.size()]);
    }
	
	private String[] evalKeyFieldNames(){
		String[] names = new String[keyFieldMappings.length];
		for(int i=0;i<names.length;i++){
			names[i] = keyFieldMappings[i].getFieldName();
		}
		return names;
	}
	
	private String[] evalKeyColumnNames(){
		String[] names = new String[keyFieldMappings.length];
		for(int i=0;i<names.length;i++){
			names[i] = keyFieldMappings[i].getColumnName();
		}
		return names;
	}

	private Map<String,FieldMapping> createColumnNameToFieldsMap(){
		Map<String,FieldMapping> map = New.linkedHashMap();
		for(FieldMapping fm : fieldMappings){
			map.put(fm.getColumn().getName().toLowerCase(),fm);
		}
		return Collections.unmodifiableMap(map);
	}
	
	private Map<String,FieldMapping> createFieldNameToFieldsMap(){
		Map<String,FieldMapping> map = New.linkedHashMap();
		for(FieldMapping fm : fieldMappings){
			map.put(fm.getFieldName().toLowerCase(),fm);
		}
		return Collections.unmodifiableMap(map);
	}

    private Map<String, RelationMapping> createNameToRelationsMap() {
        Map<String,RelationMapping> map = New.linkedHashMap();
        for(RelationMapping r : relationMappings) {

            if(map.containsKey(r.getName())) {
                throw new IllegalStateException("Found duplicated relation name '" +
                                                    r.getName() + "' in entity '" + entityName + "'");
            }

            map.put(r.getName(), r);
        }

        return Collections.unmodifiableMap(map);
    }
	
	private Map<String,FieldMapping> createMetaNameToFieldsMap() {
		Map<String,FieldMapping> map = New.hashMap();
		
		for(FieldMapping fm : fieldMappings) {
			String metaName = fm.getMetaFieldName();
			if(!Strings.isEmpty(metaName)) {
				String key = metaName.toLowerCase();
				
				FieldMapping exists = map.get(key);
				if(null != exists) {
					log.warn("Found duplicated meta field name '" + metaName + 
							 "' in entity '" + getEntityName() + 
							 "', fields [" + fm.getFieldName() + "," + exists.getFieldName() + "]");	
				}
				
				map.put(key, fm);
			}
		}
		
		return Collections.unmodifiableMap(map);
	}

    private Map<String, RelationMapping> createPrimaryKeyRelations() {
        Map<String, RelationMapping> map = new LinkedHashMap<>();

        for(RelationMapping r : relationMappings) {

            if(r.isManyToOne()) {

                boolean primaryKey = true;

                for(JoinFieldMapping f : r.getJoinFields()) {

                    if(!f.isLocalPrimaryKey()) {
                        primaryKey = false;
                    }

                }

                if(primaryKey) {
                    map.put(r.getTargetEntityName(), r);
                }

            }

        }

        return map;
    }

    private Map<String, RelationMapping> createTargetEntityRelations() {
        Map<String, List<RelationMapping>> map = new HashMap<>();

        for(RelationMapping r : relationMappings) {

            List<RelationMapping> list = map.get(r.getTargetEntityName());
            if(null == list) {
                list = New.arrayList();
                map.put(r.getTargetEntityName(), list);
            }

            list.add(r);
        }

        Map<String, RelationMapping> singleRelations = new HashMap<>();
        map.forEach((name, list) -> {
            if(list.size() == 1) {
                singleRelations.put(name, list.get(0));
            }
        });

        return Collections.unmodifiableMap(singleRelations);
    }

    private Map<String, RelationMapping> createReferenceToRelations() {
        Map<String, List<RelationMapping>> map = new HashMap<>();

        for(RelationMapping r : relationMappings) {
            if(r.isManyToOne()) {
                List<RelationMapping> list = map.get(r.getTargetEntityName());
                if (null == list) {
                    list = New.arrayList();
                    map.put(r.getTargetEntityName(), list);
                }

                list.add(r);
            }
        }

        Map<String, RelationMapping> singleRelations = new HashMap<>();
        map.forEach((name, list) -> {
            if(list.size() == 1) {
                singleRelations.put(name, list.get(0));
            }
        });

        return Collections.unmodifiableMap(singleRelations);
    }

    private RelationMapping[] evalSelfReferencingRelations() {
        List<RelationMapping> list = new ArrayList<>();
        for(RelationMapping rm : relationMappings) {
            if(rm.isManyToOne() && rm.getTargetEntityName().equalsIgnoreCase(entityName)) {
                list.add(rm);
            }
        }
        return list.toArray(new RelationMapping[0]);
    }
	
	private FieldMapping findOptimisticLockField(){
		for(FieldMapping fm : fieldMappings){
			if(fm.isOptimisticLock()){
				return fm;
			}
		}
		return null;
	}

	@Override
    public String toString() {
	    return "Entity[name=" + getEntityName() + ",table=" + getTableName() + ",class=" + (entityClass == null ? "null" : entityClass.getName()) + "]";
    }
}