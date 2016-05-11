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

import leap.db.Db;
import leap.db.DbAware;
import leap.db.DbMetadata;
import leap.db.DbMetadataReader;
import leap.db.model.*;
import leap.lang.*;
import leap.lang.logging.Log;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GenericDbMetadata implements DbMetadata,DbAware {
	
	protected Log log;
	
	private final Object cachedExtraSchemaNamesMonitor = new Object();
	private final Object cachedSchemasMonitor          = new Object();
	private final Object asyncSchemaReaderMonitor      = new Object();
	
	private Thread asyncSchemaReaderThread;
	
    protected final String           productName;
    protected final String           productVersion;
    protected final int              productMajorVersion;
    protected final int              productMinorVersion;
    protected final String[]         sqlKeywords;
    protected final String           identifierQuoteString;
    protected final boolean          supportsMixedCaseIdentifiers;
    protected final boolean          supportsAlterTableWithAddColumn;
    protected final boolean          supportsAlterTableWithDropColumn;
    protected final int              maxTableNameLength;
    protected final int              maxColumnNameLength;
    protected final String			 catalog;
    protected final String           defaultSchemaName;
    protected final DbMetadataReader metadataReader;
    
    protected GenericDb 		   db;
    protected Boolean			   driverSupportsGetParameterType;
    protected DbSchemaName[]       cachedExtraSchemaNames;
    protected Map<String,DbSchema> cachedSchemas = new ConcurrentHashMap<String, DbSchema>(2);
    
	public GenericDbMetadata(DatabaseMetaData dm,String defaultSchemaName,DbMetadataReader metadataReader) throws SQLException {
		Args.notNull(dm,"DatabaseMetaData");
		Args.notEmpty(defaultSchemaName,"default schema name");
		Args.notNull(metadataReader,"metadata reader");
		
        this.productName                      = dm.getDatabaseProductName();
        this.productVersion                   = dm.getDatabaseProductVersion();
        this.productMajorVersion              = dm.getDatabaseMajorVersion();
        this.productMinorVersion              = dm.getDatabaseMinorVersion();
        this.sqlKeywords                      = Strings.split(dm.getSQLKeywords(), ",");
        this.identifierQuoteString            = dm.getIdentifierQuoteString();
        this.supportsMixedCaseIdentifiers     = dm.supportsMixedCaseIdentifiers();
        this.supportsAlterTableWithAddColumn  = dm.supportsAlterTableWithAddColumn();
        this.supportsAlterTableWithDropColumn = dm.supportsAlterTableWithDropColumn();
        this.maxTableNameLength				  = dm.getMaxTableNameLength();
        this.maxColumnNameLength			  = dm.getMaxColumnNameLength();
        this.catalog						  = dm.getConnection().getCatalog();
        this.defaultSchemaName				  = defaultSchemaName;
        this.metadataReader					  = metadataReader;
	}
	
	@Override
    public synchronized void setDb(Db db) {
		Assert.isTrue(this.db == null);
		this.db  = (GenericDb)db;
		this.log = this.db.getLog(this.getClass());
    }

	@Override
	public String getProductName() {
		return productName;
	}

	@Override
	public String getProductVersion() {
		return productVersion;
	}

	@Override
	public int getProductMajorVersion() {
		return productMajorVersion;
	}

	@Override
	public int getProductMinorVersion() {
		return productMinorVersion;
	}

	@Override
	public String[] getSQLKeywords() {
		return sqlKeywords;
	}

	@Override
	public String getIdentifierQuoteString() {
		return identifierQuoteString;
	}

	@Override
    public boolean supportsMixedCaseIdentifiers() {
	    return supportsMixedCaseIdentifiers;
    }

	@Override
    public boolean supportsAlterTableWithAddColumn() {
	    return supportsAlterTableWithAddColumn;
    }

	@Override
    public boolean supportsAlterTableWithDropColumn() {
	    return supportsAlterTableWithDropColumn;
    }

	@Override
    public boolean driverSupportsGetParameterType() {
		if(null == driverSupportsGetParameterType) {
			driverSupportsGetParameterType = db.getDialect().testDriverSupportsGetParameterType();
		}
	    return driverSupportsGetParameterType;
    }

	@Override
    public int getMaxTableNameLength() {
	    return maxTableNameLength;
    }

	@Override
    public int getMaxColumnNameLength() {
	    return maxColumnNameLength;
    }
	
	@Override
    public DbSchemaName[] getExtraSchemaNames() {
		if(null == cachedExtraSchemaNames){
			synchronized (cachedExtraSchemaNamesMonitor) {
	            if(null == cachedExtraSchemaNames){

                    db.execute((conn) -> {
                        List<DbSchemaName> allSchemaNames = New.arrayList(metadataReader.readSchemaNames(conn));

                        DbSchemaName defaultSchemaName = null;

                        for(DbSchemaName schemaName : allSchemaNames){
                            if(Strings.equalsIgnoreCase(schemaName.getName(), getDefaultSchemaName())){
                                defaultSchemaName = schemaName;
                                break;
                            }
                        }

                        if(null != defaultSchemaName){
                            allSchemaNames.remove(defaultSchemaName);
                        }

                        cachedExtraSchemaNames = allSchemaNames.toArray(new DbSchemaName[allSchemaNames.size()]);
                    });
	            }
            }
		}
	    return cachedExtraSchemaNames;
    }
	
	@Override
    public String getDefaultSchemaName() {
	    return defaultSchemaName;
    }
	
	@Override
    public DbSchema getSchema() {
	    return getSchema(getDefaultSchemaName());
    }

	@Override
    public DbSchema getSchema(String schemaName) {
	    return getSchema(this.catalog,schemaName);
    }

	@Override
    public DbSchema getSchema(String catalog, String schemaName) {
		schemaName = Strings.isEmpty(schemaName) ? getDefaultSchemaName() : schemaName;
		
		String key = Strings.isEmpty(catalog) ? schemaName : catalog + "%" + schemaName;
		
		log.trace("Try to get the schema '{}' from cache...",schemaName);
		
		synchronized (cachedSchemasMonitor) {
			DbSchema cachedSchema = cachedSchemas.get(key);
			
			if(null == cachedSchema){
				log.debug("Schema '{}' not cached,read from db...",schemaName);

                final String theSchemaName = schemaName;

                return db.executeWithResult((conn) -> {
                    DbSchema schema = metadataReader.readSchema(conn, catalog, theSchemaName);
                    cachedSchemas.put(key, schema);
                    return schema;
                });
			}else{
				log.trace("Schema '{}' was cached,just return it",schemaName);
                return cachedSchema;
			}
		}
    }
	
	@Override
    public DbTable tryGetTable(String tableName) {
		Args.notEmpty(tableName, "table name");
	    return getSchema().findTable(tableName);
    }

	@Override
    public DbTable tryGetTable(String schema, String tableName) {
		Args.notEmpty(tableName, "table name");
	    return getSchema(schema).findTable(tableName);
    }
	
	@Override
    public DbTable tryGetTable(DbSchemaObjectName tableName) {
		Args.notNull(tableName,"table name");
		String schema = Strings.isEmpty(tableName.getSchema()) ? getDefaultSchemaName() : tableName.getSchema();
	    return getSchema(tableName.getCatalog(),schema).findTable(tableName.getName());
    }
	
	@Override
    public DbSequence tryGetSequence(String sequenceName) {
		Args.notEmpty(sequenceName,"sequence name");
		return getSchema().findSequence(sequenceName);
    }

	@Override
    public DbSequence tryGetSequence(String schema, String sequenceName) {
		Args.notEmpty(sequenceName,"sequence name");
	    return getSchema(schema).findSequence(sequenceName);
    }

	@Override
    public DbSequence tryGetSequence(DbSchemaObjectName sequenceName) {
		Args.notNull(sequenceName,"sequence name");
		String schema = Strings.isEmpty(sequenceName.getSchema()) ? getDefaultSchemaName() : sequenceName.getSchema();
	    return getSchema(sequenceName.getCatalog(),schema).findSequence(sequenceName.getName());
    }

	@Override
    public DbMetadata refresh() {
		refreshSchemasAsync();
		return this;
    }

	protected void refreshSchemasAsync(){
		cachedSchemas.clear();
		
        if(null != asyncSchemaReaderThread){
            try {
            	int times = 0;
            	synchronized (asyncSchemaReaderMonitor) {
            		while(asyncSchemaReaderThread.isAlive()){
            			log.debug("Waiting schema reader thread '{}' to finish",asyncSchemaReaderThread.getName());
            			asyncSchemaReaderMonitor.wait(500);
            			times += 500;
            			if(times >= 100000){
            				log.error("Waiting schema reader thread '{}' timeout",asyncSchemaReaderThread.getName());
            				break;
            			}
            		}
            		if(asyncSchemaReaderThread.isAlive() && !asyncSchemaReaderThread.isInterrupted()){
            			asyncSchemaReaderThread.interrupt();
            		}
                }
            } catch (Throwable e) {
            	;
            }
        }
        
        asyncSchemaReaderThread = new Thread(){
            @Override
            public void run() {
                try {
                    getSchema();
                } catch (Throwable e) {
                	log.info("Error reading schema : {}",e.getMessage(),e);
                } finally{
                	synchronized (asyncSchemaReaderMonitor) {
                		asyncSchemaReaderMonitor.notifyAll();
                		log.debug("Notify schema reader finished");
                    }
                }
            }
        };
        
        asyncSchemaReaderThread.setDaemon(true);
        asyncSchemaReaderThread.setName("dbsr-" + Dates.format(new Date(),"HHmmss.SSS"));
        asyncSchemaReaderThread.start();
	}
}