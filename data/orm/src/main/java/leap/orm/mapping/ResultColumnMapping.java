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

public class ResultColumnMapping {
	
	protected String        columnName;
	protected String        columnLabel;
	protected String		aliasName;
	protected int           columnType;
    protected String        resultName;
    protected String        normalizedName; //the normalized name of result name.
    protected EntityMapping	entityMapping;
    protected FieldMapping  fieldMapping;

	protected ResultColumnMapping() {

	}

	public String getColumnName() {
		return columnName;
	}

	protected void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnLabel() {
		return columnLabel;
	}

	protected void setColumnLabel(String columnLabel) {
		this.columnLabel = columnLabel;
	}

	public int getColumnType() {
		return columnType;
	}

	protected void setColumnType(int columnType) {
		this.columnType = columnType;
	}
	
	public EntityMapping getEntityMapping() {
		return entityMapping;
	}

	protected void setEntityMapping(EntityMapping entityMapping) {
		this.entityMapping = entityMapping;
	}

	public FieldMapping getFieldMapping() {
		return fieldMapping;
	}

	protected void setFieldMapping(FieldMapping fieldMapping) {
		this.fieldMapping = fieldMapping;
	}

    public String getResultName() {
        return resultName;
    }

    public void setResultName(String resultName) {
        this.resultName = resultName;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}
}