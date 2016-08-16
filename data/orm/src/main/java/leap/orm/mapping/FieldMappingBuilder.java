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
import leap.lang.Buildable;
import leap.lang.Classes;
import leap.lang.Ordered;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;
import leap.lang.expression.Expression;
import leap.lang.jdbc.JdbcTypes;
import leap.lang.meta.MType;
import leap.lang.tostring.ToStringBuilder;
import leap.orm.annotation.Column;
import leap.orm.domain.FieldDomain;
import leap.orm.generator.IdGenerator;
import leap.orm.generator.ValueGenerator;
import leap.orm.serialize.FieldSerializer;
import leap.orm.validation.FieldValidator;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class FieldMappingBuilder implements Buildable<FieldMapping>,Ordered {
	
	public static final int MIDDLE_SORT_ORDER = Column.ORDER_MIDDLE;
	public static final int LAST_SORT_ORDER   = Column.ORDER_LAST;

    protected String                fieldName;
    protected MType                 dataType;
    protected String                metaFieldName;
    protected Class<?>              javaType;
    protected BeanProperty          beanProperty;
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
    protected Boolean               update;
    protected Boolean               where;
    protected Expression            insertValue;
    protected Expression            updateValue;
    protected Expression            whereValue;
    protected Expression            whereIf;
    protected boolean               optimisticLock;
    protected String                newOptimisticLockFieldName;
    protected FieldDomain           domain;
    protected Annotation[]          annotations;
    protected List<FieldValidator>  validators;
    protected Integer               sortOrder;
    protected ReservedMetaFieldName reservedMetaFieldName;
    protected boolean               sharding;
    protected String                serializeFormat;
    protected FieldSerializer       serializer;

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
        this.where = template.where;
        this.insertValue = template.insertValue;
        this.updateValue = template.updateValue;
        this.whereValue = template.whereValue;
        this.whereIf = template.whereIf;
        this.optimisticLock = template.optimisticLock;
        this.newOptimisticLockFieldName = template.newOptimisticLockFieldName;
        this.domain = template.domain;
        this.annotations = template.annotations;
        this.validators = null == template.validators ? null : new ArrayList<>(template.validators);
        this.sortOrder = template.sortOrder;
        this.reservedMetaFieldName = template.reservedMetaFieldName;
    }

    public void mergedWithGlobalField(FieldMappingBuilder fm) {

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

        if(null != fm.where) {
            this.where = fm.where;
        }

        if(null != fm.insertValue) {
            this.insertValue = fm.insertValue;
        }

        if(null != fm.updateValue) {
            this.updateValue = fm.updateValue;
        }

        if(null != fm.whereValue) {
            this.whereValue = fm.whereValue;
        }

        if(null != fm.whereIf) {
            this.whereIf = fm.whereIf;
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
	
	public FieldMappingBuilder trySetBeanProperty(BeanProperty beanProperty) {
		if(null == this.beanProperty){
			this.beanProperty = beanProperty;
		}
		return this;
	}
	
	public Annotation[] getAnnotations(){
		return null != annotations ? annotations : (null == beanProperty ? Classes.EMPTY_ANNOTATION_ARRAY : beanProperty.getAnnotations());
	}
	
	public FieldMappingBuilder setAnnotations(Annotation[] annotations) {
		this.annotations = annotations;
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

    public Expression getWhereValue() {
        return whereValue;
    }

    public FieldMappingBuilder setWhereValue(Expression v) {
        this.whereValue = v;
        return this;
    }

    public FieldMappingBuilder setWhereIf(Expression e) {
        this.whereIf = e;
        return this;
    }

	public FieldMappingBuilder setValueGenerator(ValueGenerator valueGenerator){
		return setInsertValue(valueGenerator).setUpdateValue(valueGenerator);
	}
	
	public FieldMappingBuilder trySetValueGenerator(ValueGenerator valueGenerator){
		return trySetInsertValue(valueGenerator).trySetUpdateValue(valueGenerator);
	}
	
	public boolean isNullable(){
		return null == nullable || nullable;
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

    public boolean isWhere() {
        return null != where && where;
    }

    public Boolean getWhere(){
        return where;
    }

    public FieldMappingBuilder setWhere(Boolean b) {
        this.where = b;
        return this;
    }

    public FieldMappingBuilder trySetWhere(Boolean b){
        if(null == this.where){
            this.where = b;
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

	public FieldDomain getDomain() {
		return domain;
	}

	public FieldMappingBuilder setDomain(FieldDomain domain) {
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
	
	public int getSortOrder() {
		if(null == sortOrder){
			if(null != column && column.isPrimaryKey()){
				sortOrder = MINIMUM_SORT_ORDER;
			}else{
				sortOrder = MIDDLE_SORT_ORDER;
			}
		}
		return sortOrder;
	}
	
	public FieldMappingBuilder setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
		return this;
	}
	
	public FieldMappingBuilder trySetSortOrder(Integer sortOrder){
		if(null == this.sortOrder){
			this.sortOrder = sortOrder;
		}
		return this;
	}

    public boolean isSharding() {
        return sharding;
    }

    public FieldMappingBuilder setSharding(boolean sharding) {
        this.sharding = sharding;
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

    @Override
    public FieldMapping build() {
		if(null == javaType){
			javaType = null != beanProperty ? beanProperty.getType() : JdbcTypes.forTypeCode(column.getTypeCode()).getDefaultReadType();
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

        if(null == where) {
            where = false;
        }
		
		if(null == defaultValueExpression){
			defaultValueExpression = EL.tryCreateValueExpression(defaultValue, javaType);
		}
		
		if(null != reservedMetaFieldName && null == metaFieldName) {
			this.metaFieldName = reservedMetaFieldName.getFieldName();
		}

	    return new FieldMapping(fieldName,
	    						dataType,
	    						metaFieldName,
	    						javaType,
	    						beanProperty, column.build(), sequenceName,
	    						nullable,maxLength,precision,scale,
                                insert, update, where,
                                defaultValueExpression,
                                insertValue, updateValue, whereValue, whereIf,
	    						optimisticLock,newOptimisticLockFieldName,
	    						domain,validators,
	    						reservedMetaFieldName,
                                sharding, serializer);
    }

	@Override
    public String toString() {
	    return new ToStringBuilder(this).append("fieldName", fieldName).toString();
    }
	
}