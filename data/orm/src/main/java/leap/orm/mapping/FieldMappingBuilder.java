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

import leap.core.el.EL;
import leap.core.metamodel.ReservedMetaFieldName;
import leap.db.model.DbColumnBuilder;
import leap.lang.*;
import leap.lang.beans.BeanProperty;
import leap.lang.expression.Expression;
import leap.lang.jdbc.JdbcTypes;
import leap.lang.meta.MType;
import leap.orm.annotation.Column;
import leap.orm.domain.Domain;
import leap.orm.generator.IdGenerator;
import leap.orm.generator.ValueGenerator;
import leap.orm.serialize.FieldSerializer;
import leap.orm.validation.FieldValidator;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class FieldMappingBuilder implements Buildable<FieldMapping>,Ordered {
	
	public static final float MIDDLE_SORT_ORDER = Column.ORDER_MIDDLE;
	public static final float LAST_SORT_ORDER   = Column.ORDER_LAST;

    protected String                fieldName;
    protected MType                 dataType;
    protected String                metaFieldName;
    protected Class<?>              javaType;
    protected BeanProperty          beanProperty;
    protected boolean               secondary;
    protected DbColumnBuilder       column;
    protected boolean               columnNameDeclared;
    protected String                sequenceName;
    protected IdGenerator           idGenerator;
    protected Boolean               nullable;
    protected Integer               maxLength;
    protected Integer               precision;
    protected Integer               scale;
    protected String                defaultValue;
    protected Expression            defaultValueExpression;
    protected Boolean               insert;
    protected Expression            insertValue;
    protected Boolean               update;
	protected Expression            updateIf;
    protected Expression            updateValue;
    protected Boolean				embedded;
    protected Boolean               filtered;
    protected Expression            filteredIf;
    protected Expression            filteredValue;
    protected boolean               optimisticLock;
    protected String                newOptimisticLockFieldName;
    protected Domain                domain;
    protected Annotation[]          annotations;
    protected List<FieldValidator>  validators;
    protected Float                 sortOrder;
    protected ReservedMetaFieldName reservedMetaFieldName;
    protected String                serializeFormat;
    protected FieldSerializer       serializer;
    protected boolean               hasPhysicalColumn;
    protected Boolean				filterable;
    protected Boolean				sortable;

    public FieldMappingBuilder(){
		this.column = new DbColumnBuilder();
	}
	
	public FieldMappingBuilder(String name,Class<?> type){
		this();
		setFieldName(name).
		setJavaType(type);
	}

    public FieldMappingBuilder(FieldMappingBuilder template) {
        this.fieldName = template.fieldName;
        this.dataType  = template.dataType;
        this.metaFieldName = template.metaFieldName;
        this.javaType = template.getJavaType();
        this.beanProperty = template.beanProperty;
        this.secondary = template.secondary;
        this.column = new DbColumnBuilder(template.column);
        this.columnNameDeclared = template.columnNameDeclared;
        this.sequenceName = template.sequenceName;
        this.idGenerator = template.idGenerator;
        this.nullable = template.nullable;
        this.maxLength = template.maxLength;
        this.precision = template.precision;
        this.scale = template.scale;
        this.defaultValue = template.defaultValue;
        this.defaultValueExpression = template.defaultValueExpression;
        this.insert = template.insert;
        this.update = template.update;
        this.insertValue = template.insertValue;
        this.updateIf = template.updateIf;
        this.updateValue = template.updateValue;
        this.filterable = template.filterable;
        this.sortable = template.sortable;
		this.filtered = template.filtered;
        this.filteredValue = template.filteredValue;
        this.optimisticLock = template.optimisticLock;
        this.newOptimisticLockFieldName = template.newOptimisticLockFieldName;
        this.domain = template.domain;
        this.annotations = template.annotations;
        this.validators = null == template.validators ? null : new ArrayList<>(template.validators);
        this.sortOrder = template.sortOrder;
        this.reservedMetaFieldName = template.reservedMetaFieldName;
    }

    public void mergeWithGlobalField(FieldMappingBuilder fm) {

        if(null != fm.dataType) {
            this.dataType = fm.dataType;
        }

        if(null != fm.metaFieldName) {
            this.metaFieldName = fm.metaFieldName;
        }

        if(null != fm.idGenerator) {
            this.idGenerator = fm.idGenerator;
        }

        if(null != fm.nullable) {
            this.nullable = fm.nullable;
        }

        if(null != fm.maxLength) {
            this.maxLength = fm.maxLength;
        }

        if(null != fm.precision) {
            this.precision = fm.precision;
        }

        if(null != fm.scale) {
            this.scale = fm.scale;
        }

        if(null != fm.defaultValue) {
            this.defaultValue = fm.defaultValue;
        }

        if(null != fm.defaultValueExpression) {
            this.defaultValueExpression = fm.defaultValueExpression;
        }

        if(null != fm.insert) {
            this.insert = fm.insert;
        }

        if(null != fm.update) {
            this.update = fm.update;
        }

        if(null != fm.filterable) {
        	this.filterable = fm.filterable;
		}

		if(null != fm.sortable) {
			this.sortable = fm.sortable;
		}

        if(null != fm.filtered) {
            this.filtered = fm.filtered;
        }

        if(null != fm.insertValue) {
            this.insertValue = fm.insertValue;
        }

        if (null != fm.updateIf) {
        	this.updateIf = fm.updateIf;
		}

        if(null != fm.updateValue) {
            this.updateValue = fm.updateValue;
        }

        if(null != fm.filteredValue) {
            this.filteredValue = fm.filteredValue;
        }

        if(null != fm.filteredIf) {
            this.filteredIf = fm.filteredIf;
        }

        if(null != fm.domain) {
            this.domain = fm.domain;
        }

        if(null != fm.validators) {
            this.validators = null == fm.validators ? null : new ArrayList<>(fm.validators);
        }

        if(null != fm.reservedMetaFieldName) {
            this.reservedMetaFieldName = fm.reservedMetaFieldName;
        }
    }

	public String getFieldName() {
		return fieldName;
	}

	public FieldMappingBuilder setFieldName(String fieldName) {
		this.fieldName = fieldName;
		return this;
	}
	
	public FieldMappingBuilder trySetFieldName(String fieldName){
		if(Strings.isEmpty(this.fieldName)){
			this.fieldName = fieldName;
		}
		return this;
	}

    public String getColumnName() {
        return column.getName();
    }

    public MType getDataType() {
		return dataType;
	}

	public FieldMappingBuilder setDataType(MType dataType) {
		this.dataType = dataType;
		return this;
	}
	
	public FieldMappingBuilder trySetDataType(MType dataType){
		if(null == this.dataType){
			this.dataType = dataType;
		}
		return this;
	}
	
	public String getMetaFieldName() {
		return metaFieldName;
	}

	public FieldMappingBuilder setMetaFieldName(String metaFieldName) {
		this.metaFieldName = metaFieldName;
		return this;
	}

	public FieldMappingBuilder trySetMetaFieldName(String metaFieldName) {
		if(null == this.metaFieldName) {
			this.metaFieldName = metaFieldName;	
		}
		return this;
	}
	
	public ReservedMetaFieldName getReservedMetaFieldName() {
		return reservedMetaFieldName;
	}

	public FieldMappingBuilder setReservedMetaFieldName(ReservedMetaFieldName reservedMetaFieldName) {
		this.reservedMetaFieldName = reservedMetaFieldName;
		return this;
	}

	public FieldMappingBuilder trySetReservedMetaFieldName(ReservedMetaFieldName reservedMetaFieldName) {
		if(null == this.reservedMetaFieldName) {
			this.reservedMetaFieldName = reservedMetaFieldName;	
		}
		return this;
	}

	public Class<?> getJavaType() {
		return null != javaType ? javaType : (null == beanProperty ? null : beanProperty.getType());
	}
	
	public FieldMappingBuilder setJavaType(Class<?> fieldType) {
		this.javaType = fieldType;
		return this;
	}
	
	public FieldMappingBuilder trySetFieldType(Class<?> fieldType){
		if(null == this.javaType){
			this.javaType = fieldType;
		}
		return this;
	}

	public BeanProperty getBeanProperty() {
		return beanProperty;
	}

	public FieldMappingBuilder setBeanProperty(BeanProperty beanProperty) {
		this.beanProperty = beanProperty;
		return this;
	}
	
	public Annotation[] getAnnotations(){
		return null != annotations ? annotations : (null == beanProperty ? Classes.EMPTY_ANNOTATION_ARRAY : beanProperty.getAnnotations());
	}
	
	public FieldMappingBuilder setAnnotations(Annotation[] annotations) {
		this.annotations = annotations;
		return this;
	}

    public boolean isSecondary() {
        return secondary;
    }

    public FieldMappingBuilder setSecondary(boolean secondary) {
        this.secondary = secondary;
        return this;
    }

    public DbColumnBuilder getColumn() {
		return column;
	}

	public FieldMappingBuilder setColumn(DbColumnBuilder column) {
		this.column = column;
		return this;
	}

	public boolean isColumnNameDeclared() {
		return columnNameDeclared;
	}

	public FieldMappingBuilder setColumnNameDeclared(boolean columnNameDeclared) {
		this.columnNameDeclared = columnNameDeclared;
		return this;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public FieldMappingBuilder setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
		return this;
	}
	
	public FieldMappingBuilder trySetSequenceName(String sequenceName){
		if(Strings.isEmpty(this.sequenceName)){
			this.sequenceName = sequenceName;
		}
		return this;
	}
	
	public IdGenerator getIdGenerator() {
		return idGenerator;
	}

	public FieldMappingBuilder setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
		return this;
	}
	
	public FieldMappingBuilder trySetIdGenerator(IdGenerator idGenerator){
		if(null == this.idGenerator){
			this.idGenerator = idGenerator;
		}
		return this;
	}
	
	public Expression getDefaultValueExpression() {
		return defaultValueExpression;
	}

	public FieldMappingBuilder setDefaultValueExpression(Expression defaultValue) {
		this.defaultValue			= null;
		this.defaultValueExpression = defaultValue;
		return this;
	}
	
	public FieldMappingBuilder trySetDefaultValueExpression(Expression defaultValue){
		if(Strings.isEmpty(this.defaultValue) && null == this.defaultValueExpression){
			this.defaultValueExpression = defaultValue;
		}
		return this;
	}

	public Expression getInsertValue() {
		return insertValue;
	}

	public FieldMappingBuilder setInsertValue(Expression insertValue) {
		this.insertValue = insertValue;
		return this;
	}
	
	public FieldMappingBuilder trySetInsertValue(Expression insertValue){
		if(null == this.insertValue){
			this.insertValue = insertValue;
		}
		return this;
	}

	public Expression getUpdateIf() {
		return updateIf;
	}

	public FieldMappingBuilder setUpdateIf(Expression updateIf) {
		this.updateIf = updateIf;
		return this;
	}

	public FieldMappingBuilder trySetUpdateIf(Expression updateIf) {
    	if (null == this.updateIf) {
			this.updateIf = updateIf;
		}
		return this;
	}

	public Expression getUpdateValue() {
		return updateValue;
	}

	public FieldMappingBuilder setUpdateValue(Expression updateValue) {
		this.updateValue = updateValue;
		return this;
	}
	
	public FieldMappingBuilder trySetUpdateValue(Expression updateValue){
		if(null == this.updateValue){
			this.updateValue = updateValue;
		}
		return this;
	}

	public FieldMappingBuilder trySetFilterable(Boolean filterable) {
    	if(null == this.filterable) {
            this.filterable = filterable;
		}
		return this;
	}

    public FieldMappingBuilder trySetSortable(Boolean sortable) {
        if(null == this.sortable) {
            this.sortable = sortable;
        }
        return this;
    }

    public Expression getFilteredValue() {
        return filteredValue;
    }

    public FieldMappingBuilder setFilteredValue(Expression v) {
        this.filteredValue = v;
        return this;
    }

    public FieldMappingBuilder trySetFilteredValue(Expression v) {
        if(null == this.filteredValue) {
            this.filteredValue = v;
        }
        return this;
    }

    public Expression getFilteredIf() {
        return filteredIf;
    }

    public FieldMappingBuilder setFilteredIf(Expression filteredIf) {
        this.filteredIf = filteredIf;
        return this;
    }

    public FieldMappingBuilder trySetFilteredIf(Expression expr) {
        if(null == this.filteredIf) {
            this.filteredIf = expr;
        }
        return this;
    }

    public FieldMappingBuilder setValueGenerator(ValueGenerator valueGenerator){
		return setInsertValue(valueGenerator).setUpdateValue(valueGenerator);
	}
	
	public FieldMappingBuilder trySetValueGenerator(ValueGenerator valueGenerator){
		return trySetInsertValue(valueGenerator).trySetUpdateValue(valueGenerator);
	}
	
	public boolean isNullable(){
		return null == nullable ? false : nullable;
	}
	
	public Boolean getNullable() {
		return nullable;
	}

	public FieldMappingBuilder setNullable(Boolean nullable) {
		this.nullable = nullable;
		return this;
	}
	
	public FieldMappingBuilder trySetNullable(Boolean nullable){
		if(null == this.nullable){
			this.nullable = nullable;
		}
		return this;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public FieldMappingBuilder setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
		return this;
	}
	
	public FieldMappingBuilder trySetMaxLength(Integer maxLength){
		if(null == this.maxLength){
			this.maxLength = maxLength;
		}
		return this;
	}

	public Integer getPrecision() {
		return precision;
	}

	public FieldMappingBuilder setPrecision(Integer precision) {
		this.precision = precision;
		return this;
	}
	
	public FieldMappingBuilder trySetPrecision(Integer precision){
		if(null == this.precision){
			this.precision = precision; 
		}
		return this;
	}

	public Integer getScale() {
		return scale;
	}

	public FieldMappingBuilder setScale(Integer scale) {
		this.scale = scale;
		return this;
	}
	
	public FieldMappingBuilder trySetScale(Integer scale) {
		if(null == this.scale){
			this.scale = scale;
		}
		return this;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}

	public FieldMappingBuilder setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		this.defaultValueExpression = null;
		return this;
	}
	
	public FieldMappingBuilder trySetDefaultValue(String defaultValue){
		if(Strings.isEmpty(this.defaultValue) && null == this.defaultValueExpression){
			this.defaultValue = defaultValue;
		}
		return this;
	}

	public Boolean getInsert(){
		return insert;
	}

	public FieldMappingBuilder setInsert(Boolean insert) {
		this.insert = insert;
		return this;
	}
	
	public FieldMappingBuilder trySetInsert(Boolean insert){
		if(null == this.insert){
			this.insert = insert;
		}
		return this;
	}

	public Boolean getUpdate(){
		return update;
	}

	public FieldMappingBuilder setUpdate(Boolean update) {
		this.update = update;
		return this;
	}
	
	public FieldMappingBuilder trySetUpdate(Boolean update){
		if(null == this.update){
			this.update = update;
		}
		return this;
	}

	public Boolean getEmbedded() {
		return embedded;
	}

	public void setEmbedded(Boolean embedded) {
		this.embedded = embedded;
	}

	public boolean getFiltered() {
        return null != filtered && filtered;
    }

    public FieldMappingBuilder setFiltered(Boolean b) {
        this.filtered = b;
        return this;
    }

    public FieldMappingBuilder trySetFiltered(Boolean b){
        if(null == this.filtered){
            this.filtered = b;
        }
        return this;
    }

	public boolean isId(){
		return null != column && column.isPrimaryKey();
	}
	
	public boolean isOptimisticLock() {
		return optimisticLock;
	}

	public FieldMappingBuilder setOptimisticLock(boolean optimisticLock) {
		this.optimisticLock = optimisticLock;
		return this;
	}
	
	public String getNewOptimisticLockFieldName() {
		return newOptimisticLockFieldName;
	}

	public FieldMappingBuilder setNewOptimisticLockFieldName(String newOptimisticLockFieldName) {
		this.newOptimisticLockFieldName = newOptimisticLockFieldName;
		return this;
	}

	public Domain getDomain() {
		return domain;
	}

	public FieldMappingBuilder setDomain(Domain domain) {
		this.domain = domain;
		return this;
	}
	
	public List<FieldValidator> getValidators() {
		if(null == validators){
			validators = new ArrayList<FieldValidator>();
		}
		return validators;
	}

	public FieldMappingBuilder setValidators(List<FieldValidator> validators) {
		this.validators = validators;
		return this;
	}
	
	public FieldMappingBuilder addValidator(FieldValidator validator){
		getValidators().add(validator);
		return this;
	}
	
	public float getSortOrder() {
		if(null == sortOrder){
			if(null != column && column.isPrimaryKey()){
				sortOrder = MINIMUM_SORT_ORDER;
			}else{
				sortOrder = MIDDLE_SORT_ORDER;
			}
		}
		return sortOrder;
	}
	
	public FieldMappingBuilder setSortOrder(Float sortOrder) {
		this.sortOrder = sortOrder;
		return this;
	}
	
	public FieldMappingBuilder trySetSortOrder(Float sortOrder){
		if(null == this.sortOrder){
			this.sortOrder = sortOrder;
		}
		return this;
	}

    public String getSerializeFormat() {
        return serializeFormat;
    }

    public void setSerializeFormat(String serializeFormat) {
        this.serializeFormat = serializeFormat;
    }

    public FieldSerializer getSerializer() {
        return serializer;
    }

    public void setSerializer(FieldSerializer serializer) {
        this.serializer = serializer;
    }

    public boolean isHasPhysicalColumn() {
        return hasPhysicalColumn;
    }

    public void setHasPhysicalColumn(boolean hasPhysicalColumn) {
        this.hasPhysicalColumn = hasPhysicalColumn;
    }

    public boolean isFilterable() {
        return null == filterable ? false : filterable;
    }

	public Boolean getFilterable() {
		return filterable;
	}

	public FieldMappingBuilder setFilterable(boolean filterable) {
		this.filterable = filterable;
		return this;
	}

	public boolean isSortable() {
        return null == sortable ? false : sortable;
    }

	public Boolean getSortable() {
		return sortable;
	}

	public FieldMappingBuilder setSortable(boolean sortable) {
		this.sortable = sortable;
		return this;
	}

	@Override
    public FieldMapping build() {
		if(null == javaType){
			javaType = null != beanProperty ? beanProperty.getType() : JdbcTypes.forTypeCode(column.getTypeCode()).getDefaultReadType();
		}

        if(column.isAutoIncrement()) {
            insert = false;
        }
		
		if(null == nullable){
			nullable = true;
		}
		
		if(null == insert){
			insert = true;
		}
		
		if(null == update){
			update = column.isPrimaryKey() ? false : true;
		}

		if(null == embedded) {
			embedded = false;
		}

        if(null == filtered) {
            filtered = false;
        }
		
		if(null == defaultValueExpression){
			defaultValueExpression = EL.tryCreateValueExpression(defaultValue, javaType);
		}
		
		if(null != reservedMetaFieldName && null == metaFieldName) {
			this.metaFieldName = reservedMetaFieldName.getFieldName();
		}

		if(null != column && column.isPrimaryKey()) {
			filterable = true;
		}

	    return new FieldMapping(fieldName,
	    						dataType,
	    						metaFieldName,
	    						javaType,
	    						beanProperty, secondary, column.build(), sequenceName,
	    						nullable,maxLength,precision,scale,
                                insert, update, embedded, filtered, filteredIf,
                                defaultValueExpression,
                                insertValue, updateIf, updateValue, filteredValue,
	    						optimisticLock,newOptimisticLockFieldName,
	    						domain,validators,
	    						reservedMetaFieldName,
                                serializer, isFilterable(), isSortable());
    }

	@Override
    public String toString() {
	    return this.getClass().getSimpleName() + "[field=" + fieldName + "]";
    }
	
}