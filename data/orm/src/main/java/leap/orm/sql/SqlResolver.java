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
package leap.orm.sql;

import leap.lang.Strings;
import leap.lang.collection.SimpleCaseInsensitiveMap;
import leap.orm.OrmMetadata;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.metadata.MetadataContext;
import leap.orm.sql.Sql.Scope;
import leap.orm.sql.ast.*;

import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SqlResolver {

	protected final MetadataContext context;
	protected final OrmMetadata		metadata;
	protected final Sql				sql;
	
	private Stack<SqlSelect> selects = new Stack<>();
	
	public SqlResolver(MetadataContext context,Sql sql){
		this.context  = context;
		this.metadata = context.getMetadata();
		this.sql	  = sql;
	}
	
	public Sql resolve(){
		resolve(new Stack<>(), sql.nodes());
		return sql;
	}
	
	protected void resolve(Stack<Tables> tablesStack,AstNode[] nodes){
		for(int i=0;i<nodes.length;i++){
			AstNode node = nodes[i];
			resolve(tablesStack, node);
		}
	}
	
	protected void resolve(Stack<Tables> tablesStack,AstNode node){
		if(node instanceof SqlTableContainer){
			SqlTableContainer statement = (SqlTableContainer)node;
			
			if(node instanceof SqlSelect) {
				SqlSelect select = (SqlSelect)node;
				selects.add(select);
			}
			
			tablesStack.add(new Tables(metadata,statement.getTableSources()));
			resolve(tablesStack, statement.getNodes());	
			tablesStack.pop();
			
			if(node instanceof SqlSelect) {
				selects.pop();
			}
			
			return;
		}
		
		if(node instanceof SqlNodeContainer) {
			resolve(tablesStack,((SqlNodeContainer)node).getNodes());
			return;
		}

		if(node instanceof DynamicClause){
			resolve(tablesStack,((DynamicClause)node).getNodes());
			return;
		}

		if(!tablesStack.isEmpty()){
            if(node instanceof SqlObjectName) {
                resolveColumn(tablesStack, (SqlObjectName)node);
            }
		}
	}
	
	protected void resolveColumn(Stack<Tables> tablesStack,SqlObjectName name){
		//we don't resolve object name like "firstName.secondaryName.lastName"
		if(null == name.getSecondaryName()){
			if(null != name.getFirstName()){
				for(int i=tablesStack.size()-1;i>=0;i--){
					Tables tables = tablesStack.get(i);
					
					SqlTableSource table = tables.get(name.getFirstName());
					if(null != table){
						resolveColumnFromTableSource(table, name);
						break;
					}
					
					if(name.getScope() == Sql.Scope.SELECT_LIST || name.getScope() == Sql.Scope.ORDER_BY){
						break;
					}
				}
			}else{
				//TODO : group by , having ...
				if(name.getScope() == Sql.Scope.ORDER_BY) {
					SqlSelect select = currentSelectQuery();
					if(null != select && select.isSelectItemAlias(name.getLastName())) {
						return;
					}
				}
				
				for(int i=tablesStack.size()-1;i>=0;i--){
					Tables tables = tablesStack.get(i);
					
					if(resolveColumn(tables.all(),name)){
						break;
					}
					
					if(name.getScope() == Sql.Scope.SELECT_LIST || name.getScope() == Sql.Scope.ORDER_BY){
						break;
					}
				}
			}
		}
	}
	
	protected boolean resolveColumnFromTableSource(SqlTableSource tableSource,SqlObjectName name){
		if(tableSource instanceof SqlTableName){
			SqlTableName tableName = (SqlTableName)tableSource;
			if(null != tableName.getEntityMapping()){
				FieldMapping fm = tableName.getEntityMapping().tryGetFieldMapping(name.getLastName());
                if(null == fm) {
                    fm = tableName.getEntityMapping().tryGetFieldMappingByColumn(name.getLastName());
                }
				if(null != fm){
					name.setFieldMapping(tableName.getEntityMapping(), fm);
					return true;
				}
				
				//Special column/field name 'id'
				if("id".equalsIgnoreCase(name.getLastName())) {
					if(tableName.getEntityMapping().getKeyFieldMappings().length == 1) {
						name.setFieldMapping(tableName.getEntityMapping(), tableName.getEntityMapping().getKeyFieldMappings()[0]);
						return true;
					}
				}
			}
			return false;
		}else if(tableSource instanceof SqlSelect){
			return resolveColumnFromSubQuery((SqlSelect)tableSource,name);
		}else{
			return false;
			//throw new IllegalStateException("Unsupported table source '" + tableSource.getClass().getName() + "'");
		}
	}
	
	protected boolean resolveColumnFromSubQuery(SqlSelect subQuery,SqlObjectName name){
		//Checks the name is the selected item in the sub query.
		if(subQuery.isSelectItemAlias(name.getLastName())) {
			return true;
		}
		
		boolean selectAll = false;

		for(AstNode node : subQuery.getSelectList().getNodes()) {
			if(node instanceof SqlObjectName) {
				SqlObjectName selectedItem = (SqlObjectName)node;
				if(selectedItem.getScope() == Scope.SELECT_LIST) {
					if(name.getLastName().equalsIgnoreCase(selectedItem.getLastName())) {
						name.setReferenceTo(selectedItem);
						return true;
					}
				}
			}else if(node instanceof SqlAllColumns) {
				selectAll = true;
			}
		}
		
		if(!selectAll) {
			return false;
		}else{
			return resolveColumn(subQuery.getTableSources(), name);
		}
	}
	
	protected boolean resolveColumn(List<SqlTableSource> tableSources,SqlObjectName name){
		for(SqlTableSource tableSource : tableSources){
			if(resolveColumnFromTableSource(tableSource, name)){
				return true;
			}
		}
		return false;
	}
	
	protected SqlSelect currentSelectQuery() {
		if(selects.isEmpty()) {
			return null;
		}else{
			return selects.peek();
		}
	}
	
	protected static class Tables {
		private final Map<String,SqlTableSource> aliases = new SimpleCaseInsensitiveMap<>();
		private final List<SqlTableSource>       tables;
		
		public Tables(OrmMetadata metadata, List<SqlTableSource> tables){
			this.tables = tables;
			
			for(SqlTableSource table : tables){
				resolveTableSource(metadata, table);
			}
		}
		
		protected void resolveTableSource(OrmMetadata metadata,SqlTableSource table) {
			if(table instanceof SqlTableName){
				SqlTableName tableName = (SqlTableName)table;
				
				String lastName = tableName.getLastName();

				EntityMapping em = metadata.tryGetEntityMapping(lastName);
				if(null == em){
					em = metadata.tryGetEntityMappingByTableName(lastName);
                    if(null == em) {
                        em = metadata.tryGetEntityMappingByTableName(lastName);
                    }
				}
				if(null != em){
					if(tableName.getSecondaryOrFirstName() == null ||
					   Strings.equalsIgnoreCase(em.getTable().getSchema(), tableName.getSecondaryOrFirstName())){
						tableName.setEntityMapping(em);
					}
				}
			}
			
			String alias = table.getAlias();
			if(null != alias){
				aliases.put(alias, table);
			}
		}
		
		public SqlTableSource get(String alias){
			return aliases.get(alias);
		}

		public List<SqlTableSource> all() {
			return tables;
		}
	}
}