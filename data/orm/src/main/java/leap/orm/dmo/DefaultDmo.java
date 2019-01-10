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
package leap.orm.dmo;

import leap.db.Db;
import leap.db.model.DbSchema;
import leap.db.model.DbSchemaBuilder;
import leap.db.model.DbSequence;
import leap.db.model.DbTable;
import leap.lang.Args;
import leap.lang.Confirm;
import leap.lang.Strings;
import leap.lang.tostring.ToStringBuilder;
import leap.orm.OrmContext;
import leap.orm.command.CreateEntityCommand;
import leap.orm.command.CreateTableCommand;
import leap.orm.command.DropTableCommand;
import leap.orm.command.UpgradeSchemaCommand;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.EntityNotFoundException;
import leap.orm.mapping.MappingNotFoundException;
import leap.orm.mapping.SequenceMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultDmo extends DmoBase {

	public DefaultDmo(){
		super();
	}

	public DefaultDmo(String name){
		super(name);
	}

    public DefaultDmo(OrmContext context) {
        super(context.getName());
        this.ormContext = context;
    }

	@Override
    public List<DbSchema> getDbSchemas() {
	    return createDbSchemas();
    }

	@Override
    public void truncate(Class<?> entityClass) {
	    Args.notNull(entityClass,"entity class");

	    EntityMapping em = em(entityClass);

	    Confirm.checkConfirmed("Dmo.truncate","may lost all the data in table '" + em.getTableName() + "'");

	    commandFactory().newTruncateEntityCommand(this, em).execute();
    }

	@Override
    public boolean createTableIfNotExists(Class<?> entityClass) {
	    Args.notNull(entityClass, "entity class");
	    return commandFactory().newCreateTableCommand(this, em(entityClass)).execute();
    }

    @Override
    public CreateEntityCommand cmdCreateEntity(Class<?> entityClass) {
		Args.notNull(entityClass,"entity class");
	    return commandFactory().newCreateEntityCommand(this, entityClass);
    }

	@Override
	public CreateTableCommand cmdCreateTable(Class<?> entityClass) {
		Args.notNull(entityClass, "entity class");
		return commandFactory().newCreateTableCommand(this, em(entityClass));
	}

    @Override
    public CreateTableCommand cmdCreateTable(EntityMapping em) {
        Args.notNull(em, "entity mapping");
        return commandFactory().newCreateTableCommand(this, em);
    }

    @Override
	public DropTableCommand cmdDropTable(Class<?> entityClass) {
		Args.notNull(entityClass, "entity class");
		return commandFactory().newDropTableCommand(this, em(entityClass));
	}

	@Override
    public UpgradeSchemaCommand cmdUpgradeSchema(Class<?> entityClass) throws EntityNotFoundException {
	    Args.notNull(entityClass,"entity class");
        return commandFactory().newUpgradeSchemaCommand(this, em(entityClass));
    }

    @Override
    public UpgradeSchemaCommand cmdUpgradeSchema(EntityMapping em) {
        Args.notNull(em, "entity mapping");
        return commandFactory().newUpgradeSchemaCommand(this, em);
    }

    @Override
    public UpgradeSchemaCommand cmdUpgradeSchema() {
		return commandFactory().newUpgradeSchemaCommand(this);
    }

    protected List<DbSchema> createDbSchemas() {
    	Db db = ormContext.getDb();

    	List<EntityMapping>   entityMappings   = ormContext.getMetadata().getEntityMappingSnapshotList();
    	List<SequenceMapping> sequenceMappings = ormContext.getMetadata().getSequenceMappingSnapshotList();

		DbSchemaBuilder 		     defaultSchema = new DbSchemaBuilder(db.getMetadata().getDefaultSchemaName());
		Map<String, DbSchemaBuilder> extraSchemas  = null;

		//build tables
		for(int i=0;i<entityMappings.size();i++){
			EntityMapping em = entityMappings.get(i);
			if(em.isRemote() || em.isNarrowEntity()){
				continue;
			}
			DbTable       table = em.getTable();
            DbTable       secondaryTable = em.getSecondaryTable();

			String schemaName = table.getSchema();

			if(Strings.isEmpty(schemaName)){
				defaultSchema.addTable(table);
                if(null != secondaryTable) {
                    defaultSchema.addTable(secondaryTable);
                }
			}else{
				if(null == extraSchemas){
					extraSchemas = new HashMap<>();
				}

				DbSchemaBuilder schema = extraSchemas.get(schemaName.toLowerCase());
				if(null == schema){
					schema = new DbSchemaBuilder(schemaName).setCatalog(table.getCatalog());
					extraSchemas.put(schemaName.toLowerCase(), schema);
				}

				schema.addTable(table);

                if(null != secondaryTable) {
                    schema.addTable(secondaryTable);
                }
			}
		}

		//build sequences
		for(int i=0;i<sequenceMappings.size();i++){
			SequenceMapping sm  = sequenceMappings.get(i);
			DbSequence      seq = sm.getSequence();

			String schemaName = seq.getSchema();
			if(Strings.isEmpty(schemaName)){
				defaultSchema.addSequence(seq);
			}else{
				if(null == extraSchemas){
					extraSchemas = new HashMap<String, DbSchemaBuilder>();
				}
				DbSchemaBuilder schema = extraSchemas.get(schemaName.toLowerCase());
				if(null == schema){
					schema = new DbSchemaBuilder(schemaName).setCatalog(seq.getCatalog());
					extraSchemas.put(schemaName.toLowerCase(), schema);
				}

				schema.addSequence(seq);
			}
		}

		List<DbSchema> schemas = new ArrayList<DbSchema>();

		schemas.add(defaultSchema.build());

		if(null != extraSchemas){
			for(DbSchemaBuilder schema : extraSchemas.values()){
				schemas.add(schema.build());
			}
		}

	    return schemas;
    }

	protected EntityMapping em(String name) throws MappingNotFoundException {
		return metadata().getEntityMapping(name);
	}

	protected EntityMapping em(Class<?> type) throws MappingNotFoundException {
		return metadata().getEntityMapping(type);
	}

	@Override
    public String toString() {
		ToStringBuilder tsb = new ToStringBuilder(this);

		if(null != ormContext){
			tsb.append("dataSource",ormContext.getDataSource());
		}

		return tsb.toString();
	}
}