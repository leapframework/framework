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
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.annotation.Nullable;
import leap.lang.beans.BeanProperty;
import leap.lang.expression.Expression;
import leap.lang.meta.MType;
import leap.orm.domain.FieldDomain;
import leap.orm.validation.FieldValidator;

import java.util.List;

public class FieldMapping {
	
	protected final String		     fieldName;
	protected final MType            dataType;
	protected final String			 metaFieldName;
	protected final Class<?>         javaType;
	protected final BeanProperty     beanProperty;
	protected final DbColumn         column;
	protected final String		     sequenceName;
	protected final boolean		     nullable;
	protected final Integer          maxLength;
	protected final Integer          precision;
	protected final Integer          scale;
	protected final boolean		     insert;
	protected final boolean		     update;
    protected final boolean          delete;
    protected final boolean          select;
	protected final Expression       defaultValue;
	protected final Expression	     insertValue;
	protected final Expression	     updateValue;
    protected final Expression       deleteValue;
    protected final Expression       selectValue;
	protected final FieldDomain      domain;
	protected final boolean		     optimisticLock;
	protected final String		     newOptimisticLockFieldName;
	protected final FieldValidator[] validators;
	
	protected final ReservedMetaFieldName reservedMetaFieldName;
	
	public FieldMapping(String fieldName,
						MType dataType,
						String metaFieldName,
					    Class<?> javaType,
						BeanProperty beanProperty,
						DbColumn column,
						String sequenceName,
						boolean nullable,
						Integer maxLength,Integer presison,Integer scale,
						boolean insert,boolean update,boolean delete, boolean select,
						Expression defaultValue,
						Expression insertValue,
						Expression updateValue,
                        Expression deleteValue,
                        Expression selectValue,
						boolean optimisticLock,
						String newOptimisticLockFieldName,
						FieldDomain domain,
						List<FieldValidator> validators,
						ReservedMetaFieldName reservedMetaFieldName) {
		
		Args.notEmpty(fieldName,"field name");
		Args.notNull(javaType,"java type");
		Args.notNull(column,"column");
		
		this.fieldName		= fieldName;
		this.dataType		= dataType;
		this.metaFieldName  = metaFieldName;
		this.javaType       = javaType;
	    this.beanProperty   = beanProperty;
	    this.column         = column;
	    this.sequenceName   = sequenceName;
	    this.nullable		= nullable;
	    this.maxLength		= maxLength;
	    this.precision		= presison;
	    this.scale		    = scale;
	    this.insert         = insert;
	    this.update         = update;
        this.delete         = delete;
        this.select         = select;
	    this.defaultValue   = defaultValue;
	    this.insertValue    = insertValue;
	    this.updateValue    = updateValue;
        this.deleteValue    = deleteValue;
        this.selectValue    = selectValue;
	    this.optimisticLock = optimisticLock;
	    this.newOptimisticLockFieldName = newOptimisticLockFieldName;
	    this.domain         = domain;
	    this.validators     = null == validators ? new FieldValidator[]{} : validators.toArray(new FieldValidator[validators.size()]);
	    
	    if(optimisticLock){
	    	Args.notEmpty(newOptimisticLockFieldName);
	    	Args.assertFalse(newOptimisticLockFieldName.equalsIgnoreCase(getFieldName()),
	    				     "newOptimisticLockFieldName must not equals the field name");
	    }
	    
	    this.reservedMetaFieldName = reservedMetaFieldName;
    }
	
	public String getFieldName(){
		return fieldName;
	}
	
	public MType getDataType() {
		return dataType;
	}
	
	public String getMetaFieldName() {
		return metaFieldName;
	}

	public ReservedMetaFieldName getReservedMetaFieldName() {
		return reservedMetaFieldName;
	}
	
	public boolean isReservedField() {
		return null != reservedMetaFieldName;
	}

	public Class<?> getJavaType() {
		return javaType;
	}
	
	@Nullable
	public BeanProperty getBeanProperty() {
		return beanProperty;
	}
	
	public String getColumnName(){
		return column.getName();
	}
	
	@Nullable
	public String getSequenceName() {
		return sequenceName;
	}
	
	public Expression getDefaultValue() {
		return defaultValue;
	}

	public Expression getInsertValue() {
		return insertValue;
	}

	public Expression getUpdateValue() {
		return updateValue;
	}

    public Expression getDeleteValue() {
        return deleteValue;
    }

    public Expression getSelectValue() {
        return selectValue;
    }

    public boolean isAutoGenerateValue(){
		return null != insertValue || column.isAutoIncrement() || !Strings.isEmpty(sequenceName);
	}

	public DbColumn getColumn() {
		return column;
	}
	
	public boolean isInsert() {
		return insert;
	}

	public boolean isUpdate() {
		return update;
	}

    public boolean isDelete() {
        return delete;
    }

    public boolean isSelect() {
        return select;
    }

    public boolean isPrimaryKey() {
		return column.isPrimaryKey();
	}
	
	public boolean isNullable(){
		return this.nullable;
	}
	
	public Integer getMaxLength() {
		return maxLength;
	}

	public Integer getPrecision() {
		return precision;
	}

	public Integer getScale() {
		return scale;
	}

	public boolean isOptimisticLock() {
		return optimisticLock;
	}
	
	public String getNewOptimisticLockFieldName() {
		return newOptimisticLockFieldName;
	}

	@Nullable
	public FieldDomain getDomain() {
		return domain;
	}

	public FieldValidator[] getValidators() {
		return validators;
	}

	@Override
    public String toString() {
	    return "FieldMapping[name=" + getFieldName() + ",column=" + getColumnName() + ",dataType=" + dataType + "]";
    }
}