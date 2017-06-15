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

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.db.DbDialect;
import leap.db.model.DbColumn;
import leap.db.model.DbTable;
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.annotation.Nullable;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.metadata.MetadataContext;

public class DefaultSqlFactory implements SqlFactory {
	
	private static final Log log = LogFactory.get(DefaultSqlFactory.class);
	
	protected @Inject @M SqlLanguage defaultSqlLanguage;
	
	@Override
    public SqlCommand createSqlCommand(MetadataContext context,String sql) {
	    return createCommand(context,null, null, sql);
    }

	@Override
    public SqlCommand createSqlCommand(MetadataContext context,String source, String sql) {
	    return createCommand(context,null,source,sql);
    }

	@Override
    public SqlCommand createInsertCommand(MetadataContext context,EntityMapping em) {
		String sql = getInsertSql(context, em);
	    return null == sql ? null : createCommand(context,em,null,sql);
    }
	
	@Override
    public SqlCommand createInsertCommand(MetadataContext context, EntityMapping em, String[] fields) {
	    return createCommand(context,em,null,getInsertSql(context, em, fields));
    }

	@Override
    public SqlCommand createUpdateCommand(MetadataContext context,EntityMapping em) {
		String sql = getUpdateSql(context, em);
	    return null == sql ? null : createCommand(context,em,null,sql);
    }
	
	@Override
    public SqlCommand createUpdateCommand(MetadataContext context, EntityMapping em, String[] fields) {
	    return createCommand(context,em,null,getUpdateSql(context, em, fields));
    }

	@Override
    public SqlCommand createDeleteCommand(MetadataContext context,EntityMapping em) {
	    return createCommand(context,em,null,getDeleteSql(context,em));
    }
	
	@Override
    public SqlCommand createDeleteAllCommand(MetadataContext context, EntityMapping em) {
	    return createCommand(context, em, null, getDeleteAllSql(context, em));
    }
	
	@Override
    public SqlCommand createExistsCommand(MetadataContext context, EntityMapping em) {
		return createCommand(context, em, null, getExistsSql(context, em));
    }

	@Override
    public SqlCommand createCountCommand(MetadataContext context, EntityMapping em) {
		return createCommand(context, em, null, getCountSql(context, em));
    }

	@Override
    public SqlCommand createFindCommand(MetadataContext context,EntityMapping em) {
	    return createCommand(context,em,null,getFindSql(context,em));
    }
	
	@Override
    public SqlCommand createFindListCommand(MetadataContext context, EntityMapping em) {
	    return createCommand(context,em,null,getFindListSql(context,em));
    }

	@Override
    public SqlCommand createFindAllCommand(MetadataContext context, EntityMapping em) {
	    return createCommand(context, em, null, getFindAllSql(context, em));
    }
	
	protected SqlCommand createCommand(MetadataContext context,@Nullable EntityMapping em,String source,String sql){
		Args.notEmpty(sql,"sql");
		return new DefaultSqlCommand(source, source, null, defaultSqlLanguage, sql,null);
	}
	
	protected String getInsertSql(MetadataContext context,EntityMapping em){
		DbDialect dialect = context.getDb().getDialect();
		DbTable   table   = em.getTable();
		
        StringBuilder sql    = new StringBuilder();
        StringBuilder values = new StringBuilder();
        
        sql.append("insert into ").append(dialect.qualifySchemaObjectName(table)).append("(");
        
        int index = 0;
        
        for(FieldMapping fm : em.getFieldMappings()){
        	if(fm.isInsert()){
            	if(index > 0){
            		sql.append(",");
            		values.append(",");
            	}
				parseFieldInsertSql(sql,values,fm,context);
        		index++;
        	}
        }
        
        if(index == 0) {
        	log.warn("Cannot create insert sql for entity '{}' : no insert columns", em.getEntityName());
        	return null;
        }
        
        sql.append(") values (").append(values).append(")");
        
		return sql.toString();
	}
	
	protected String getInsertSql(MetadataContext context,EntityMapping em,String[] fields){
		DbDialect dialect = context.getDb().getDialect();
		DbTable   table   = em.getTable();
		
        StringBuilder sql    = new StringBuilder();
        StringBuilder values = new StringBuilder();
        
        sql.append("insert into ").append(dialect.qualifySchemaObjectName(table)).append("(");
        
        int index = 0;

        for(FieldMapping fm : em.getFieldMappings()){
        	if(!fm.isInsert()){
        		continue;
        	}
        	
        	boolean contains = false;
        	for(String field : fields){
        		if(Strings.equalsIgnoreCase(field, fm.getFieldName())){
					contains = true;
					break;
				}
        	}
        	
        	if( contains || fm.isAutoGenerateValue()){
				if(index > 0){
					sql.append(",");
					values.append(",");
				}
				parseFieldInsertSql(sql,values,fm,context);
        		index++;
        	}
        }
        
        sql.append(") values (").append(values).append(")");
        
		return sql.toString();
	}
	
	protected void parseFieldInsertSql(StringBuilder sql,StringBuilder values,FieldMapping fm,MetadataContext context){
		DbDialect dialect = context.getDb().getDialect();

		DbColumn column = fm.getColumn();

		sql.append(dialect.quoteIdentifier(column.getName()));

		if(!Strings.isEmpty(fm.getSequenceName())){
			values.append(dialect.getNextSequenceValueSqlString(fm.getSequenceName()));
		}else{
			values.append("#").append(fm.getFieldName()).append("#");
		}
	}
	
	protected String getUpdateSql(MetadataContext context,EntityMapping em){
		DbDialect dialect = context.getDb().getDialect();
		DbTable   table   = em.getTable();
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("update ").append(dialect.qualifySchemaObjectName(table)).append(" set ");
		
		int index = 0;
		
		for(FieldMapping fm : em.getFieldMappings()){
			if(fm.isUpdate() && !fm.isPrimaryKey()){
				
				if(index > 0){
					sql.append(",");
				}
				
				parseFieldUpdateSql(sql,context,fm);
				
				index++;
			}
		}
		
		if(index == 0) {
			log.warn("Cannot create update sql for entity '{}': no update columns", em.getEntityName());
			return null;
		}
		
		sql.append(" where ");
		index = 0;
		
		for(FieldMapping key : em.getKeyFieldMappings()){
			if(index > 0){
				sql.append(" and ");
			}
			sql.append(dialect.quoteIdentifier(key.getColumnName())).append("=#").append(key.getFieldName()).append("#");
			index++;
		}
		
		FieldMapping lp = em.getOptimisticLockField();
		if(null != lp){
			if(index > 0){
				sql.append(" and");
			}
			sql.append(" ").append(dialect.quoteIdentifier(lp.getColumnName())).append("=#").append(lp.getFieldName()).append("#");
		}

        if(em.isSharding()) {
            FieldMapping fm = em.getShardingField();
            if(index > 0){
                sql.append(" and");
            }
            sql.append(" ").append(dialect.quoteIdentifier(fm.getColumnName())).append("=#").append(fm.getFieldName()).append("#");
        }
		
		return sql.toString();
	}
	
	protected String getUpdateSql(MetadataContext context,EntityMapping em,String[] fields){
		DbDialect dialect = context.getDb().getDialect();
		DbTable   table   = em.getTable();
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("update ").append(dialect.qualifySchemaObjectName(table)).append(" set ");
		
		int index = 0;
		
		for(FieldMapping fm : em.getFieldMappings()){
			
			if(fm.isPrimaryKey() || !fm.isUpdate()){
				continue;
			}
			
        	boolean contains = false;
        	for(String field : fields){
        		if(Strings.equalsIgnoreCase(field, fm.getFieldName())){
        			contains = true;
        			break;
        		}
        	}
        	
			if(contains || fm.isOptimisticLock()) {
				if(index > 0){
					sql.append(",");
				}

				parseFieldUpdateSql(sql,context,fm);
				
				index++;
			}
		}
		
		sql.append(" where ");
		index = 0;
		
		for(FieldMapping key : em.getKeyFieldMappings()){
			if(index > 0){
				sql.append(" and ");
			}
			sql.append(dialect.quoteIdentifier(key.getColumnName())).append("=#").append(key.getFieldName()).append("#");
			index++;
		}
		
		FieldMapping lp = em.getOptimisticLockField();
		if(null != lp){
			if(index > 0){
				sql.append(" and");
			}
			sql.append(" ").append(dialect.quoteIdentifier(lp.getColumnName())).append("=#").append(lp.getFieldName()).append("#");
		}

        if(em.isSharding()) {
            FieldMapping fm = em.getShardingField();
            if(index > 0){
                sql.append(" and");
            }
            sql.append(" ").append(dialect.quoteIdentifier(fm.getColumnName())).append("=#").append(fm.getFieldName()).append("#");
        }
		
		return sql.toString();
	}
	
	protected void parseFieldUpdateSql(StringBuilder sql,MetadataContext context,FieldMapping fm){
		DbDialect dialect = context.getDb().getDialect();
		
		sql.append(dialect.quoteIdentifier(fm.getColumnName())).append("=#");

		if(fm.isOptimisticLock()){
			sql.append(fm.getNewOptimisticLockFieldName());
		}else{
			sql.append(fm.getFieldName());
		}

		sql.append("#");
	}
	
	protected String getDeleteSql(MetadataContext context,EntityMapping em){
		DbDialect dialect = context.getDb().getDialect();
		DbTable   table   = em.getTable();
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("delete from ").append(dialect.qualifySchemaObjectName(table)).append(" where ");
		
		int index = 0;
		
		for(FieldMapping key : em.getKeyFieldMappings()){
			if(index > 0){
				sql.append(" and ");
			}
			
			sql.append(dialect.quoteIdentifier(key.getColumnName())).append("=#").append(key.getFieldName()).append("#");
			index++;
		}

        if(em.isSharding()) {
            FieldMapping fm = em.getShardingField();
            if(index > 0){
                sql.append(" and");
            }
            sql.append(" ").append(dialect.quoteIdentifier(fm.getColumnName())).append("=#").append(fm.getFieldName()).append("#");
        }
		
		return sql.toString();
	}
	
	protected String getDeleteAllSql(MetadataContext context,EntityMapping em){
		DbDialect dialect = context.getDb().getDialect();
		DbTable   table   = em.getTable();
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("delete from ").append(dialect.qualifySchemaObjectName(table));
		
		return sql.toString();
	}
	
	protected String getExistsSql(MetadataContext context,EntityMapping em){
		DbDialect dialect = context.getDb().getDialect();
		DbTable   table   = em.getTable();
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("select 1 from ").append(dialect.qualifySchemaObjectName(table)).append(" where ");
		
		int index = 0;
		for(FieldMapping key : em.getKeyFieldMappings()){
			if(index > 0){
				sql.append(" and ");
			}
			
			sql.append(dialect.quoteIdentifier(key.getColumnName())).append("=#").append(key.getFieldName()).append("#");
			index++;
		}
		
		return sql.toString();
	}
	
	protected String getCountSql(MetadataContext context,EntityMapping em){
		DbDialect dialect = context.getDb().getDialect();
		DbTable   table   = em.getTable();
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("select count(*) from ").append(dialect.qualifySchemaObjectName(table));
		
		return sql.toString();
	}
	
	protected String getFindSql(MetadataContext context,EntityMapping em){
		DbDialect dialect = context.getDb().getDialect();
		DbTable   table   = em.getTable();
		
		StringBuilder sql = new StringBuilder();

        sql.append("select ")
            .append(createSelectColumns(context, em, null))
            .append(" from ")
            .append(dialect.qualifySchemaObjectName(table))
			.append(" t ")
			.append(" where ");
		
		int index = 0;
		for(FieldMapping key : em.getKeyFieldMappings()){
			if(index > 0){
				sql.append(" and ");
			}
			
			sql.append(dialect.quoteIdentifier(key.getColumnName())).append("=#").append(key.getFieldName()).append("#");
			index++;
		}
		
		return sql.toString();
	}
	
	protected String getFindListSql(MetadataContext context,EntityMapping em){
		if(em.isCompositeKey()) {
			throw new IllegalStateException("Cannot create 'findList' sql for composite key entity '" + em.getEntityName() + "'");
		}
		
		DbDialect dialect = context.getDb().getDialect();
		DbTable   table   = em.getTable();
		
		StringBuilder sql = new StringBuilder();

        sql.append("select ")
            .append(createSelectColumns(context, em, null))
            .append(" from ")
            .append(dialect.qualifySchemaObjectName(table))
			.append(" t ").append(" where ");
		
		int index = 0;
		for(FieldMapping key : em.getKeyFieldMappings()){
			if(index > 0){
				sql.append(" and ");
			}
			
			sql.append(dialect.quoteIdentifier(key.getColumnName())).append(" in #").append(key.getFieldName()).append("#");
			index++;
		}
		
		return sql.toString();
	}	
	
	protected String getFindAllSql(MetadataContext context,EntityMapping em){
		DbDialect dialect = context.getDb().getDialect();
		DbTable   table   = em.getTable();
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("select ")
            .append(createSelectColumns(context, em, null))
            .append(" from ")
            .append(dialect.qualifySchemaObjectName(table))
			.append(" t ");
		
		return sql.toString();
	}

    @Override
    public String createSelectColumns(MetadataContext context, EntityMapping em, String tableAlias) {
        StringBuilder s = new StringBuilder();

        int index = 0;

        for(FieldMapping fm : em.getFieldMappings()){

            if(index > 0) {
                s.append(',');
            }

            if(!Strings.isEmpty(tableAlias)) {
                s.append(tableAlias).append('.');
            }

            s.append(fm.getColumnName());

            index++;
        }

        return s.toString();
    }
}