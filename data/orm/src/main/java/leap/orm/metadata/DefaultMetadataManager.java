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
package leap.orm.metadata;

import leap.core.AppContext;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.db.Db;
import leap.db.model.DbSchemaBuilder;
import leap.lang.Args;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.time.StopWatch;
import leap.orm.DefaultOrmMetadata;
import leap.orm.OrmConfig;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.domain.Domains;
import leap.orm.mapping.*;
import leap.orm.naming.NamingStrategy;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlConfigContext;
import leap.orm.sql.SqlFactory;
import leap.orm.sql.SqlSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultMetadataManager implements OrmMetadataManager {
	
	private static final Log log = LogFactory.get(DefaultMetadataManager.class);
	
    protected @Inject @M BeanFactory beanFactory;
    protected @Inject @M Mapper[]    mappers;
    protected @Inject @M SqlSource[] sqlSources;
    protected @Inject @M SqlFactory  sqlFactory;
    protected @Inject @M Domains     domains;
    protected @Inject @M SqlRegistry sqlRegistry;
    
	@Override
    public OrmMetadata createMetadata() {
	    DefaultOrmMetadata md = new DefaultOrmMetadata();

        md.setDomains(domains);
        md.setSqlRegistry(sqlRegistry);

        return md;
    }

    @Override
    public void createEntity(MetadataContext context, EntityMapping em) throws MetadataException {
        OrmMetadata md = context.getMetadata();

        //Adds entity to metadata.
        md.addEntityMapping(em);
            
        //try create default sql commands
        tryCreateDefaultSqlCommands(context, em);
    }

    @Override
    public void loadMetadata(final OrmContext context) throws MetadataException {
        log.debug("Loading metadata for orm context '{}'...", context.getName());

		LoadingContext loadingContext = new LoadingContext(context);
		
		StopWatch sw = StopWatch.startNew();
		
		//loading entity mappings
		for(Mapper loader : mappers){
			loader.loadMappings(loadingContext);
		}

        //processing entity mappings.
        processMappings(loadingContext);

        //create mappings.
		loadingContext.buildMappings();
		log.debug("Load {} entities used {}ms",context.getMetadata().getEntityMappingSize(),sw.getElapsedMilliseconds());
		
		sw.restart();
		
		//init sql commands
		for(SqlSource ss : sqlSources){
			ss.loadSqlCommands(loadingContext);
		}

		log.debug("Load {} sqls used {}ms",context.getMetadata().getSqlCommandSize(),sw.getElapsedMilliseconds());

		//create default sql commands for all entities.
        DbSchemaBuilder schema = new DbSchemaBuilder(context.getName());
		for(EntityMapping em : context.getMetadata().getEntityMappingSnapshotList()){
		    tryCreateDefaultSqlCommands(loadingContext, em);
            tryCreateTable(loadingContext, em, schema);
		}

        if(!schema.getTables().isEmpty()) {
            context.getDb().cmdCreateSchema(schema.build()).execute();
        }

        //preparing sql commands.
        for(SqlCommand command : context.getMetadata().getSqlCommandSnapshotList()) {
            command.prepare(context);
        }
    }

    @Override
    public void processMappings(MappingConfigContext context) {
        //post loading entity mappings
        for(Mapper loader : mappers){
            loader.postMappings(context);
        }

        //complete loading entity mappings
        for(Mapper loader : mappers){
            loader.completeMappings(context);
        }
    }

    protected void tryCreateDefaultSqlCommands(MetadataContext context, EntityMapping em) {
        tryCreateInsertCommand(context, em);
        tryCreateUpdateCommand(context, em);
        tryCreateDeleteCommand(context, em);
        tryCreateDeleteAllCommand(context,em);
        tryCreateFindCommand(context, em);
        tryCreateFindListCommand(context, em);
        tryCreateFindAllCommand(context,em);
        tryCreateExistsCommand(context, em);
        tryCreateCountCommand(context, em);
    }

    protected void tryCreateTable(MetadataContext context, EntityMapping em, DbSchemaBuilder schema) {
        if(em.isAutoCreateTable() || context.getConfig().isAutoCreateTables()) {
            if(!context.getDb().checkTableExists(em.getTable())){
                log.info("Will auto create table '{}' of entity '{}", em.getTableName(), em.getEntityName());
                schema.addTable(em.getTable());
            }
        }
    }
	
	protected void tryCreateInsertCommand(MetadataContext context,EntityMapping em) {
		SqlCommand cmd = context.getMetadata().tryGetSqlCommand(em.getEntityName(), SqlCommand.INSERT_COMMAND_NAME);
		if(null == cmd){
			tryAddSqlCommand(context, em, SqlCommand.INSERT_COMMAND_NAME, sqlFactory.createInsertCommand(context,em));
		}
	}
	
	protected void tryCreateUpdateCommand(MetadataContext context,EntityMapping em) {
		SqlCommand cmd = context.getMetadata().tryGetSqlCommand(em.getEntityName(), SqlCommand.UPDATE_COMMAND_NAME);
		if(null == cmd){
			tryAddSqlCommand(context, em, SqlCommand.UPDATE_COMMAND_NAME, sqlFactory.createUpdateCommand(context,em));
		}
	}
	
	protected void tryCreateDeleteCommand(MetadataContext context,EntityMapping em) {
		SqlCommand cmd = context.getMetadata().tryGetSqlCommand(em.getEntityName(), SqlCommand.DELETE_COMMAND_NAME);
		if(null == cmd){
			tryAddSqlCommand(context,em, SqlCommand.DELETE_COMMAND_NAME, sqlFactory.createDeleteCommand(context,em));
		}
	}
	
	private void tryCreateDeleteAllCommand(MetadataContext context, EntityMapping em) {
		SqlCommand cmd = context.getMetadata().tryGetSqlCommand(em.getEntityName(), SqlCommand.DELETE_ALL_COMMAND_NAME);
		if(null == cmd){
			tryAddSqlCommand(context,em, SqlCommand.DELETE_ALL_COMMAND_NAME, sqlFactory.createDeleteAllCommand(context,em));
		}
    }
	
	protected void tryCreateFindCommand(MetadataContext context,EntityMapping em) {
		SqlCommand cmd = context.getMetadata().tryGetSqlCommand(em.getEntityName(), SqlCommand.FIND_COMMAND_NAME);
		if(null == cmd){
			tryAddSqlCommand(context,em, SqlCommand.FIND_COMMAND_NAME, sqlFactory.createFindCommand(context,em));
		}
	}
	
	protected void tryCreateFindListCommand(MetadataContext context,EntityMapping em) {
		if(!em.isCompositeKey()) {
			SqlCommand cmd = context.getMetadata().tryGetSqlCommand(em.getEntityName(), SqlCommand.FIND_LIST_COMMAND_NAME);
			if(null == cmd){
				tryAddSqlCommand(context,em, SqlCommand.FIND_LIST_COMMAND_NAME, sqlFactory.createFindListCommand(context,em));
			}
		}
	}
	
	protected void tryCreateFindAllCommand(MetadataContext context, EntityMapping em) {
		SqlCommand cmd = context.getMetadata().tryGetSqlCommand(em.getEntityName(), SqlCommand.FIND_ALL_COMMAND_NAME);
		if(null == cmd){
			tryAddSqlCommand(context,em, SqlCommand.FIND_ALL_COMMAND_NAME, sqlFactory.createFindAllCommand(context,em));
		}
    }
	
	protected void tryCreateExistsCommand(MetadataContext context, EntityMapping em) {
		SqlCommand cmd = context.getMetadata().tryGetSqlCommand(em.getEntityName(), SqlCommand.EXISTS_COMMAND);
		if(null == cmd){
			tryAddSqlCommand(context,em, SqlCommand.EXISTS_COMMAND, sqlFactory.createExistsCommand(context,em));
		}
    }
	
	protected void tryCreateCountCommand(MetadataContext context, EntityMapping em) {
		SqlCommand cmd = context.getMetadata().tryGetSqlCommand(em.getEntityName(), SqlCommand.COUNT_COMMAND);
		if(null == cmd){
			tryAddSqlCommand(context,em, SqlCommand.COUNT_COMMAND, sqlFactory.createCountCommand(context,em));
		}
    }
	
	protected void tryAddSqlCommand(MetadataContext context, EntityMapping em, String name, SqlCommand command) {
		if(null != command) {
			context.getMetadata().addSqlCommand(em, name, command);
		}
	}

	protected static class LoadingContext implements MappingConfigContext,SqlConfigContext {
		
		private final OrmContext	 				     ormContext;
		private final Map<Class<?>,EntityMappingBuilder> classToEntityMappings = new ConcurrentHashMap<>();
		private final Map<String,EntityMappingBuilder>   nameToEntityMappings  = new ConcurrentHashMap<>();

		protected LoadingContext(OrmContext ormContext){
			this.ormContext = ormContext;
		}

        @Override
        public String getName() {
            return ormContext.getName();
        }

        @Override
        public OrmConfig getConfig() {
	        return ormContext.getConfig();
        }

		@Override
        public AppContext getAppContext() {
	        return ormContext.getAppContext();
        }

		@Override
        public OrmContext getOrmContext() {
	        return ormContext;
        }
		
		@Override
        public OrmMetadataManager getMetadataManager() {
            return ormContext.getMetadataManager();
        }

        @Override
        public Iterable<EntityMappingBuilder> getEntityMappings() {
	        return nameToEntityMappings.values();
        }

		@Override
        public void addEntityMapping(EntityMappingBuilder emb) {
	        Args.notNull(emb,"entity mapping builder");
	        Args.notEmpty(emb.getEntityName(),"entity name");

	        if(nameToEntityMappings.containsKey(emb.getEntityName().toLowerCase())){
	        	throw new MappingExistsException("entity name '" + emb.getEntityName() + "' already exists in this mapping context");
	        }
	        
	        nameToEntityMappings.put(emb.getEntityName().toLowerCase(), emb);
	        
	        if(null != emb.getEntityClass()){
	        	if(classToEntityMappings.containsKey(emb.getEntityClass())){
	        		throw new MappingExistsException("entity class '" + emb.getEntityClass().getName() + "' already exists in this mapping context");
	        	}
	        	classToEntityMappings.put(emb.getEntityClass(), emb);
	        }
        }

		@Override
        public EntityMappingBuilder getEntityMapping(String entityName) throws MappingNotFoundException {
			EntityMappingBuilder emb = tryGetEntityMapping(entityName);
			
			if(null == emb){
				throw new MappingNotFoundException("entity '" + entityName + "' not found in this mapping context");
			}
			
	        return emb;
        }

		@Override
        public EntityMappingBuilder getEntityMapping(Class<?> entityClass) throws MappingNotFoundException {
			EntityMappingBuilder emb = tryGetEntityMapping(entityClass);
			
			if(null == emb){
				throw new MappingNotFoundException("no entity mapped to the class '" + entityClass.getName() + "' in this mapping context");
			}
			
	        return emb;
        }

		@Override
        public EntityMappingBuilder tryGetEntityMapping(String entityName) {
			Args.notEmpty(entityName,"entity name");
	        return nameToEntityMappings.get(entityName.toLowerCase());
        }

		@Override
        public EntityMappingBuilder tryGetEntityMapping(Class<?> entityClass) {
			Args.notNull(entityClass,"entity class");
	        return classToEntityMappings.get(entityClass);
        }

		@Override
        public Db getDb() {
	        return ormContext.getDb();
        }

		@Override
        public OrmMetadata getMetadata() {
	        return ormContext.getMetadata();
        }
		
		@Override
        public MappingStrategy getMappingStrategy() {
	        return ormContext.getMappingStrategy();
        }

		@Override
        public NamingStrategy getNamingStrategy() {
	        return ormContext.getNamingStrategy();
        }

		protected void buildMappings(){
			OrmMetadata metadata = ormContext.getMetadata();
			for(EntityMappingBuilder emb : nameToEntityMappings.values()){
				metadata.addEntityMapping(emb.build());
			}
		}
	}
}