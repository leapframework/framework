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
package leap.orm.sql.ast;

import leap.db.DbDialect;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.sql.Sql.Scope;

import java.io.IOException;

/**
 * Object name means : firstName.lastName or firstName.secondaryName.lastName or lastName
 */
public class SqlObjectName extends SqlObjectNameBase {

    protected EntityMapping entityMapping;
	protected FieldMapping  fieldMapping;
	protected SqlObjectName referenceTo;
	protected String        alias;
	
	public SqlObjectName() {

	}
	
	public SqlObjectName(Scope scope, String lastName) {
		this.scope    = scope;
		this.lastName = lastName;
	}

    public EntityMapping getEntityMapping() {
        return entityMapping;
    }

    public FieldMapping getFieldMapping() {
		return fieldMapping;
	}

	public void setFieldMapping(EntityMapping entityMapping, FieldMapping fieldMapping) {
        this.entityMapping = entityMapping;
		this.fieldMapping  = fieldMapping;
	}
	
	public SqlObjectName getReferenceTo() {
		return referenceTo;
	}

	public void setReferenceTo(SqlObjectName referenceTo) {
		this.referenceTo = referenceTo;
	}

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isField(){
		return null != fieldMapping;
	}
	
	@Override
    protected void toSql_(Appendable out) throws IOException {
		if(null != firstName){
			out.append(firstName).append('.');
		}
		
		if(null != secondaryName){
			out.append(secondaryName).append('.');
		}
		
		appendLastName(out);
    }

    protected void appendLastName(Appendable out) throws IOException {
        if (null != fieldMapping) {
            out.append(fieldMapping.getColumnName());
            return;
        }

        if (null != referenceTo) {
            referenceTo.appendLastName(out);
            return;
        }

        out.append(lastName);
    }

    
    @Override
    protected void appendLastName(Appendable out, DbDialect dialect) throws IOException {
        if (null != fieldMapping) {
            out.append(dialect.quoteIdentifier(fieldMapping.getColumnName(), true));
            return;
        }

        if (null != referenceTo) {
            referenceTo.appendLastName(out, dialect);
            return;
        }

        if(isField()){
            out.append(dialect.quoteIdentifier(lastName, true));
        }else{
            out.append(lastName);
        }
    }
}
