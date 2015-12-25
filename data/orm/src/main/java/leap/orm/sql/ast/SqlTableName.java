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

import java.io.IOException;

import leap.db.DbDialect;
import leap.orm.mapping.EntityMapping;

public class SqlTableName extends SqlObjectNameBase implements SqlTableSource {

	private String 		  alias;
	private EntityMapping entityMapping;
	
	public SqlTableName() {
		
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public EntityMapping getEntityMapping() {
		return entityMapping;
	}

	public void setEntityMapping(EntityMapping entityMapping) {
		this.entityMapping = entityMapping;
	}
	
	public boolean isEntity(){
		return null != entityMapping;
	}
	
	@Override
    protected void toSql_(Appendable out) throws IOException {
		if(null != firstName){
			out.append(firstName).append('.');
		}
		
		if(null != secondaryName){
			out.append(secondaryName).append('.');
		}
		
		if(null != entityMapping){
			out.append(entityMapping.getTableName());
		}else{
			out.append(lastName);
		}
    }

    @Override
    protected void appendLastName(Appendable buf, DbDialect dialect) throws IOException {
        if(null != entityMapping) {
            buf.append(dialect.quoteIdentifier(entityMapping.getTableName(), true));
        }else{
            buf.append(dialect.quoteIdentifier(lastName, true));    
        }
    }
}