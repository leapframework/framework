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

import leap.db.model.*;
import leap.lang.Comparators;
import leap.lang.*;
import leap.lang.collection.SimpleCaseInsensitiveMap;
import leap.lang.exception.ObjectExistsException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.command.DeleteHandler;
import leap.orm.command.InsertHandler;
import leap.orm.command.UpdateHandler;
import leap.orm.event.EntityListenersBuilder;
import leap.orm.interceptor.EntityExecutionInterceptor;
import leap.orm.model.Model;
import leap.orm.validation.EntityValidator;

import java.util.*;
import java.util.function.Predicate;

public class EntityMappingBuilder extends ExtensibleBase implements Buildable<EntityMapping> {

    private static final Log log = LogFactory.get(EntityMappingBuilder.class);

    protected String              entityName;
    protected String              wideEntityName;
    protected Class<?>            entityClass;
    protected Class<?>            extendedEntityClass;
    protected boolean             _abstract;
    protected DbTableBuilder      table;
    protected DbTableBuilder      secondaryTable;
    protected String              tablePrefix;
    protected String              dynamicTableName;
    protected boolean             tableNameDeclared;
    protected boolean             idDeclared;
    protected boolean             autoCreateTable;
    protected boolean             autoGenerateColumns;
    protected Boolean             queryFilterEnabled;
    protected boolean             autoValidate;
    protected String              queryView;
    protected boolean             logical;
    protected boolean             remote;
    protected RemoteSettings      remoteSettings;
    protected UnionSettings       unionSettings;
    protected Map<String, String> groupByExprs    = new SimpleCaseInsensitiveMap<>();
    protected Map<String, String> selectExprs     = new SimpleCaseInsensitiveMap<>();
    protected Map<String, String> orderByExprs    = new SimpleCaseInsensitiveMap<>();
    protected Map<String, String> filtersExprs    = new SimpleCaseInsensitiveMap<>();
    protected Map<String, String> aggregatesExprs = new SimpleCaseInsensitiveMap<>();

    protected List<FieldMappingBuilder>    fieldMappings      = new ArrayList<>();
    protected InsertHandler                insertHandler;
    protected UpdateHandler                updateHandler;
    protected DeleteHandler                deleteHandler;
    protected EntityExecutionInterceptor   insertInterceptor;
    protected EntityExecutionInterceptor   updateInterceptor;
    protected EntityExecutionInterceptor   deleteInterceptor;
    protected EntityExecutionInterceptor   findInterceptor;
    protected Class<? extends Model>       modelClass;
    protected DbTable                      physicalTable;
    protected List<EntityValidator>        validators;
    protected List<RelationMappingBuilder> relationMappings   = new ArrayList<>();
    protected List<RelationPropertyBuilder> relationProperties = new ArrayList<>();
    protected List<UniqueKeyBuilder>        keys               = new ArrayList<>();
    protected EntityListenersBuilder        listeners          = new EntityListenersBuilder();

    /*
    public EntityMappingBuilder shallowCopy() {
        EntityMappingBuilder c = new EntityMappingBuilder();

        c.entityName = entityName;
        c.entityClass = entityClass;
        c.extendedEntityClass = extendedEntityClass;
        c._abstract = _abstract;
        c.table = null == table ? null : new DbTableBuilder(table.build());
        c.secondaryTable = null == secondaryTable ? null : new DbTableBuilder(secondaryTable.build());
        c.tablePrefix = tablePrefix;
        c.dynamicTableName = dynamicTableName;
        c.tableNameDeclared = tableNameDeclared;
        c.idDeclared = idDeclared;
        c.autoCreateTable = autoCreateTable;
        c.autoGenerateColumns = autoGenerateColumns;
        c.queryFilterEnabled = queryFilterEnabled;
        c.autoValidate = autoValidate;
        c.remote = remote;
        c.remoteSettings = Beans.copyNew(remoteSettings);
        c.fieldMappings.addAll(fieldMappings);
        c.insertInterceptor = insertInterceptor;
        c.updateInterceptor = updateInterceptor;
        c.deleteInterceptor = deleteInterceptor;
        c.findInterceptor = findInterceptor;
        c.modelClass = modelClass;
        c.physicalTable = physicalTable;

        if(null != validators) {
            c.validators = new ArrayList<>();
            c.validators.addAll(validators);
        }

        c.relationMappings.addAll(relationMappings);
        c.relationProperties.addAll(relationProperties);
        c.keys.addAll(keys);
        c.listeners = listeners;

        return c;
    }
    */

    public Class<?> getSourceClass() {
        return null != entityClass ? entityClass : modelClass;
    }

    public boolean isModelClass() {
        return null != getSourceClass() && Model.class.isAssignableFrom(getSourceClass());
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    @SuppressWarnings("unchecked")
    public EntityMappingBuilder setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;

        if (null != entityClass && Model.class.isAssignableFrom(entityClass)) {
            setModelClass((Class<? extends Model>) entityClass);
        }

        return this;
    }

    public EntityMappingBuilder setExtendedEntityClass(Class<?> cls) {
        this.extendedEntityClass = cls;
        return this;
    }

    public String getEntityName() {
        return entityName;
    }

    public EntityMappingBuilder setEntityName(String entityName) {
        this.entityName = entityName;
        return this;
    }

    public String getWideEntityName() {
        return wideEntityName;
    }

    public void setWideEntityName(String wideEntityName) {
        this.wideEntityName = wideEntityName;
    }

    public boolean isAbstract() {
        return _abstract;
    }

    public EntityMappingBuilder setAbstract(boolean isAbstract) {
        this._abstract = isAbstract;
        return this;
    }

    public DbTableBuilder getTable() {
        if (null == table) {
            table = new DbTableBuilder();
        }
        return table;
    }

    public EntityMappingBuilder setTable(DbTableBuilder table) {
        this.table = table;
        return this;
    }

    public DbTableBuilder getSecondaryTable() {
        return secondaryTable;
    }

    public void setSecondaryTable(DbTableBuilder secondaryTable) {
        this.secondaryTable = secondaryTable;
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

    public String getDynamicTableName() {
        return dynamicTableName;
    }

    public EntityMappingBuilder setDynamicTableName(String dynamicTableName) {
        this.dynamicTableName = dynamicTableName;
        return this;
    }

    public String getTableName() {
        return getTable().getName();
    }

    public String getTableNameWithPrefix() {
        return Strings.concat(tablePrefix, getTableName());
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

    public boolean isAutoGenerateColumns() {
        return autoGenerateColumns;
    }

    public EntityMappingBuilder setAutoGenerateColumns(boolean autoGenerateColumns) {
        this.autoGenerateColumns = autoGenerateColumns;
        return this;
    }

    public Boolean getQueryFilterEnabled() {
        return queryFilterEnabled;
    }

    public void setQueryFilterEnabled(Boolean queryFilterEnabled) {
        this.queryFilterEnabled = queryFilterEnabled;
    }

    public boolean isAutoValidate() {
        return autoValidate;
    }

    public void setAutoValidate(boolean autoValidate) {
        this.autoValidate = autoValidate;
    }

    public boolean isNarrow() {
        return !Strings.isEmpty(wideEntityName);
    }

    public String getQueryView() {
        return queryView;
    }

    public void setQueryView(String queryView) {
        this.queryView = queryView;
    }

    public boolean isLogical() {
        return logical;
    }

    public void setLogical(boolean logical) {
        this.logical = logical;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public void addGroupByExpr(String name, String expr) {
        groupByExprs.put(name, expr);
    }

    public Map<String, String> getGroupByExprs() {
        return groupByExprs;
    }

    public void setGroupByExprs(Map<String, String> groupByExprs) {
        this.groupByExprs = groupByExprs;
    }

    public void addSelectExpr(String name, String expr) {
        selectExprs.put(name, expr);
    }

    public Map<String, String> getSelectExprs() {
        return selectExprs;
    }

    public void setSelectExprs(Map<String, String> selectExprs) {
        this.selectExprs = selectExprs;
    }

    public void addOrderByExpr(String name, String expr) {
        orderByExprs.put(name, expr);
    }

    public Map<String, String> getOrderByExprs() {
        return orderByExprs;
    }

    public void setOrderByExprs(Map<String, String> orderByExprs) {
        this.orderByExprs = orderByExprs;
    }

    public void addFiltersExpr(String name, String expr) {
        filtersExprs.put(name, expr);
    }

    public Map<String, String> getFiltersExprs() {
        return filtersExprs;
    }

    public void setFiltersExprs(Map<String, String> filtersExprs) {
        this.filtersExprs = filtersExprs;
    }

    public void addAggregatesExpr(String name, String expr) {
        aggregatesExprs.put(name, expr);
    }

    public Map<String, String> getAggregatesExprs() {
        return aggregatesExprs;
    }

    public void setAggregatesExprs(Map<String, String> aggregatesExprs) {
        this.aggregatesExprs = aggregatesExprs;
    }

    public boolean mayBeJoinEntityOf(EntityMappingBuilder e1, EntityMappingBuilder e2) {
        List<FieldMappingBuilder> keyFields = getIdFieldMappings();
        if (null == keyFields || keyFields.isEmpty()) {
            return false;
        }

        final String[] idFields1 = e1.getIdFieldNames();
        final String[] idFields2 = e2.getIdFieldNames();

        if (keyFields.size() != idFields1.length + idFields2.length) {
            return false;
        }

        RelationMappingBuilder rm1 =
                findIdRelationByTargetFields(e1.getEntityName(), idFields1);
        if (null == rm1) {
            return false;
        }

        RelationMappingBuilder rm2 =
                findIdRelationByTargetFields(e2.getEntityName(), idFields2);
        if (null == rm2) {
            return false;
        }

        return true;
    }

    public List<FieldMappingBuilder> getFieldMappings() {
        return fieldMappings;
    }

    public FieldMappingBuilder findFieldMappingByName(String name) {
        for (FieldMappingBuilder fmb : getFieldMappings()) {
            if (Strings.equalsIgnoreCase(name, fmb.getFieldName())) {
                return fmb;
            }
        }
        return null;
    }

    public FieldMappingBuilder findFieldMappingByColumn(String column) {
        for (FieldMappingBuilder fmb : getFieldMappings()) {
            if (Strings.equalsIgnoreCase(column, fmb.getColumn().getName())) {
                return fmb;
            }
        }
        return null;
    }

    public FieldMappingBuilder findFieldMappingByMetaName(String name) {
        for (FieldMappingBuilder fmb : getFieldMappings()) {
            if (Strings.equalsIgnoreCase(name, fmb.getMetaFieldName())) {
                return fmb;
            }
            if (null != fmb.getReservedMetaFieldName() && Strings.equalsIgnoreCase(name, fmb.getReservedMetaFieldName().getFieldName())) {
                return fmb;
            }
        }
        return null;
    }

    public EntityMappingBuilder addFieldMapping(FieldMappingBuilder fm) {
        fieldMappings.add(fm);
        return this;
    }

    public boolean hasPrimaryKey() {
        for (FieldMappingBuilder fm : this.fieldMappings) {
            if (fm.isId()) {
                return true;
            }
        }
        return false;
    }

    public boolean isIdField(String name) {
        FieldMappingBuilder fm = findFieldMappingByName(name);
        return null != fm && fm.isId();
    }

    public String[] getIdFieldNames() {
        return getIdFieldMappings().stream().map(f -> f.getFieldName()).toArray(String[]::new);
    }

    public List<FieldMappingBuilder> getIdFieldMappings() {
        List<FieldMappingBuilder> list = New.arrayList();
        for (FieldMappingBuilder fm : this.fieldMappings) {
            if (fm.isId()) {
                list.add(fm);
            }
        }
        return list;
    }

    public InsertHandler getInsertHandler() {
        return insertHandler;
    }

    public EntityMappingBuilder setInsertHandler(InsertHandler insertHandler) {
        this.insertHandler = insertHandler;
        return this;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public EntityMappingBuilder setUpdateHandler(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
        return this;
    }

    public DeleteHandler getDeleteHandler() {
        return deleteHandler;
    }

    public EntityMappingBuilder setDeleteHandler(DeleteHandler deleteHandler) {
        this.deleteHandler = deleteHandler;
        return this;
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
        if (null == validators) {
            validators = new ArrayList<EntityValidator>();
        }
        return validators;
    }

    public EntityMappingBuilder setValidators(List<EntityValidator> validators) {
        this.validators = validators;
        return this;
    }

    public EntityMappingBuilder addValidator(EntityValidator validator) {
        getValidators().add(validator);
        return this;
    }

    public RelationMappingBuilder findIdRelationByTargetFields(String targetEntity, String... referencedFields) {
        return findRelationByTargetFields(targetEntity, referencedFields, (f) -> f.isId());
    }

    public RelationMappingBuilder findUniqueRelationByTargetFields(String uniqueName, String targetEntity, String... referencedFields) {
        final UniqueKeyBuilder key = getKey(uniqueName);
        if (null == key) {
            return null;
        }
        return findRelationByTargetFields(targetEntity, referencedFields, (f) -> key.containsField(f.getFieldName()));
    }

    protected RelationMappingBuilder findRelationByTargetFields(String targetEntity, String[] referencedFields, Predicate<FieldMappingBuilder> test) {
        for (RelationMappingBuilder rm : relationMappings) {
            if (!rm.getTargetEntityName().equalsIgnoreCase(targetEntity)) {
                continue;
            }
            if (rm.getJoinFields().size() != referencedFields.length) {
                continue;
            }
            boolean match = true;
            for (String name : referencedFields) {
                if (!rm.getJoinFields().stream().anyMatch((jf) -> jf.getReferencedFieldName().equalsIgnoreCase(name))) {
                    match = false;
                    break;
                }

                for (JoinFieldMappingBuilder jf : rm.getJoinFields()) {
                    FieldMappingBuilder fm = findFieldMappingByName(jf.getLocalFieldName());
                    if (null == fm || !test.test(fm)) {
                        match = false;
                        break;
                    }
                }
            }
            if (match) {
                return rm;
            }
        }
        return null;
    }

    public RelationMappingBuilder findSingleOrNullByTargetEntity(RelationType type, String targetEntityName) {
        List<RelationMappingBuilder> found = new ArrayList<>();
        for (RelationMappingBuilder rm : relationMappings) {
            if (rm.getType() == type && rm.getTargetEntityName().equalsIgnoreCase(targetEntityName)) {
                found.add(rm);
            }
        }
        if (found.size() == 1) {
            return found.get(0);
        } else {
            return null;
        }
    }

    public RelationMappingBuilder getRelationMapping(String name) {
        for (RelationMappingBuilder rm : relationMappings) {
            if (rm.getName().equalsIgnoreCase(name)) {
                return rm;
            }
        }
        return null;
    }

    public List<RelationMappingBuilder> getRelationMappings() {
        return relationMappings;
    }

    public EntityMappingBuilder addRelationMapping(RelationMappingBuilder relationMapping) {
        //check exists
        if (!Strings.isEmpty(relationMapping.getName()) && null != getRelationMapping(relationMapping.getName())) {
            throw new ObjectExistsException("The relation '" + relationMapping.getName() + "' already exists in entity '" + entity() + "'");
        }
        relationMappings.add(relationMapping);
        return this;
    }

    public RelationPropertyBuilder getRelationProperty(String name) {
        for (RelationPropertyBuilder rp : relationProperties) {
            if (rp.getName().equalsIgnoreCase(name)) {
                return rp;
            }
        }
        return null;
    }

    public List<RelationPropertyBuilder> getRelationProperties() {
        return relationProperties;
    }

    public EntityMappingBuilder addRelationProperty(RelationPropertyBuilder p) {
        if (null != getRelationProperty(p.getName())) {
            throw new ObjectExistsException("The relation property '" + p.getName() + "' already exists in entity '" + entity() + "'");
        }
        relationProperties.add(p);
        return this;
    }

    public List<UniqueKeyBuilder> getKeys() {
        return keys;
    }

    public UniqueKeyBuilder getKey(String name) {
        Optional<UniqueKeyBuilder> key =
                keys.stream().filter(k -> k.getName().equalsIgnoreCase(name)).findFirst();
        return key.isPresent() ? key.get() : null;
    }

    public EntityMappingBuilder addKey(UniqueKeyBuilder key) {
        if (null != getKey(key.getName())) {
            throw new ObjectExistsException("Unique key '" + key.getName() + "' already exists in entity '" + entity() + "'");
        }
        keys.add(key);
        return this;
    }

    public EntityListenersBuilder listeners() {
        return listeners;
    }

    public RemoteSettings getRemoteSettings() {
        return remoteSettings;
    }

    public void setRemoteSettings(RemoteSettings remoteSettings) {
        this.remoteSettings = remoteSettings;
    }

    public UnionSettings getUnionSettings() {
        return unionSettings;
    }

    public void setUnionSettings(UnionSettings unionSettings) {
        this.unionSettings = unionSettings;
    }

    private String entity() {
        return Strings.isEmpty(entityName) ? entityClass.getSimpleName() : entityName;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[entity=" + entityName + "]";
    }

    @Override
    public EntityMapping build() {
        Collections.sort(fieldMappings, Comparators.ORDERED_COMPARATOR);

        if(remote) {
            logical = true;
        }

        try {
            List<FieldMapping>    fields         = Builders.buildList(fieldMappings);
            List<RelationMapping> relations      = Builders.buildList(relationMappings);
            DbTable               table          = buildTable(fields, relations);
            DbTable               secondaryTable = buildSecondaryTable(fields, relations);

            EntityMapping em =
                    new EntityMapping(this,
                            entityName, wideEntityName, dynamicTableName, entityClass, extendedEntityClass,
                            table, secondaryTable, queryView, fields,
                            insertHandler, updateHandler, deleteHandler,
                            insertInterceptor, updateInterceptor, deleteInterceptor, findInterceptor,
                            modelClass, validators,
                            relations,
                            Builders.buildArray(relationProperties, new RelationProperty[0]),
                            autoCreateTable, queryFilterEnabled == null ? false : queryFilterEnabled, autoValidate,
                            logical, remote, remoteSettings, unionSettings,
                            groupByExprs, selectExprs, orderByExprs, filtersExprs, aggregatesExprs,
                            listeners.build());

            em.getExtensions().putAll(extensions);

            return em;
        } catch (RuntimeException e) {
            log.error("Error create entity mapping '" + entityName, e);
            throw e;
        }
    }

    public DbSchemaObjectName getTableSchemaObjectName() {
        return new DbSchemaObjectName(getTableCatalog(), getTableSchema(), getTableNameWithPrefix());
    }

    protected DbTable buildTable(List<FieldMapping> fields, List<RelationMapping> relations) {
        DbTableBuilder table = getTable();

        if (!Strings.isEmpty(tablePrefix)) {
            table.setName(getTableNameWithPrefix());
        }

        //columns
        for (FieldMapping fm : fields) {
            if (!fm.isSecondary()) {
                table.addColumn(fm.getColumn());
            }
        }

        //uniques
        for (UniqueKeyBuilder key : getKeys()) {
            DbIndexBuilder ix = new DbIndexBuilder();
            ix.setName("key_" + key.getName());
            ix.setUnique(true);
            for (String name : key.getFields()) {
                FieldMappingBuilder fm = findFieldMappingByName(name);
                if (null == fm) {
                    throw new IllegalStateException("No field '" + name +
                            "' exists, check unique '" + key.getName() + "' at entity " + entityName);
                }
                ix.addColumnName(fm.getColumnName());
            }
            if (!table.getIndexes().stream().anyMatch(ix1 -> ix1.matchUnique(ix))) {
                table.addIndex(ix);
            }
        }

        //todo: indexes

        return table.build();
    }

    protected DbTable buildSecondaryTable(List<FieldMapping> fields, List<RelationMapping> relations) {
        if (null == secondaryTable) {
            return null;
        }

        //foreign key
        DbForeignKeyBuilder fk = new DbForeignKeyBuilder();
        fk.setName("fk_" + Strings.lowerUnderscore(table.getName()) + "_secondary");
        fk.setForeignTable(table.getTableName());

        //columns
        for (FieldMapping fm : fields) {
            if (fm.isPrimaryKey()) {
                fk.addColumn(new DbForeignKeyColumn(fm.getColumnName(), fm.getColumnName()));
            }
            if (fm.isPrimaryKey() || fm.isSecondary()) {
                secondaryTable.addColumn(fm.getColumn());
            }
        }

        secondaryTable.addForeignKey(fk);

        return secondaryTable.build();
    }
}