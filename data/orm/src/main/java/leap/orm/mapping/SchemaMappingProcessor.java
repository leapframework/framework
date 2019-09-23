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

import leap.db.model.DbColumn;
import leap.db.model.DbColumnBuilder;
import leap.db.model.DbSchema;
import leap.db.model.DbTable;
import leap.orm.metadata.MetadataContext;
import leap.orm.metadata.MetadataException;

public class SchemaMappingProcessor extends MappingProcessorAdapter {

	@Override
    public void postMappingEntity(MetadataContext context, EntityMappingBuilder emb) throws MetadataException {
		if(!context.getConfig().isReadDbSchema()) {
			return;
		}
		if(null == context.getDb()) {
			return;
		}

		String entityName = emb.getEntityName();
		
		DbSchema schema = context.getDb().getMetadata().getSchema(emb.getTableCatalog(),emb.getTableSchema());
		if(null == schema) return;
		
		//find matched table name in db schema
		DbTable  table;
		if(!emb.isTableNameDeclared()){
			table = findTableOf(context, entityName, schema);
		}else{
			table = schema.findTable(emb.getTableName());
		}
		
		if(null != table){
			mappingTableToEntity(context, table, emb);
		}
    }
	
	@Override
    public void finalMappingEntity(MetadataContext context, EntityMappingBuilder emb) throws MetadataException {
		DbTable table = emb.getPhysicalTable();
		
		if(null != table){
			//Checks the declared id fields and primary key columns.
			if(emb.isIdDeclared() && !table.isView()) {
				if(table.getPrimaryKeyColumns().length != emb.getIdFieldMappings().size()) {
					throw new MappingConfigException("The declared id fields(" + emb.getIdFieldMappings().size() + 
													 ") do not match the primary key columns(" + table.getPrimaryKeyColumns().length + 
													 ") in entity '" + emb.getEntityName() + "'");
				}
				
				for(DbColumn pk : table.getPrimaryKeyColumns()) {
					FieldMappingBuilder fmb = findFieldOf(context, pk, emb);
					if(null == fmb) {
						throw new MappingConfigException("Id field must be declared in entity '" + emb.getEntityName() +
														 "' for primary key column '" + pk.getName() + "' in table '" + table.getName() + "'");
					}
				}
			}
			
			for(FieldMappingBuilder fmb : emb.getFieldMappings()) {
				DbColumn column;
				
				if(!fmb.isColumnNameDeclared()) {
					column = findColumnOf(context, table, fmb);
				}else{
					column = table.findColumn(fmb.getColumn().getName());
				}

				if(null != column) {
					mappingColumnToField(context, table, column, emb, fmb);
				}
			}

            if(emb.isAutoGenerateColumns()) {
                for (DbColumn column : table.getColumns()) {
                    FieldMappingBuilder fmb = emb.findFieldMappingByColumn(column.getName());
                    if (null == fmb && (context.getMappingStrategy().isAutoGeneratedColumn(context, column) || column.isPrimaryKey())) {
                        emb.addFieldMapping(context.getMappingStrategy().createFieldMappingByColumn(context, emb, column));
                    }
                }
            }
		}
    }

	protected void mappingTableToEntity(MetadataContext context, DbTable table,EntityMappingBuilder emb) {
		emb.setTableName(context.getNamingStrategy().tableName(table.getName()));
		emb.setPhysicalTable(table);
	}
	
	protected void mappingColumnToField(MetadataContext context,
										DbTable table,DbColumn column,
										EntityMappingBuilder emb,FieldMappingBuilder fmb){
		
		DbColumnBuilder cb = new DbColumnBuilder(column);
        if(table.isView() && fmb.isId()) {
            cb.setPrimaryKey(true);
            cb.setNullable(false);
        }

        cb.setName(context.getNamingStrategy().columnName(cb.getName()));
        fmb.setColumn(cb);
        fmb.setHasPhysicalColumn(true);

		
		if(null == fmb.getNullable()){
			fmb.setNullable(cb.isNullable());
		}
	}
	
	protected FieldMappingBuilder findFieldOf(MetadataContext context,DbColumn column,EntityMappingBuilder emb){
		for(FieldMappingBuilder fmb : emb.getFieldMappings()){
			if(fmb.isColumnNameDeclared() && fmb.getColumn().getName().equalsIgnoreCase(column.getName())) {
				return fmb;
			}
			
			if(context.getNamingStrategy().isColumnOfField(column.getName(), fmb.getFieldName())){
				return fmb;
			}
		}
		return null;
	}
	
	protected DbColumn findColumnOf(MetadataContext context,DbTable table, FieldMappingBuilder fmb ){
		for(DbColumn column : table.getColumns()) {
			if(context.getNamingStrategy().isColumnOfField(column.getName(), fmb.getFieldName())) {
				return column;
			}
		}
		return null;
	}
	
	protected DbTable findTableOf(MetadataContext context, String entityName,DbSchema schema){
		for(DbTable table : schema.getTables()){
			if(context.getNamingStrategy().isTableOfEntity(table.getName(), entityName)){
				return table;
			}
		}

		if(context.getConfig().isAutoMappingTableWithAcronym()) {
			for (DbTable table : schema.getTables()) {
				if (context.getNamingStrategy().isTableOfWithAcronym(table.getName(), entityName)) {
					return table;
				}
			}
		}
		
		return null;
	}
}
