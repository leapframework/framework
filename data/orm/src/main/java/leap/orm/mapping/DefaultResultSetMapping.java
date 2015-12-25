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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import leap.lang.Strings;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;

public class DefaultResultSetMapping implements ResultSetMapping {
	
	protected final OrmMetadata   metadata;
	protected final EntityMapping primaryEntityMapping;
	
	protected int columnCount;
	protected ResultColumnMapping[] columnMappings;

	public DefaultResultSetMapping(OrmContext context,ResultSet rs,EntityMapping primaryEntityMapping) throws SQLException {
		this.metadata             = context.getMetadata();
		this.primaryEntityMapping = primaryEntityMapping;
		
		this.mapping(rs);
	}
	
	@Override
    public EntityMapping getPrimaryEntityMapping() {
	    return primaryEntityMapping;
    }

	@Override
    public int getColumnCount() {
	    return columnCount;
    }
	
	@Override
    public ResultColumnMapping getColumnMapping(int index) {
	    return columnMappings[index];
    }

	protected void mapping(ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		
		this.columnCount    = md.getColumnCount();
		this.columnMappings = new ResultColumnMapping[this.columnCount];
		
		for(int i=1;i<=this.columnCount;i++){
			ResultColumnMapping cm = new ResultColumnMapping();
			
			cm.setColumnName(md.getColumnName(i));
			cm.setColumnLabel(md.getColumnLabel(i));
			cm.setColumnType(md.getColumnType(i));
			
			String primaryTableName = primaryEntityMapping.getTableName();
			String columnTableName  = md.getTableName(i);
			
			if(Strings.isEmpty(columnTableName) || primaryTableName.equalsIgnoreCase(columnTableName)){
				FieldMapping fm = primaryEntityMapping.tryGetFieldMappingByColumn(cm.getColumnName());
				
				if(null != fm){
					cm.setEntityMapping(primaryEntityMapping);
					cm.setFieldMapping(fm);
				}
			}
			
			columnMappings[i-1] = cm; 
		}
	}
}