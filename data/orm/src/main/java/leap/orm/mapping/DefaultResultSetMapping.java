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

import leap.lang.Strings;
import leap.orm.OrmConfig;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.sql.Sql;
import leap.orm.sql.SqlContext;
import leap.orm.sql.ast.SqlObjectName;
import leap.orm.sql.ast.SqlSelect;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class DefaultResultSetMapping implements ResultSetMapping {

	protected final OrmConfig     ormConfig;
	protected final OrmMetadata   metadata;
	protected final EntityMapping primaryEntityMapping;
	
	protected int columnCount;
	protected ResultColumnMapping[] columnMappings;

	public DefaultResultSetMapping(OrmContext context, SqlContext sqlContext, ResultSet rs, EntityMapping primaryEntityMapping) throws SQLException {
		this.ormConfig            = context.getConfig();
		this.metadata             = context.getMetadata();
		this.primaryEntityMapping = primaryEntityMapping;
		
		this.mapping(rs,sqlContext);
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

	protected void mapping(ResultSet rs, SqlContext ctx) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		
		this.columnCount    = md.getColumnCount();
		this.columnMappings = new ResultColumnMapping[this.columnCount];
		SqlSelect selectCmd = null;
        Sql sql = ctx.getQuerySql();
		if(null != sql && sql.isSelect() && sql.nodes()[0] instanceof SqlSelect) {
            selectCmd = (SqlSelect) sql.nodes()[0];
		}
		List<SqlObjectName> objectNames = selectCmd.getSqlObjectNames();
		for(int i=1;i<=this.columnCount;i++){
			ResultColumnMapping cm = new ResultColumnMapping();
			
			cm.setColumnName(md.getColumnName(i));
			cm.setColumnLabel(md.getColumnLabel(i));
			cm.setColumnType(md.getColumnType(i));

			if(null != selectCmd && selectCmd.isSelectItemAlias(cm.getColumnLabel())){
                cm.setAliasName(selectCmd.getSelectItemAlias(cm.getColumnLabel()));
                cm.setResultName(cm.getAliasName());
                cm.setNormalizedName(normalizeName(cm.getAliasName()));
			}
			EntityMapping em = null;
			FieldMapping fm = null;
			if (ormConfig.isConvertFieldForJoin() && i <= objectNames.size()) {
				SqlObjectName objectName = objectNames.get(i-1);
				fm = objectName.getFieldMapping();
				if (null != fm && Strings.equals(fm.getColumnName(), cm.getColumnLabel())) {
					em = objectName.getEntityMapping();
				}
			}

			if (null == em) {
				em = primaryEntityMapping;
				fm = em.tryGetFieldMappingByColumn(cm.getColumnLabel());
			}

			if(null != fm){
				cm.setEntityMapping(em);
				cm.setFieldMapping(fm);
                if(null == cm.getResultName()) {
					cm.setResultName(fm.getFieldName());
					cm.setNormalizedName(fm.getFieldName());
                }
			}else {
                if(null == cm.getResultName()) {
                    String name = Strings.firstNotEmpty(cm.getColumnLabel(), cm.getColumnName());
                    for(int j=0;j<name.length();j++) {
                        char c = name.charAt(j);
                        if(Character.isLetter(c)) {
                            if(Character.isUpperCase(c)) {
                                name = name.toLowerCase();
                            }
                            break;
                        }
                    }
					cm.setResultName(normalizeName(name));
					cm.setNormalizedName(cm.getResultName());
                }
            }

			columnMappings[i-1] = cm;
		}
	}

    protected String normalizeName(String name) {
        return Strings.lowerCamel(name, '_');
    }
}