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
package leap.db.platform;

import leap.db.DbDialect;
import leap.db.DbMetadataReader;
import leap.db.exception.DbSchemaException;
import leap.db.model.*;
import leap.lang.Assert;
import leap.lang.Builders;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.exception.NestedSQLException;
import leap.lang.jdbc.ConnectionProxy;
import leap.lang.jdbc.JDBC;
import leap.lang.jdbc.JdbcType;
import leap.lang.jdbc.JdbcTypes;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.time.StopWatch;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.sql.DatabaseMetaData.*;

public class GenericDbMetadataReader extends GenericDbMetadataReaderBase implements DbMetadataReader {
	
	public static final String INTERNAL_NAME_PREFIX = "INTERNAL_";
	
	private final Log log = LogFactory.get(this.getClass());
	
	protected DbDialect dialect;
	
	protected String   defaultCatalogPattern = "%";
	protected String   defaultSchemaPattern  = "%";
	protected String   defaultTablePattern   = "%";
	protected String   defaultColumnPattern  = null;
	protected String[] defaultTableTypes     = new String[]{DbTableTypes.TABLE,DbTableTypes.VIEW};

	protected GenericDbMetadataReader(){

	}
	
	protected synchronized void init(DbDialect dialect){
		Assert.isTrue(this.dialect == null);
		this.dialect = dialect;
	}

	@Override
    public DbSchemaName[] readSchemaNames(Connection connection) {
		ResultSet rs = null;
		
		try{
			rs = getSchemas(connection, connection.getMetaData());
			
			List<DbSchemaName> schemas = new ArrayList<DbSchemaName>();

			while(rs.next()){
				String catalog = getSchemaCatalog(rs);
				String schema  = getSchemaName(rs);
				
				if(!dialect.isSystemSchema(schema)){
					schemas.add(new DbSchemaName(catalog, schema));
				}
			}
			
			return schemas.toArray(new DbSchemaName[schemas.size()]);
		}catch(SQLException e){
			throw new NestedSQLException("Error reading schema names : " + e.getMessage(),e);
		}finally{
			JDBC.closeResultSetAndStatement(rs);
		}
    }
	
    @Override
    @SuppressWarnings("unchecked")
    public DbSchema readSchema(Connection connection, String catalog, String schema) {
		try {
	        DatabaseMetaData   dm     = connection.getMetaData();
	        MetadataParameters params = createMetadataParameters(connection,dm,catalog,schema);
	        
	        StopWatch sw = StopWatch.startNew();
	        
	        List<DbTableBuilder> tables = readAllTables(connection, dm, params);
	        
	        log.debug("read {} tables used {}ms ",tables.size(),sw.getElapsedMilliseconds());
	        readAllTableObjects(connection, dm, params, tables);
	        
	        List<DbSequence> sequences = null;
	        
	        if(dialect.supportsSequence()){
	        	sequences = readAllSequences(connection, dm, params);
	        }else{
	        	sequences = (List<DbSequence>)Collections.EMPTY_LIST;
	        }
	        
	        //post read tabels
	        postReadTables(connection, dm, params, tables);
	        
	        return new DbSchemaBuilder(catalog,schema)
	        				.addTables(Builders.buildArray(tables,new DbTable[tables.size()]))
	        				.addSequences(sequences)
	        				.build();
        } catch (SQLException e) {
        	throw new NestedSQLException("Error reading schema : " + e.getMessage(),e);
        }
    }
    
    protected void postReadTables(Connection connection,DatabaseMetaData dm,MetadataParameters params,List<DbTableBuilder> tables) {
    	//A table without columns (empty table) may be dropped, so we should remove it. 
    	List<DbTableBuilder> emptyTables = new ArrayList<>();
    	
    	for(DbTableBuilder table : tables){
    		
    		if(table.getColumns().isEmpty()){
    			emptyTables.add(table);
    			continue;
    		}
    		
    		postReadTable(connection, dm, params, table);
    	}
    	
    	for(DbTableBuilder table : emptyTables){
    		tables.remove(table);
    	}
    }
    
    protected void postReadTable(Connection connection,DatabaseMetaData dm,MetadataParameters params,DbTableBuilder table){
    	List<DbIndexBuilder> rmIndexes = new ArrayList<DbIndexBuilder>();
    	
    	for(DbIndexBuilder ix : table.getIndexes()){
    		//set column's unique property to true
    		if(ix.isUnique() && ix.getColumnNames().size() == 1){
    			DbColumnBuilder column = table.findColumn(ix.getColumnNames().get(0));
    			if(null != column && !column.isPrimaryKey()){
    				column.setUnique(true);
    			}
    		}
    		
    		//removes internal index
    		if(ix.isInternal()){
    			rmIndexes.add(ix);
    		}
    	}
    	
    	for(DbIndexBuilder rm : rmIndexes){
    		table.getIndexes().remove(rm);
    	}
    }
    
	protected List<DbTableBuilder> readAllTables(Connection connection,DatabaseMetaData dm,MetadataParameters params) throws SQLException {
		ResultSet rs = null;
		try{
			List<DbTableBuilder> tables = new ArrayList<>();
			rs = getTables(connection, dm, params);
			if(null != rs){
                while ( rs.next() ) {
                	String tableCatalog = getTableCatalog(rs);
                    String tableSchema  = getTableSchema(rs);
                    String tableName    = rs.getString(TABLE_NAME);
                    
                    if(Strings.isEmpty(params.schema) || params.schema.equalsIgnoreCase(tableSchema)){
                    	DbTableBuilder table = new DbTableBuilder(tableCatalog,tableSchema,tableName);
                    	
                    	readTableProperties(table, rs);
                    	
                    	tables.add(table);
                    }
                }
            }
            
            return tables;
		}finally{
			JDBC.closeResultSetAndStatement(rs);
		}
	}
	
	protected List<DbSequence> readAllSequences(Connection connection,DatabaseMetaData dm,MetadataParameters params) throws SQLException {
		ResultSet rs = null;
		try{
			List<DbSequenceBuilder> sequences = new ArrayList<DbSequenceBuilder>();

			rs = getSequences(connection, dm, params);
			if(rs != null){
				while(rs.next()){
					String sequenceCatalog = rs.getString(SEQUENCE_CATALOG);
					String sequenceSchema  = rs.getString(SEQUENCE_SCHEMA);
					String sequenceName    = rs.getString(SEQUENCE_NAME);

					if(Strings.isEmpty(params.schema) || params.schema.equalsIgnoreCase(sequenceSchema)){
						DbSequenceBuilder sequence = new DbSequenceBuilder(sequenceName).setCatalog(sequenceCatalog).setSchema(sequenceSchema);

						if(readSequenceProperties(sequence, rs)){
							sequences.add(sequence);
						}
					}
				}
			}
			return Builders.buildList(sequences);
		}finally{
			JDBC.closeResultSetAndStatement(rs);
		}
	}
	
	protected void readTableProperties(DbTableBuilder table,ResultSet rs) throws SQLException {
		table.setType(rs.getString(TABLE_TYPE));
		table.setComment(rs.getString(REMARKS));
	}
	
	protected boolean readSequenceProperties(DbSequenceBuilder sequence,ResultSet rs) throws SQLException {
		sequence.setMinValue(dialect.getColumnValue(rs, SEQUENCE_MINVALUE,Long.class));
		sequence.setMaxValue(dialect.getColumnValue(rs, SEQUENCE_MAXVALUE,Long.class));
		sequence.setIncrement(dialect.getColumnValue(rs, SEQUENCE_INCREMENT,Integer.class));
		sequence.setStart(dialect.getColumnValue(rs, SEQUENCE_START,Long.class));
		sequence.setCache(dialect.getColumnValue(rs, SEQUENCE_CACHE,Integer.class));
		sequence.setCycle(dialect.getColumnValue(rs, SEQUENCE_CYCLE,Boolean.class));
		
		return !isInternalSequence(sequence, rs);
	}
	
	protected void readAllTableObjects(Connection connection,DatabaseMetaData dm,MetadataParameters params,List<DbTableBuilder> tables) throws SQLException {
		if(null == tables || tables.isEmpty()){
			return ;
		}
		
		AtomicInteger counter = new AtomicInteger();
		
		StopWatch sw = StopWatch.startNew();
		
		readAllColumns(connection, dm, params, tables, counter);
		log.debug("read {} columns used {}ms",counter.get(),sw.stop().getElapsedMilliseconds());
		
		sw.restart();
		counter.set(0);
		readAllPrimaryKeys(connection, dm, params, tables, counter);
		log.debug("read {} primary keys used {}ms",counter.get(),sw.stop().getElapsedMilliseconds());
		
		sw.restart();
		counter.set(0);
		readAllForeignKeys(connection, dm, params, tables, counter);
		log.debug("read {} foreign keys used {}ms",counter.get(),sw.stop().getElapsedMilliseconds());
		
		sw.restart();
		counter.set(0);
		readAllIndexes(connection, dm, params, tables, counter);
		log.debug("read {} indexes used {}ms",counter.get(),sw.stop().getElapsedMilliseconds());
	}
	
    protected void readAllColumns(Connection connection,DatabaseMetaData dm,MetadataParameters params,List<DbTableBuilder> tables, AtomicInteger counter) throws SQLException {
		ResultSet rs = null;
		try{
			rs = getColumns(connection, dm, params);
			
			if(null != rs){
				while(rs.next()){
					counter.incrementAndGet();
					
					String schemaName = getColumnSchema(rs);
					String tableName  = rs.getString(TABLE_NAME);
					
					if(Strings.equalsIgnoreCase(params.schema, schemaName)){
						for(DbTableBuilder table : tables){
							if(Strings.equalsIgnoreCase(tableName, table.getName())){
								DbColumnBuilder column = new DbColumnBuilder();
								
								if(readColumnProperties(table,column,rs)){
									if(table.findColumn(column.getName()) != null){
										throw new DbSchemaException(
												Strings.format("Found duplicate column '{0}' in table '{1}'",
															   column.getName(),table.getName()));
									}
									
									table.addColumn(column,readPosition(rs, ORDINAL_POSITION));
								}
							}
						}
					}
				}
			}
		}finally{
			JDBC.closeResultSetAndStatement(rs);
		}
	}	
	
	protected boolean readColumnProperties(DbTableBuilder table, DbColumnBuilder column,ResultSet rs) throws SQLException {
		column.setName(rs.getString(COLUMN_NAME));
		column.setTypeCode(rs.getInt(COLUMN_TYPE));
		
		JdbcType jdbcType = JdbcTypes.forTypeCode(column.getTypeCode());
		
		column.setTypeName(jdbcType.getName());
		
		String nativeDefaultValue = rs.getString(COLUMN_DEFAULT);
		if(!Strings.isEmpty(nativeDefaultValue) && !isInternalDefaultValue(table, column, rs, nativeDefaultValue)){
			column.setDefaultValue(dialect.readDefaultValue(column.getTypeCode(), nativeDefaultValue));
		}
		
		if(jdbcType.supportsLength()) {
		    readColumnSizeString(column, rs.getString(COLUMN_SIZE));
		}else if(jdbcType.supportsPrecisionAndScale()){
            Object precision = rs.getObject(COLUMN_PRECISION);
            if (precision != null) {
                column.setPrecision(Converts.toInt(precision));
            }

            if (column.getPrecision() != null && column.getPrecision() > 0) {
                Object scale = rs.getObject(COLUMN_SCALE);
                if (scale != null) {
                    int scaleValue = Converts.toInt(scale);
						if (scaleValue > 0) {
                        column.setScale(scaleValue);
                    }
                }
            } else {
                readColumnSizeString(column, rs.getString(COLUMN_SIZE));
            }
		}

		column.setNullable(!"NO".equalsIgnoreCase(Strings.trim(rs.getString(COLUMN_NULLABLE))));
		column.setComment(Strings.trimToNull(rs.getString(REMARKS)));
		
		if(column.getLength() != null && column.getLength() > 0) {
		    if(null != column.getPrecision() && column.getPrecision() > 0) {
		        log.warn("Column '" + column.getName() + "' should not has both length and precision value");
		    }
		    
		    if(null != column.getScale() && column.getScale() > 0) {
		        log.warn("Column '" + column.getName() + "' should not has both length and scale value");
		    }
		}
		
		return true;
	}
	
	/**
	 * Returns the size of the column. This is either a simple integer value or a comma-separated pair of integer values
	 * specifying the size and scale. I.e. "size" or "precision,scale".
	 * 
	 * <p>
	 * The COLUMN_SIZE column the specified column size for the given column. 
     * For numeric data, this is the maximum precision.  For character data, this is the length in characters. 
     * For datetime datatypes, this is the length in characters of the String representation (assuming the 
     * maximum allowed precision of the fractional seconds component). For binary data, this is the length in bytes.  For the ROWID datatype, 
     * this is the length in bytes. Null is returned for data types where the
     * column size is not applicable.
	 * 
	 * @see DatabaseMetaData#getColumns(String, String, String, String)
	 */
	protected void readColumnSizeString(DbColumnBuilder column,String sizeString){
		if(!Strings.isEmpty(sizeString)){
            int pos = sizeString.indexOf(",");

			if (pos < 0) {
				column.setScale(0);
				column.setPrecision(0);
				column.setLength(Integer.parseInt(sizeString));
			} else {
				column.setPrecision(Integer.parseInt(sizeString.substring(0, pos).trim()));
				column.setScale(Integer.parseInt(sizeString.substring(pos+1).trim()));
				column.setLength(0);
			}
		}
	}
	
	protected void readAllPrimaryKeys(Connection connection,DatabaseMetaData dm,MetadataParameters params,List<DbTableBuilder> tables, AtomicInteger counter) throws SQLException {
		if(supportsReadAllPrimaryKeys()){
			ResultSet rs = null;
			try{
				rs = getPrimaryKeys(connection, dm, params);
				if(null != rs){
					while(rs.next()){
						counter.incrementAndGet();
						
						String schemaName = getPrimaryKeySchema(rs);
						String tableName  = rs.getString(TABLE_NAME);
						
						if(Strings.equalsIgnoreCase(params.schema, schemaName)){
							for(DbTableBuilder table : tables){
								if(Strings.equalsIgnoreCase(tableName, table.getName())){
									readPrimaryKeyColumn(table, rs);
								}
							}
						}
					}
				}
			}finally{
				JDBC.closeResultSetAndStatement(rs);
			}
		}else{
			for(DbTableBuilder t : tables){
				ResultSet rs = null;
				try{
					rs = dm.getPrimaryKeys(params.catalog, params.schema, t.getName());
					if(null != rs){
						counter.incrementAndGet();
						
						while(rs.next()){
							String schemaName = getPrimaryKeySchema(rs);
							String tableName  = rs.getString(TABLE_NAME);
							
							if(Strings.equals(params.schema, schemaName) && Strings.equalsIgnoreCase(tableName, t.getName())){
								readPrimaryKeyColumn(t, rs);
							}
						}
					}
				}finally{
					JDBC.closeResultSetAndStatement(rs);
				}
			}
		}
	}
	
	protected boolean supportsReadAllPrimaryKeys() {
		return true;
	}
	
	protected void readPrimaryKeyColumn(DbTableBuilder table,ResultSet rs) throws SQLException {
		String pkName		= rs.getString(PK_NAME);
		String pkColumnName = rs.getString(COLUMN_NAME);
		
		DbColumnBuilder column = table.findColumn(pkColumnName);
		
		Assert.notNull(column,"the primary key column name '{0}' not found in table '{1}'",pkColumnName,table.getName());
		
		column.setPrimaryKey(true);
		
		table.setPrimaryKeyName(pkName);
        table.addPrimaryKeyColumnName(pkColumnName, readPosition(rs, KEY_SEQ));
	}
	
	protected void readAllForeignKeys(Connection connection,DatabaseMetaData dm,MetadataParameters params,List<DbTableBuilder> tables, AtomicInteger counter) throws SQLException {
		if(supportsReadAllForeignKeys()){
			ResultSet rs = null;
			try{
				rs = getForeignKeys(connection, dm, params);
				if(null != rs){
					while(rs.next()){
						counter.incrementAndGet();
						String schemaName = getForeignKeySchema(rs);
						String tableName  = rs.getString(FKTABLE_NAME);
						
						if(Strings.equalsIgnoreCase(params.schema, schemaName)){
							for(DbTableBuilder table : tables){
								if(Strings.equalsIgnoreCase(tableName, table.getName())){
									String fkName = rs.getString(FK_NAME);
									
									DbForeignKeyBuilder fk = table.findForeignKey(fkName);
									
									if(null == fk){
										fk = new DbForeignKeyBuilder().setName(fkName);
										
										if(!readForeignKeyProperties(table,fk, rs)){
											//ignored
											break;
										}
										
										readForeignKeyColumn(table,fk, rs);
										
										table.addForeignKey(fk);
									}else{
										readForeignKeyColumn(table,fk, rs);
									}
								}
							}
						}
					}
				}
			}finally{
				JDBC.closeResultSetAndStatement(rs);
			}
		}else{
			for(DbTableBuilder t : tables) {
				ResultSet rs = null;
				try{
					rs = dm.getImportedKeys(params.catalogPattern, params.schemaPattern, t.getName());
					if(null != rs){
						counter.incrementAndGet();
						
						while(rs.next()) {
							String schemaName = getForeignKeySchema(rs);
							
							if(Strings.equalsIgnoreCase(params.schema, schemaName)){
								String fkName = rs.getString(FK_NAME);
								
								DbForeignKeyBuilder fk = t.findForeignKey(fkName);
								
								if(null == fk){
									fk = new DbForeignKeyBuilder().setName(fkName);
									
									if(!readForeignKeyProperties(t,fk, rs)){
										//ignored
										break;
									}
									
									readForeignKeyColumn(t,fk, rs);
									
									t.addForeignKey(fk);
								}else{
									readForeignKeyColumn(t,fk, rs);
								}
							}						
						}
					}
				}finally{
					JDBC.closeResultSetAndStatement(rs);
				}
			}
		}		
	}
	
	protected boolean supportsReadAllForeignKeys() {
		return true;
	}
	
	protected void readForeignKeyColumn(DbTableBuilder table, DbForeignKeyBuilder fk,ResultSet rs) throws SQLException {
		String localColumnName   = rs.getString(FKCOLUMN_NAME);
		String foreignColumnName = rs.getString(PKCOLUMN_NAME);
		
        fk.addColumn(new DbForeignKeyColumn(localColumnName,foreignColumnName), readPosition(rs, KEY_SEQ));
	}
	
	protected boolean readForeignKeyProperties(DbTableBuilder table,DbForeignKeyBuilder fk,ResultSet rs) throws SQLException {
		
		DbSchemaObjectName foreignTable = 
				new DbSchemaObjectName(rs.getString(PKTABLE_CATALOG), rs.getString(PKTABLE_SCHEMA),rs.getString(PKTABLE_NAME));
		
        fk.setForeignTable(foreignTable);

        DbCascadeAction onUpdateAction = convertAction(rs.getShort("UPDATE_RULE"));
        DbCascadeAction onDeleteAction = convertAction(rs.getShort("DELETE_RULE"));
        
        fk.setOnUpdate(onUpdateAction);
        fk.setOnDelete(onDeleteAction);
        
        return true;
	}
	
	protected void readAllIndexes(Connection connection,DatabaseMetaData dm,MetadataParameters params,List<DbTableBuilder> tables, AtomicInteger counter) throws SQLException {
		if(supportsReadAllIndexes()){
			ResultSet rs = null;
			try{
				rs = getIndexes(connection, dm, params);
				if(null != rs){
					while(rs.next()){
						counter.incrementAndGet();
						
						String schemaName = getIndexSchema(rs);
						String tableName  = rs.getString(TABLE_NAME);
						
						if(Strings.equalsIgnoreCase(params.schema, schemaName)){
							for(DbTableBuilder table : tables){
								if(Strings.equalsIgnoreCase(tableName, table.getName())){
									if(!readIndex(table, rs)){
										break;
									}
								}
							}
						}
					}
				}
			}finally{
				JDBC.closeResultSetAndStatement(rs);
			}
		}else{
			for(DbTableBuilder t : tables) {
				ResultSet rs = null;
				try{
					rs = dm.getIndexInfo(params.catalogPattern, params.schemaPattern, t.getName(), false, false);
					if(null != rs){
						counter.incrementAndGet();
						
						while(rs.next()) {
							String schemaName = getIndexSchema(rs);
							String tableName  = rs.getString(TABLE_NAME);
							
							if(Strings.equalsIgnoreCase(params.schema, schemaName)){
								if(Strings.equalsIgnoreCase(tableName, t.getName())){
									if(!readIndex(t, rs)){
										break;
									}
								}
							}
						}
					}
				}finally{
					JDBC.closeResultSetAndStatement(rs);
				}
			}
		}
	}	
	
	protected boolean readIndex(DbTableBuilder t,ResultSet rs) throws SQLException {
		String ixName = rs.getString(INDEX_NAME);
		
		DbIndexBuilder ix = t.findIndex(ixName);
		
		if(null == ix){
			ix = new DbIndexBuilder().setName(ixName);
			
			if(!readIndexInfo(t,ix,rs)){
				//ignored
				return false;
			}
			
			readIndexColumn(t,ix,rs);
			
			t.addIndex(ix);
		}else{
			readIndexColumn(t,ix,rs);
		}
		
		return true;
	}
	
	protected boolean supportsReadAllIndexes() {
		return true;
	}
	
	protected void readIndexColumn(DbTableBuilder table,DbIndexBuilder ix,ResultSet rs) throws SQLException {
        ix.addColumnName(rs.getString(COLUMN_NAME),readPosition(rs, ORDINAL_POSITION));
	}
	
	protected boolean readIndexInfo(DbTableBuilder table,DbIndexBuilder ix,ResultSet rs) throws SQLException {
		short type = rs.getShort("TYPE");
		
        // we're ignoring statistic indexes
        if (type == DatabaseMetaData.tableIndexStatistic){
        	return false;
        }
        
        ix.setUnique(!rs.getBoolean(NON_UNIQUE));
        
        if(isInternalIndexGeneratedByApp(table, ix ,rs)|| isInternalIndex(table, ix, rs)){
        	ix.setInternal(true);
        }
        
		return true;
	}	
	
	protected boolean isInternalIndexGeneratedByApp(DbTableBuilder table,DbIndexBuilder ix,ResultSet rs) {
		return ix.getName().startsWith(INTERNAL_NAME_PREFIX);
	}
	
	protected boolean isInternalIndex(DbTableBuilder table,DbIndexBuilder ix,ResultSet rs) throws SQLException {
		return false;
	}
	
	protected boolean isInternalDefaultValue(DbTableBuilder table, DbColumnBuilder column,ResultSet rs,String defaultValue) throws SQLException {
		return false;
	}
	
	
	protected boolean isInternalSequence(DbSequenceBuilder seq,ResultSet rs) throws SQLException {
		return false;
	}
    
    protected DbCascadeAction convertAction(Short jdbcActionValue){
		DbCascadeAction action = null;

		if (jdbcActionValue != null) {
			switch (jdbcActionValue.shortValue()) {
				case DatabaseMetaData.importedKeyCascade:
					action = DbCascadeAction.CASCADE;
					break;
				case DatabaseMetaData.importedKeySetNull:
					action = DbCascadeAction.SET_NULL;
					break;
				case DatabaseMetaData.importedKeySetDefault:
					action = DbCascadeAction.SET_DEFAULT;
					break;
				case DatabaseMetaData.importedKeyRestrict:
					action = DbCascadeAction.RESTRICT;
					break;
			}
		}
		return action;
    }    
	
	protected ResultSet getSchemas(Connection connection,DatabaseMetaData dm) throws SQLException {
		return dm.getSchemas();
	}
	
	protected ResultSet getTables(Connection connection,DatabaseMetaData dm,MetadataParameters params) throws SQLException {
		return dm.getTables(params.catalogPattern, params.schemaPattern, params.tablePattern, params.tableTypes);
	}
	
	protected ResultSet getColumns(Connection connection,DatabaseMetaData dm,MetadataParameters params) throws SQLException {
		return dm.getColumns(params.catalogPattern, params.schemaPattern, params.tablePattern, getDefaultColumnPattern());
	}
	
	protected ResultSet getPrimaryKeys(Connection connection,DatabaseMetaData dm,MetadataParameters params) throws SQLException {
		return dm.getPrimaryKeys(params.catalogPattern, params.schemaPattern, params.tablePattern);
	}
	
	protected ResultSet getForeignKeys(Connection connection,DatabaseMetaData dm,MetadataParameters params) throws SQLException {
		return dm.getImportedKeys(params.catalogPattern, params.schemaPattern, params.tablePattern);
	}
	
	protected ResultSet getIndexes(Connection connection,DatabaseMetaData dm,MetadataParameters params) throws SQLException {
		return dm.getIndexInfo(params.catalogPattern, params.schemaPattern, params.tablePattern, false, false);
	}
	
	protected ResultSet getSequences(Connection connection,DatabaseMetaData dm,MetadataParameters params) throws SQLException {
		throw new IllegalStateException("reading sequences metadata not supported");
	}

	protected String getSchemaCatalog(ResultSet rs) throws SQLException {
		return Strings.trimToNull(rs.getString(SCHEMA_CATALOG));
	}
	
	protected String getSchemaName(ResultSet rs) throws SQLException {
		return Strings.trimToNull(rs.getString(SCHEMA_NAME));
	}
	
	protected String getTableCatalog(ResultSet rs) throws SQLException {
		return Strings.trimToNull(rs.getString(TABlE_CATALOG));
	}
	
    protected String getTableSchema(ResultSet rs) throws SQLException {
    	return Strings.trimToNull(rs.getString(TABLE_SCHEMA));
    }
    
    protected String getColumnSchema(ResultSet rs) throws SQLException {
    	return Strings.trimToNull(rs.getString(TABLE_SCHEMA));
    }
    
    protected String getPrimaryKeySchema(ResultSet rs) throws SQLException {
    	return Strings.trimToNull(rs.getString(TABLE_SCHEMA));
    }
    
    protected String getForeignKeySchema(ResultSet rs) throws SQLException {
    	return Strings.trimToNull(rs.getString(FKTABLE_SCHEMA));
    }
    
    protected String getIndexSchema(ResultSet rs) throws SQLException {
    	return Strings.trimToNull(rs.getString(TABLE_SCHEMA));
    }
	
	public String getDefaultCatalogPattern() {
		return defaultCatalogPattern;
	}
	
	public String getDefaultSchemaPattern() {
		return defaultSchemaPattern;
	}

	public String getDefaultTablePattern() {
		return defaultTablePattern;
	}
	
	public String getDefaultColumnPattern() {
		return defaultColumnPattern;
	}
	
	public void setDefaultColumnPattern(String defaultColumnPattern) {
		this.defaultColumnPattern = defaultColumnPattern;
	}

	public String[] getDefaultTableTypes() {
		return defaultTableTypes;
	}
	
	protected int readPosition(ResultSet rs,String positionColumnName) throws SQLException{
		int index = -1;
        ResultSetMetaData rsm = rs.getMetaData();
        for(int i=1;i<=rsm.getColumnCount();i++){
        	String columnName = rs.getMetaData().getColumnName(i);
        	
        	if(columnName.equalsIgnoreCase(positionColumnName)){
        		index = rs.getInt(i) - 1;
        	}
        }
        return index;
	}
	
	protected String generateUpdateRuleClause() {
		return "CASE WHEN R.UPDATE_RULE='CASCADE' THEN " + String.valueOf(importedKeyCascade) 
				+ " WHEN R.UPDATE_RULE='SET NULL' THEN " + String.valueOf(importedKeySetNull)  
				+ " WHEN R.UPDATE_RULE='SET DEFAULT' THEN " + String.valueOf(importedKeySetDefault) 
				+ " WHEN R.UPDATE_RULE='RESTRICT' THEN " + String.valueOf(importedKeyRestrict)
				+ " WHEN R.UPDATE_RULE='NO ACTION' THEN " + String.valueOf(importedKeyNoAction)
				+ " ELSE " + String.valueOf(importedKeyNoAction) + " END ";
	}	
	
	protected String generateDeleteRuleClause() {
		return "CASE WHEN R.DELETE_RULE='CASCADE' THEN " + String.valueOf(importedKeyCascade) 
				+ " WHEN R.DELETE_RULE='SET NULL' THEN " + String.valueOf(importedKeySetNull)  
				+ " WHEN R.DELETE_RULE='SET DEFAULT' THEN " + String.valueOf(importedKeySetDefault) 
				+ " WHEN R.DELETE_RULE='RESTRICT' THEN " + String.valueOf(importedKeyRestrict)
				+ " WHEN R.DELETE_RULE='NO ACTION' THEN " + String.valueOf(importedKeyNoAction)
				+ " ELSE " + String.valueOf(importedKeyNoAction) + " END ";
	}	
	
	protected MetadataParameters createMetadataParameters(Connection connection,DatabaseMetaData dm,String catalog,String schema) {
		MetadataParameters p = new MetadataParameters();
		
		p.catalog 		 = catalog;
		p.catalogPattern = Strings.isEmpty(catalog) ? getDefaultCatalogPattern() : catalog;
		p.schema         = schema;
		p.schemaPattern  = Strings.isEmpty(schema)  ? getDefaultSchemaPattern()  : schema;
		p.tablePattern   = getDefaultTablePattern();
		p.tableTypes     = getDefaultTableTypes();
		
		return p;
 	}
	
	protected static class MetadataParameters {
		public String   catalog;
		public String   catalogPattern;
		public String   schema;
		public String   schemaPattern;
		public String   tablePattern;
		public String[] tableTypes;
	}
}