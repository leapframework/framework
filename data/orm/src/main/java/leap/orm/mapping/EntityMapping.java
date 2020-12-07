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
import leap.db.model.DbTableBuilder;
import leap.lang.*;
import leap.lang.beans.BeanType;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.command.DeleteHandler;
import leap.orm.command.InsertHandler;
import leap.orm.command.UpdateHandler;
import leap.orm.event.EntityListeners;
import leap.orm.interceptor.EntityExecutionInterceptor;
import leap.orm.mapping.config.QueryConfig;
import leap.orm.model.Model;
import leap.orm.validation.EntityValidator;
import java.util.*;
import java.util.function.Supplier;

public class EntityMapping extends ExtensibleBase {
    private static final Log log = LogFactory.get(EntityMapping.class);

    private static final ThreadLocal<List<EntityListeners>> CONTEXT_LISTENERS = new ThreadLocal<>();
    private static final ThreadLocal<DynamicAndMapping>     CONTEXT_DYNAMIC   = new ThreadLocal<>();

    public static List<EntityListeners> getContextListeners() {
        return CONTEXT_LISTENERS.get();
    }

    public static void addContextListeners(EntityListeners listeners) {
        if (null == listeners) {
            return;
        }
        List<EntityListeners> list = CONTEXT_LISTENERS.get();
        if (null == list) {
            list = new ArrayList<>();
            CONTEXT_LISTENERS.set(list);
        }
        list.add(listeners);
    }

    public static void clearContextListeners() {
        CONTEXT_LISTENERS.remove();
    }

    public static void withContextListeners(EntityListeners listeners, Runnable func) {
        try {
            addContextListeners(listeners);
            func.run();
        } finally {
            clearContextListeners();
        }
    }

    public static <T> T withContextListeners(EntityListeners listeners, Supplier<T> func) {
        try {
            addContextListeners(listeners);
            return func.get();
        } finally {
            clearContextListeners();
        }
    }

    public static void withDynamic(Dynamic dynamic, Runnable func) {
        try {
            CONTEXT_DYNAMIC.set(new DynamicAndMapping(dynamic));
            func.run();
        } finally {
            CONTEXT_DYNAMIC.remove();
        }
    }

    public static <T> T withDynamic(Dynamic dynamic, Supplier<T> func) {
        try {
            CONTEXT_DYNAMIC.set(new DynamicAndMapping(dynamic));
            return func.get();
        } finally {
            CONTEXT_DYNAMIC.remove();
        }
    }

    public static void tryWithDynamic(Dynamic dynamic, Runnable func) {
        if (null == dynamic) {
            func.run();
        } else {
            withDynamic(dynamic, func);
        }
    }

    public static <T> T tryWithDynamic(Dynamic dynamic, Supplier<T> func) {
        if (null == dynamic) {
            return func.get();
        } else {
            return withDynamic(dynamic, func);
        }
    }

    public static void setDynamic(Dynamic dynamic) {
        CONTEXT_DYNAMIC.set(new DynamicAndMapping(dynamic));
    }

    public static void removeDynamic() {
        CONTEXT_DYNAMIC.remove();
    }

    protected final EntityMappingBuilder       builder;
    protected final String                     entityName;
    protected final String                     dynamicTableName;
    protected final String                     wideEntityName;
    protected final Class<?>                   entityClass;
    protected final Class<?>                   extendedEntityClass;
    protected final BeanType                   beanType;
    protected final DbTable                    table;
    protected final DbTable                    secondaryTable;
    protected final DbColumn                   embeddingColumn;
    protected final String                     queryView;
    protected final FieldMapping[]             fieldMappings;
    protected final FieldMapping[]             filterFieldMappings;
    protected final FieldMapping[]             embeddedFieldMappings;
    protected final FieldMapping[]             keyFieldMappings;
    protected final String[]                   keyFieldNames;
    protected final String[]                   keyColumnNames;
    protected final boolean                    autoIncrementKey;
    protected final DbColumn                   autoIncrementKeyColumn;
    protected final FieldMapping               autoIncrementKeyField;
    protected final FieldMapping               optimisticLockField;
    protected final InsertHandler              insertHandler;
    protected final UpdateHandler              updateHandler;
    protected final DeleteHandler              deleteHandler;
    protected final EntityExecutionInterceptor insertInterceptor;
    protected final EntityExecutionInterceptor updateInterceptor;
    protected final EntityExecutionInterceptor deleteInterceptor;
    protected final EntityExecutionInterceptor findInterceptor;
    protected final Class<? extends Model>     modelClass;

    protected final EntityValidator[]   validators;
    protected final RelationMapping[]   relationMappings;
    protected final RelationProperty[]  relationProperties;
    protected final boolean             autoCreateTable;
    protected final boolean             selfReferencing;
    protected final RelationMapping[]   selfReferencingRelations;
    protected final EntityListeners     listeners;
    protected final QueryConfig         queryConfig;
    protected final boolean             queryFilterEnabled;
    protected final boolean             autoValidate;
    protected final boolean             dynamicEnabled;
    protected final Dynamic             dynamic;
    protected final boolean             logical;
    protected final boolean             remote;
    protected final RemoteSettings      remoteSettings;
    protected final UnionSettings       unionSettings;
    protected final Map<String, String> groupByExprs;
    protected final Map<String, String> selectExprs;
    protected final Map<String, String> orderByExprs;
    protected final Map<String, String> filtersExprs;
    protected final Map<String, String> aggregatesExprs;

    private final Map<String, FieldMapping>    columnNameToFields;
    private final Map<String, FieldMapping>    fieldNameToFields;
    private final Map<String, FieldMapping>    metaNameToFields;
    private final Map<String, RelationMapping> nameToRelations;
    private final Map<String, RelationMapping> primaryKeyRelations;
    private final Map<String, RelationMapping> targetEntityRelations;
    private final Map<String, RelationMapping> referenceToRelations;

    public EntityMapping(EntityMappingBuilder builder,
                         String entityName, String wideEntityName, String dynamicTableName,
                         Class<?> entityClass, Class<?> extendedEntityClass, DbTable table, DbTable secondaryTable,
                         DbColumn embeddingColumn,
                         String queryView, List<FieldMapping> fieldMappings,
                         InsertHandler insertHandler, UpdateHandler updateHandler, DeleteHandler deleteHandler,
                         EntityExecutionInterceptor insertInterceptor, EntityExecutionInterceptor updateInterceptor,
                         EntityExecutionInterceptor deleteInterceptor, EntityExecutionInterceptor findInterceptor,
                         Class<? extends Model> modelClass,
                         List<EntityValidator> validators,
                         List<RelationMapping> relationMappings,
                         RelationProperty[] relationProperties,
                         boolean autoCreateTable, QueryConfig queryConfig, boolean queryFilterEnabled, boolean autoValidate,
                         boolean dynamicEnabled, Dynamic dynamic,
                         boolean logical, boolean remote, RemoteSettings remoteSettings, UnionSettings unionSettings,
                         Map<String, String> groupByExprs, Map<String, String> selectExprs, Map<String, String> orderByExprs,
                         Map<String, String> filtersExprs, Map<String, String> aggregatesExprs,
                         EntityListeners listeners) {

        Args.notEmpty(entityName, "entity name");
        Args.notNull(table, "table");
        Args.notEmpty(fieldMappings, "field mappings");
        Args.notNull(listeners);
        if (remote && null == remoteSettings) {
            throw new IllegalStateException("Remote settings must not be null for remote entity '" + entityName + "'");
        }

        if (null != dynamic) {
            dynamic.getFieldMappings().forEach(fieldMappings::add);
        }

        this.builder = builder;
        this.entityName = entityName;
        this.wideEntityName = wideEntityName;
        this.dynamicTableName = dynamicTableName;
        this.entityClass = entityClass;
        this.extendedEntityClass = extendedEntityClass;
        this.beanType = null == entityClass ? null : BeanType.of(entityClass);
        this.table = table;
        this.secondaryTable = secondaryTable;
        this.embeddingColumn = embeddingColumn;
        this.queryView = queryView;
        this.insertHandler = insertHandler;
        this.updateHandler = updateHandler;
        this.deleteHandler = deleteHandler;
        this.insertInterceptor = insertInterceptor;
        this.updateInterceptor = updateInterceptor;
        this.deleteInterceptor = deleteInterceptor;
        this.findInterceptor = findInterceptor;
        this.modelClass = modelClass;
        this.validators = null == validators ? new EntityValidator[]{} : validators.toArray(new EntityValidator[validators.size()]);
        this.relationMappings = null == relationMappings ? new RelationMapping[]{} : relationMappings.toArray(new RelationMapping[relationMappings.size()]);
        this.relationProperties = relationProperties;

        this.fieldMappings = fieldMappings.toArray(new FieldMapping[fieldMappings.size()]);
        this.columnNameToFields = createColumnNameToFieldsMap();
        this.fieldNameToFields = createFieldNameToFieldsMap();
        this.metaNameToFields = createMetaNameToFieldsMap();
        this.nameToRelations = createNameToRelationsMap();
        this.primaryKeyRelations = createPrimaryKeyRelations();
        this.targetEntityRelations = createTargetEntityRelations();
        this.referenceToRelations = createReferenceToRelations();
        this.filterFieldMappings = evalFilterFieldMappings();
        this.embeddedFieldMappings = evalEmbeddedFieldMappings();
        this.keyFieldMappings = evalKeyFieldMappings();
        this.keyFieldNames = evalKeyFieldNames();
        this.keyColumnNames = evalKeyColumnNames();
        this.autoIncrementKey = table.getPrimaryKeyColumnNames().length == 1 && table.getPrimaryKeyColumns()[0].isAutoIncrement();
        this.autoIncrementKeyColumn = autoIncrementKey ? table.getPrimaryKeyColumns()[0] : null;
        this.autoIncrementKeyField = autoIncrementKey ? keyFieldMappings[0] : null;
        this.optimisticLockField = findOptimisticLockField();
        this.autoCreateTable = autoCreateTable;
        this.queryConfig = queryConfig;
        this.queryFilterEnabled = queryFilterEnabled;
        this.autoValidate = autoValidate;
        this.dynamicEnabled = dynamicEnabled;
        this.dynamic = dynamic;
        this.logical = logical;
        this.remote = remote;
        this.remoteSettings = remoteSettings;
        this.unionSettings = unionSettings;
        this.groupByExprs = null == groupByExprs ? Collections.emptyMap() : Collections.unmodifiableMap(groupByExprs);
        this.selectExprs = null == selectExprs ? Collections.emptyMap() : Collections.unmodifiableMap(selectExprs);
        this.orderByExprs = null == orderByExprs ? Collections.emptyMap() : Collections.unmodifiableMap(orderByExprs);
        this.filtersExprs = null == filtersExprs ? Collections.emptyMap() : Collections.unmodifiableMap(filtersExprs);
        this.aggregatesExprs = null == aggregatesExprs ? Collections.emptyMap() : Collections.unmodifiableMap(aggregatesExprs);

        this.selfReferencingRelations = evalSelfReferencingRelations();
        this.selfReferencing = selfReferencingRelations.length > 0;

        this.listeners = listeners;

        if (filterFieldMappings.length > 1) {
            throw new IllegalStateException("Two or more filter columns in an entity is not supported yet!");
        }

        if (null != secondaryTable && keyFieldNames.length != 1) {
            throw new IllegalStateException("Entity with secondary table must has one key field only");
        }

        if (null != unionSettings) {
            this.unionSettings.initTypeAndEntityMap();
        }
    }

    /**
     * Returns the {@link EntityMapping} with the {@link Dynamic} or self if no {@link Dynamic}.
     */
    public EntityMapping withDynamic() {
        if (null != dynamic) {
            return this;
        }

        if (dynamicEnabled) {
            DynamicAndMapping dynamicAndMapping = CONTEXT_DYNAMIC.get();
            if (null != dynamicAndMapping) {
                EntityMapping mapping = dynamicAndMapping.mapping;
                if (null == mapping) {
                    DbTableBuilder ot = builder.getTable();
                    DbTableBuilder nt = new DbTableBuilder(ot.getCatalog(), ot.getSchema(), ot.getName());
                    builder.setTable(nt);
                    mapping = builder.build(dynamicAndMapping.dynamic);
                    dynamicAndMapping.mapping = mapping;
                }
                return mapping;
            }
        }

        return this;
    }

    /**
     * Returns the builder to build this object.
     */
    public EntityMappingBuilder getBuilder() {
        return builder;
    }

    /**
     * Returns the entity name.
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Returns true if this entity is a view of other physical entity.
     */
    public boolean isNarrowEntity() {
        return !Strings.isEmpty(wideEntityName);
    }

    /**
     * Returns the wide entity name.
     */
    public String getWideEntityName() {
        return wideEntityName;
    }

    /**
     * Returns the dynamic table name.
     */
    public String getDynamicTableName() {
        return dynamicTableName;
    }

    /**
     * Returns the table name of entity.
     */
    public String getTableName() {
        return table.getName();
    }

    /**
     * Returns true if query view defined.
     */
    public boolean hasQueryView() {
        return null != queryView;
    }

    /**
     * Returns the sql as query view.
     */
    public String getQueryView() {
        return queryView;
    }

    /**
     * Optional. Returns the mapping java class of entity.
     */
    public Class<?> getEntityClass() {
        return entityClass;
    }

    /**
     * Optional.
     */
    public Class<?> getExtendedEntityClass() {
        return extendedEntityClass;
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
     * Returns the embedding column name or null.
     */
    public String getEmbeddingColumnName() {
        return null == embeddingColumn ? null : embeddingColumn.getName();
    }

    /**
     * Returns the column that stores embedded fields.
     */
    public DbColumn getEmbeddingColumn() {
        return embeddingColumn;
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
        if (null == r) {
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
     * <p>
     * <p/>
     * Returns null if no relation or multi relations has been found fo the target entity name.
     */
    public RelationMapping tryGetKeyRelationMappingOfTargetEntity(String entityName) {
        return primaryKeyRelations.get(entityName);
    }

    /**
     * Returns the unique {@link RelationMapping} of the target entity name.
     * <p>
     * <p/>
     * Returns null if no relation or multi relations has been found fo the target entity name.
     */
    public RelationMapping tryGetRelationMappingOfTargetEntity(String entityName) {
        return targetEntityRelations.get(entityName);
    }

    /**
     * Returns the unique many-to-one {@link RelationMapping} reference to the target entity name.
     * <p>
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
        if (null == p) {
            throw new ObjectNotFoundException("Relation Property '" + name + "' not exists!");
        }
        return p;
    }

    /**
     * Returns the {@link RelationProperty} matches the given name.
     * <p>
     * <p/>
     * Returns null if not exists.
     */
    public RelationProperty tryGetRelationProperty(String name) {
        for (RelationProperty p : relationProperties) {
            if (Strings.equals(p.getName(), name)) {
                return p;
            }
        }
        return null;
    }

    public boolean hasEmbeddedFieldMappings() {
        EntityMapping em = withDynamic();
        if (em == this) {
            return embeddedFieldMappings.length > 0;
        } else {
            return em.hasEmbeddedFieldMappings();
        }
    }

    public FieldMapping[] getEmbeddedFieldMappings() {
        EntityMapping em = withDynamic();
        if (em == this) {
            return embeddedFieldMappings;
        } else {
            return em.getEmbeddedFieldMappings();
        }
    }

    /**
     * Returns all the fields of entity.
     */
    public FieldMapping[] getFieldMappings() {
        EntityMapping em = withDynamic();
        if (em == this) {
            return fieldMappings;
        } else {
            return em.getFieldMappings();
        }
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
        if (keyFieldNames.length == 1) {
            return keyFieldNames[0];
        }
        throw new IllegalStateException("not the one key field only");
    }

    /**
     * Returns the column name if only one key column
     */
    public String idColumnName() throws IllegalStateException {
        if (keyColumnNames.length == 1) {
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

    /**
     * Returns the query config.
     */
    public QueryConfig getQueryConfig() {
        return queryConfig;
    }

    public boolean isQueryFilterEnabled() {
        return queryFilterEnabled;
    }

    /**
     * Returns is auto validates the entity if insert or update.
     */
    public boolean isAutoValidate() {
        return autoValidate;
    }

    /**
     * Is the {@link Dynamic} enabled.
     */
    public boolean isDynamicEnabled() {
        return dynamicEnabled;
    }

    /**
     * Returns <code>true</code> if the field mappings contains dynamic fields.
     */
    public boolean hasDynamicFields() {
        return null != dynamic && dynamic.getFieldMappings().size() > 0;
    }

    /**
     * Is a logical entity?
     */
    public boolean isLogical() {
        return logical;
    }

    /**
     * Returns true if this entity is an reference entity.
     */
    public boolean isRemote() {
        return remote;
    }

    /**
     * Returns true if this entity is a remote rest entity.
     */
    public boolean isRemoteRest() {
        return remote && remoteSettings.isRest();
    }

    /**
     * Returns the validators for validating the entity.
     */
    public EntityValidator[] getValidators() {
        return validators;
    }

    public FieldMapping getFieldMapping(String fieldName) throws ObjectNotFoundException {
        FieldMapping fm = tryGetFieldMapping(fieldName);

        if (null == fm) {
            throw new ObjectNotFoundException("Field mapping '" + fieldName + "' not exists in entity '" + getEntityName() + "'");
        }

        return fm;
    }

    public FieldMapping tryGetFieldMapping(String fieldName) {
        EntityMapping em = withDynamic();
        if (em == this) {
            return fieldNameToFields.get(fieldName.toLowerCase());
        } else {
            return em.tryGetFieldMapping(fieldName);
        }
    }

    /**
     * Returns the {@link FieldMapping} object mapping to the given column (ignore case).
     *
     * @throws ObjectNotFoundException if no {@link FieldMapping} mapping to the given column.
     */
    public FieldMapping getFieldMappingByColumn(String columnName) throws ObjectNotFoundException {
        FieldMapping fm = tryGetFieldMappingByColumn(columnName);

        if (null == fm) {
            throw new ObjectNotFoundException("No field mapped to the column '" + columnName + "' in entity '" + getEntityName() + "'");
        }

        return fm;
    }

    public FieldMapping tryGetFieldMappingByColumn(String columnName) {
        EntityMapping em = withDynamic();
        if (em == this) {
            return columnNameToFields.get(columnName.toLowerCase());
        } else {
            return em.tryGetFieldMappingByColumn(columnName);
        }
    }

    public FieldMapping getFieldMappingByMetaName(ReservedMetaFieldName metaFieldName) throws ObjectNotFoundException {
        Args.notNull(metaFieldName, "metaFieldName");
        return getFieldMappingByMetaName(metaFieldName.getFieldName());
    }

    public FieldMapping getFieldMappingByMetaName(String metaFieldName) throws ObjectNotFoundException {
        FieldMapping fm = tryGetFieldMappingByMetaName(metaFieldName);
        if (null == fm) {
            throw new ObjectNotFoundException("No meta field '" + metaFieldName + "' in entity '" + getEntityName() + "'");
        }
        return fm;
    }

    public FieldMapping tryGetFieldMappingByMetaName(ReservedMetaFieldName metaFieldName) {
        if (null == metaFieldName) {
            return null;
        }
        return metaNameToFields.get(metaFieldName.getFieldName().toLowerCase());
    }

    public FieldMapping tryGetFieldMappingByMetaName(String metaFieldName) {
        if (null == metaFieldName) {
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

    public boolean hasOptimisticLock() {
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

    public InsertHandler getInsertHandler() {
        return insertHandler;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public DeleteHandler getDeleteHandler() {
        return deleteHandler;
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
        for (RelationMapping rm : relationMappings) {
            if (rm.isManyToOne() && rm.getTargetEntityName().equalsIgnoreCase(entityName)) {
                return true;
            }
        }
        return false;
    }

    public RemoteSettings getRemoteSettings() {
        return remoteSettings;
    }

    public boolean isUnionEntity() {
        return null != unionSettings;
    }

    public UnionSettings getUnionSettings() {
        return unionSettings;
    }

    public Map<String, String> getGroupByExprs() {
        return groupByExprs;
    }

    public Map<String, String> getSelectExprs() {
        return selectExprs;
    }

    public Map<String, String> getOrderByExprs() {
        return orderByExprs;
    }

    public Map<String, String> getFiltersExprs() {
        return filtersExprs;
    }

    public Map<String, String> getAggregatesExprs() {
        return aggregatesExprs;
    }

    public EntityListeners getListeners() {
        return listeners;
    }

    private FieldMapping[] evalKeyFieldMappings() {
        List<FieldMapping> list = New.arrayList();

        for (FieldMapping fm : this.fieldMappings) {
            if (fm.isPrimaryKey()) {
                list.add(fm);
            }
        }

        return list.toArray(new FieldMapping[list.size()]);
    }

    private FieldMapping[] evalFilterFieldMappings() {
        List<FieldMapping> list = New.arrayList();

        for (FieldMapping fm : this.fieldMappings) {
            if (fm.isFiltered()) {
                Assert.isTrue(null != fm.getFilteredValue(),
                        "There filter value expression must not be null of filter field '" + fm.getFieldName() + "'");
                list.add(fm);
            }
        }

        return list.toArray(new FieldMapping[list.size()]);
    }

    private FieldMapping[] evalEmbeddedFieldMappings() {
        List<FieldMapping> list = New.arrayList();

        for (FieldMapping fm : this.fieldMappings) {
            if (fm.isEmbedded()) {
                list.add(fm);
            }
        }
        return list.toArray(new FieldMapping[list.size()]);
    }

    private String[] evalKeyFieldNames() {
        String[] names = new String[keyFieldMappings.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = keyFieldMappings[i].getFieldName();
        }
        return names;
    }

    private String[] evalKeyColumnNames() {
        String[] names = new String[keyFieldMappings.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = keyFieldMappings[i].getColumnName();
        }
        return names;
    }

    private Map<String, FieldMapping> createColumnNameToFieldsMap() {
        Map<String, FieldMapping> map = New.linkedHashMap();
        for (FieldMapping fm : fieldMappings) {
            map.put(fm.getColumn().getName().toLowerCase(), fm);
        }
        return Collections.unmodifiableMap(map);
    }

    private Map<String, FieldMapping> createFieldNameToFieldsMap() {
        Map<String, FieldMapping> map = New.linkedHashMap();
        for (FieldMapping fm : fieldMappings) {
            map.put(fm.getFieldName().toLowerCase(), fm);
        }
        return Collections.unmodifiableMap(map);
    }

    private Map<String, RelationMapping> createNameToRelationsMap() {
        Map<String, RelationMapping> map = New.linkedHashMap();
        for (RelationMapping r : relationMappings) {

            if (map.containsKey(r.getName())) {
                throw new IllegalStateException("Found duplicated relation name '" +
                        r.getName() + "' in entity '" + entityName + "'");
            }

            map.put(r.getName(), r);
        }

        return Collections.unmodifiableMap(map);
    }

    private Map<String, FieldMapping> createMetaNameToFieldsMap() {
        Map<String, FieldMapping> map = New.hashMap();

        for (FieldMapping fm : fieldMappings) {
            String metaName = fm.getMetaFieldName();
            if (!Strings.isEmpty(metaName)) {
                String key = metaName.toLowerCase();

                FieldMapping exists = map.get(key);
                if (null != exists) {
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

        for (RelationMapping r : relationMappings) {

            if (r.isManyToOne()) {

                boolean primaryKey = true;

                for (JoinFieldMapping f : r.getJoinFields()) {

                    if (!f.isLocalPrimaryKey()) {
                        primaryKey = false;
                    }

                }

                if (primaryKey) {
                    map.put(r.getTargetEntityName(), r);
                }

            }

        }

        return map;
    }

    private Map<String, RelationMapping> createTargetEntityRelations() {
        Map<String, List<RelationMapping>> map = new HashMap<>();

        for (RelationMapping r : relationMappings) {

            List<RelationMapping> list = map.get(r.getTargetEntityName());
            if (null == list) {
                list = New.arrayList();
                map.put(r.getTargetEntityName(), list);
            }

            list.add(r);
        }

        Map<String, RelationMapping> singleRelations = new HashMap<>();
        map.forEach((name, list) -> {
            if (list.size() == 1) {
                singleRelations.put(name, list.get(0));
            }
        });

        return Collections.unmodifiableMap(singleRelations);
    }

    private Map<String, RelationMapping> createReferenceToRelations() {
        Map<String, List<RelationMapping>> map = new HashMap<>();

        for (RelationMapping r : relationMappings) {
            if (r.isManyToOne()) {
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
            if (list.size() == 1) {
                singleRelations.put(name, list.get(0));
            }
        });

        return Collections.unmodifiableMap(singleRelations);
    }

    private RelationMapping[] evalSelfReferencingRelations() {
        List<RelationMapping> list = new ArrayList<>();
        for (RelationMapping rm : relationMappings) {
            if (rm.isManyToOne() && rm.getTargetEntityName().equalsIgnoreCase(entityName)) {
                list.add(rm);
            }
        }
        return list.toArray(new RelationMapping[0]);
    }

    private FieldMapping findOptimisticLockField() {
        for (FieldMapping fm : fieldMappings) {
            if (fm.isOptimisticLock()) {
                return fm;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Entity[name=" + getEntityName() + ",table=" + getTableName() + ",class=" + (entityClass == null ? "null" : entityClass.getName()) + "]";
    }

    public interface Dynamic {
        Collection<FieldMapping> getFieldMappings();
    }

    private static class DynamicAndMapping {
        private final Dynamic       dynamic;
        private       EntityMapping mapping;

        public DynamicAndMapping(Dynamic dynamic) {
            this.dynamic = dynamic;
        }
    }
}