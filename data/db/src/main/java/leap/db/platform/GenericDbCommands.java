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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import leap.db.DbCommand;
import leap.db.DbCommands;
import leap.db.DbExecution;
import leap.db.command.*;
import leap.db.model.DbColumn;
import leap.db.model.DbForeignKey;
import leap.db.model.DbIndex;
import leap.db.model.DbPrimaryKey;
import leap.db.model.DbSchema;
import leap.db.model.DbSchemaObjectName;
import leap.db.model.DbSequence;
import leap.db.model.DbTable;
import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Assert;
import leap.lang.Collections2;
import leap.lang.Confirm;
import leap.lang.New;
import leap.lang.collection.ListEnumerable;
import leap.lang.exception.ObjectExistsException;
import leap.lang.jdbc.ConnectionCallbackWithResult;

public class GenericDbCommands extends ListEnumerable<DbCommand> implements DbCommands{
	
	public static final Comparator<DbCommand> COMMAND_COMPARATOR = new Comparator<DbCommand>() {
		@Override
		public int compare(DbCommand c1, DbCommand c2) {
			if(c1 != null && c2 != null){
				if(c1.getSortOrder() > c2.getSortOrder()){
					return 1;
				}
			}
			return -1;
		}
	};
	
	protected final GenericDb db;
	
	public GenericDbCommands(GenericDb db) {
	    super(new ArrayList<DbCommand>());
	    this.db = db;
    }
	
	public GenericDbCommands(GenericDb db, List<DbCommand> commands) {
	    super(commands);
	    this.db = db;
    }
	
	public GenericDbCommands add(DbCommand command){
		l.add(command);
		return this;
	}
	
	public GenericDbCommands addAll(Collection<DbCommand> commands){
		l.addAll(commands);
		return this;
	}
	
	@Override
    public DbCommands filter(Predicate<DbCommand> predicate) {
		GenericDbCommands filtered = new GenericDbCommands(db);
		
		for(DbCommand command : this){
			if(predicate.test(command)){
				filtered.add(command);
			}
		}
		
		return filtered;
    }

	@Override
    public String[] getExecutionScripts() {
		List<String> sqls = new ArrayList<String>();

		List<DbCommand> commands = new ArrayList<DbCommand>(l);
		Collections.sort(commands, COMMAND_COMPARATOR);
		
		for(DbCommand command : commands){
			sqls.addAll(command.sqls());
		}
		
	    return sqls.toArray(new String[sqls.size()]);
    }

	@Override
    public DbExecution execute() {
		return db.executeWithResult(new ConnectionCallbackWithResult<DbExecution>() {
			@Override
            public DbExecution execute(Connection connection) throws SQLException {
	            return GenericDbCommands.this.execute(connection);
            }
		});
    }

	@Override
    public DbExecution execute(Connection connection) {
	    DbExecution execution = new GenericDbExecution(db).addAll(getExecutionScripts());
	    
	    execution.execute(connection);
	    
	    return execution;
    }

	public static abstract class GenericDbCommand implements DbCommand {
		
		protected final GenericDb        db;
		protected final GenericDbDialect dialect;
		
		protected boolean throwExceptionOnExecuting = true;
		
		protected GenericDbCommand(GenericDb db){
			this.db      = db;
			this.dialect = db.getDialect();
		}
		
		@Override
        public DbCommand setThrowExceptionOnExecuting(boolean throwExceptionOnExecuting) {
			this.throwExceptionOnExecuting = throwExceptionOnExecuting;
	        return this;
        }

		public DbExecution execute(){
			return db.executeWithResult(new ConnectionCallbackWithResult<DbExecution>() {
				@Override
                public DbExecution execute(Connection connection) throws SQLException {
	                return GenericDbCommand.this.execute(connection);
                }
			});
		}
		
		public DbExecution execute(Connection connection) {
			DbExecution execution = new GenericDbExecution(db).addAll(sqls()).setThrowExceptionOnExecuting(throwExceptionOnExecuting);
			
			execution.execute(connection);
			
			return execution;
		}
	}
	
	public static abstract class GenericAlterColumnBase extends GenericDbCommand {
		
		protected final DbSchemaObjectName tableName;
		protected final String		 	   columnName;
		
		//protected DbTable  table;
		//protected DbColumn column;
		
		public GenericAlterColumnBase(GenericDb db,DbSchemaObjectName tableName,String columnName) {
	        super(db);
	        
	        Args.notNull(tableName,"table name");
	        Args.notEmpty(columnName,"column name");
	        
	        this.tableName  = tableName;
	        this.columnName = columnName;
	        
	        /*
	        this.table = db.getMetadata().tryGetTable(tableName);
	        Assert.isTrue(this.table != null,"the given table '" + tableName.toString() + "' not exists");

	        this.column = table.findColumn(columnName);
	        Assert.isTrue(this.column != null,"column '" + columnName + "' not exists in table '" + table.getName() + "'");
	        */
        }
		
		public DbSchemaObjectName getTableName() {
			return tableName;
		}
		
		public String getColumnName(){
			return columnName;
		}
	}
	
	public static class GenericSqlCommand extends GenericDbCommand {
		
		protected final List<String> sqls;

		public GenericSqlCommand(GenericDb db,String... sqls) {
	        super(db);
	        this.sqls = New.arrayList(sqls);
        }
		
		public GenericSqlCommand(GenericDb db,List<String> sqls) {
	        super(db);
	        this.sqls = sqls;
		}
		
		@Override
        public int getSortOrder() {
	        return -1;
        }

		@Override
        public List<String> sqls() {
	        return sqls;
        }
	}

    protected static class GenericCreateSchema extends GenericDbCommand implements CreateSchema {

        private final DbSchema schema;

        public GenericCreateSchema(GenericDb db, DbSchema schema) {
            super(db);
            Args.notNull(schema, "schema");
            this.schema = schema;
        }

        @Override
        public List<String> sqls() {
            List<String> sqls = new ArrayList<>();

            //create sequences
            for(DbSequence seq : schema.getSequences()) {
                sqls.addAll(dialect.getCreateSequenceSqls(seq));
            }

            //create tables.
            for(DbTable table : schema.getTables()) {
                sqls.addAll(dialect.getCreateTableSqls(table));
            }

            //create foreign keys.
            for(DbTable table : schema.getTables()) {
                for(DbForeignKey fk : table.getForeignKeys()) {
                    sqls.addAll(dialect.getCreateForeignKeySqls(table, fk));
                }
            }

            //create indexes.
            for(DbTable table : schema.getTables()) {
                for(DbIndex ix : table.getIndexes()) {
                    sqls.addAll(dialect.getCreateIndexSqls(table, ix));
                }
            }

            return sqls;
        }

        @Override
        public int getSortOrder() {
            return 0;
        }
    }

	protected static class GenericCreateTable extends GenericDbCommand implements CreateTable {
		
		protected final DbTable table;
		
		protected boolean createForeignKey = true;
		protected boolean createIndex      = true;
		
		protected GenericCreateTable(GenericDb db,DbTable table) {
	        super(db);
	        Args.notNull(table,"table");
	        this.table = table;
	        
	        /*
	        if(db.checkTableExists(table)){
	        	throw new ObjectExistsException("The to be created table '" + table.getQualifiedName() + "' aleady exists");
	        }
	        */
        }

		@Override
        public CreateTable setCreateForeignKey(boolean createForeignKey) {
			this.createForeignKey = createForeignKey;
	        return this;
        }

		@Override
        public CreateTable setCreateIndex(boolean createIndex) {
			this.createIndex = createIndex;
			return this;
        }

		@Override
        public CreateTable dontCreateForeignKeyAndIndex() {
			return setCreateForeignKey(false).setCreateIndex(false);
        }
		
		@Override
        public List<String> sqls() {
	        List<String> sqls = New.arrayList(dialect.getCreateTableSqls(table));
	        
			if(createForeignKey && table.hasForeignKeys()){
				for(DbForeignKey fk : table.getForeignKeys()){
					sqls.addAll(dialect.getCreateForeignKeySqls(table, fk));
				}
			}
			
			if(createIndex && table.hasIndexes()){
				for(DbIndex ix : table.getIndexes()){
					sqls.addAll(dialect.getCreateIndexSqls(table, ix));
				}
			}
			
			return sqls;
        }
	}
	
	protected static class GenericAlterTable extends GenericDbCommand implements AlterTable {
		
		protected final DbSchemaObjectName tableName;
		//protected final DbTable     	   table;
		
		protected DbPrimaryKey		 primaryKeyToAdd   = null;
		protected boolean			 dropPrimaryKey    = false;
		protected List<DbColumn>     columnsToAdd      = null;
		protected List<String>       columnsToDrop     = null;
		protected List<DbForeignKey> foreignKeysToAdd  = null;
		protected List<String>       foreignKeysToDrop = null;
		protected List<DbIndex>      indexesToAdd      = null;
		protected List<String>       indexesToDrop     = null;
		protected String 		     commentToChange   = null;

		protected GenericAlterTable(GenericDb db,DbSchemaObjectName tableName) {
	        super(db);
	        Args.notNull(tableName,"table name");
	        this.tableName = tableName;
	        
	        /*
	        this.table     = db.getMetadata().tryGetTable(tableName);
	        
	        if(null == this.table){
	        	throw new IllegalStateException("the to be modified table '" + tableName.getQualifiedName() + "' not exists");
	        }
	        */
        }
		
		@Override
        public AlterTable addPrimaryKey(String... pkColumnNames) throws IllegalStateException {
			Args.notEmpty(pkColumnNames,"primary key columns");
			//Assert.isTrue(table.getPrimaryKeyColumnNames().length == 0,"cannot add primary key : a primary key aleady exists in table '" + table.getName() + "'");
			this.primaryKeyToAdd = new DbPrimaryKey(dialect.generatePrimaryKeyName(tableName, pkColumnNames), pkColumnNames);
	        return this;
        }

		@Override
        public AlterTable dropPrimaryKey() throws IllegalStateException {
			//Assert.isTrue(table.getPrimaryKeyColumnNames().length > 0,"cannot drop primary key : no primary key exists in table '" + table.getName() + "");
			this.dropPrimaryKey = true;
	        return this;
        }

		@Override
        public AlterTable addColumn(DbColumn column) throws IllegalStateException {
			Args.notNull(column);
			
			/*
			Assert.isTrue(table.findColumn(column.getName()) == null,
						   "the to be added column '" + column.getName() + "' aleady exists in table '" + table.getName() + "'");
			*/
			
			if(null == columnsToAdd){
				columnsToAdd = New.arrayList(column);
			}else{
				columnsToAdd.add(column);
			}
			
	        return this;
        }

		@Override
        public AlterTable dropColumn(String columnName) throws IllegalStateException {
			Args.notEmpty(columnName);
			
			/*
			DbColumn column = table.findColumn(columnName);
			Assert.isTrue(column != null,
						   "the to be dropped column '" + columnName + "' not exists in table '" + table.getName() + "'");
			*/
			
			return doDropColumn(columnName);
        }

		@Override
        public AlterTable dropColumn(DbColumn column) throws IllegalStateException {
			Args.notNull(column);
			
			/*
			Assert.isTrue(table.findColumn(column.getName()) != null,
					   	   "the to be dropped column '" + column.getName() + "' not exists in table '" + table.getName() + "'");
			*/
			
			return doDropColumn(column.getName());
        }
		
		protected AlterTable doDropColumn(String columnName){
			if(null == columnsToDrop){
				columnsToDrop = New.arrayList(columnName);
			}else{
				columnsToDrop.add(columnName);
			}
			
	        return this;
		}

		@Override
        public AlterTable addForeignKey(DbForeignKey fk) throws IllegalStateException {
	        Args.notNull(fk);
	        
	        /*
			Assert.isTrue(table.findColumn(fk.getName()) == null,
					       "the to be added foreign key '" + fk.getName() + "' aleady exists in table '" + table.getName() + "'");
			*/
	        
			if(null == foreignKeysToAdd){
				foreignKeysToAdd = New.arrayList(fk);
			}else{
				foreignKeysToAdd.add(fk);
			}
	        
	        return this;
        }

		@Override
        public AlterTable dropForeignKey(String fkName) throws IllegalStateException {
			Args.notEmpty(fkName);
			
			/*
			DbForeignKey fk = table.findForeignKey(fkName);
			
			Assert.isTrue(fk != null,
					       "the to be dropped foreign key '" + fkName + "' not exists in table '" + table.getName() + "'");
			*/
			
			return doDropForeignKey(fkName);
        }

		@Override
        public AlterTable dropForeignKey(DbForeignKey fk) throws IllegalStateException {
			Args.notNull(fk);
			Args.notEmpty(fk.getName(),"foreign key's name");
	        
			/*
			Assert.isTrue(table.findForeignKey(fk.getName()) != null,
				       "the to be dropped foreign key '" + fk.getName() + "' not exists in table '" + table.getName() + "'");
			*/
			
			return doDropForeignKey(fk.getName());
        }
		
		protected AlterTable doDropForeignKey(String fkName){
			
			if(null == foreignKeysToDrop){
				foreignKeysToDrop = New.arrayList(fkName);
			}else{
				foreignKeysToDrop.add(fkName);
			}
			
			return this;
		}

		@Override
        public AlterTable addIndex(DbIndex ix) throws IllegalStateException {
	        Args.notNull(ix);
	        
	        /*
			Assert.isTrue(table.findIndex(ix.getName()) == null,
					       "the to be added index '" + ix.getName() + "' aleady exists in table '" + table.getName() + "'");
			*/
	        
			if(null == indexesToAdd){
				indexesToAdd = New.arrayList(ix);
			}else{
				indexesToAdd.add(ix);
			}
	        
	        return this;
        }

		@Override
        public AlterTable dropIndex(String ixName) throws IllegalStateException {
			Args.notEmpty(ixName);
			
			/*
			DbIndex ix = table.findIndex(ixName);
			
			Assert.isTrue(ix != null,
					       "the to be dropped index '" + ixName + "' not exists in table '" + table.getName() + "'");
			*/
			
			return doDropIndex(ixName);
        }

		@Override
        public AlterTable dropIndex(DbIndex ix) throws IllegalStateException {
			Args.notNull(ix);
			Args.notEmpty(ix.getName(),"index's name");
			
			
			/*
			Assert.isTrue(table.findIndex(ix.getName()) != null,
				       "the to be dropped index '" + ix.getName() + "' not exists in table '" + table.getName() + "'");
			*/
			
			return doDropIndex(ix.getName());
        }
		
		protected AlterTable doDropIndex(String ixName) {
			if(null == indexesToDrop){
				indexesToDrop = New.arrayList(ixName);
			}else{
				indexesToDrop.add(ixName);
			}
			
			return this;
		}
		
		@Override
        public AlterTable changeComment(String comment) {
			Args.notNull(comment,"comment");
			this.commentToChange = comment;
	        return this;
        }
		
		@Override
        public List<String> sqls() {
	        List<String> sqls = new ArrayList<String>();
			
	        //add columns
	        if(null != columnsToAdd){
	        	for(DbColumn column : columnsToAdd){
	        		sqls.addAll(dialect.getCreateColumnSqls(tableName, column));
	        	}
	        }
	        
	        //add primary key
	        if(null != primaryKeyToAdd){
	        	sqls.addAll(dialect.getCreatePrimaryKeySqls(tableName, primaryKeyToAdd));
	        }
	        
	        //add foreign key
	        if(null != foreignKeysToAdd){
	        	for(DbForeignKey fk : foreignKeysToAdd){
	        		sqls.addAll(dialect.getCreateForeignKeySqls(tableName, fk));
	        	}
	        }
	        
	        //add index
	        if(null != indexesToAdd){
	        	for(DbIndex ix : indexesToAdd){
	        		sqls.addAll(dialect.getCreateIndexSqls(tableName, ix));
	        	}
	        }
	        
	        //drop index
	        if(null != indexesToDrop){
	        	for(String ixName : indexesToDrop){
	        		sqls.addAll(dialect.getDropIndexSqls(tableName, ixName));
	        	}
	        }
	        
	        //drop foreign key
	        if(null != foreignKeysToDrop){
	        	for(String fkName : foreignKeysToDrop){
	        		sqls.addAll(dialect.getDropForeignKeySqls(tableName, fkName));
	        	}
	        }
	        
	        //drop primary key
	        if(dropPrimaryKey){
	        	sqls.addAll(dialect.getDropPrimaryKeySqls(tableName));
	        }
	        
	        //drop columns
	        if(null != columnsToDrop){
	        	for(String columnName : columnsToDrop){
	        		sqls.addAll(dialect.getDropColumnSqls(tableName, columnName));
	        	}
	        }
	        
	        //change comment
	        if(null != commentToChange){
	        	sqls.addAll(dialect.getCommentOnTableSqls(tableName, commentToChange));
	        }
	        
	        return sqls;
        }
		
		@Override
        public DbColumn[] getColumnsToAdd() {
	        return null == columnsToAdd ? (DbColumn[])Arrays2.EMPTY_OBJECT_ARRAY : columnsToAdd.toArray(new DbColumn[columnsToAdd.size()]);
        }

		@Override
        public String[] getColumnsToDrop() {
			return null == columnsToDrop ? (String[])Arrays2.EMPTY_OBJECT_ARRAY : columnsToDrop.toArray(new String[columnsToDrop.size()]);
        }

		@Override
        public DbForeignKey[] getForeignKeysToAdd() {
			return null == foreignKeysToAdd ? (DbForeignKey[])Arrays2.EMPTY_OBJECT_ARRAY : foreignKeysToAdd.toArray(new DbForeignKey[foreignKeysToAdd.size()]);
        }

		@Override
        public String[] getForeignKeysToDrop() {
			return null == foreignKeysToDrop ? (String[])Arrays2.EMPTY_OBJECT_ARRAY : foreignKeysToDrop.toArray(new String[foreignKeysToDrop.size()]);
        }

		@Override
        public DbIndex[] getIndexesToAdd() {
			return null == indexesToAdd ? (DbIndex[])Arrays2.EMPTY_OBJECT_ARRAY : indexesToAdd.toArray(new DbIndex[indexesToAdd.size()]);
        }

		@Override
        public String[] getIndexesToDrop() {
			return null == indexesToDrop ? (String[])Arrays2.EMPTY_OBJECT_ARRAY : indexesToDrop.toArray(new String[indexesToDrop.size()]);
        }

		@Override
        public boolean isCommentChanged(){
			return null != commentToChange;
		}
		
		@Override
        public String getCommentToChange() {
			return commentToChange;
		}
		
		@Override
        public boolean hasObjectsToAdd() {
	        return Collections2.isNotEmpty(columnsToAdd) || 
		           Collections2.isNotEmpty(foreignKeysToAdd) ||
		           Collections2.isNotEmpty(indexesToAdd);
        }

		@Override
        public boolean hasObjectsToDrop() {
	        return Collections2.isNotEmpty(columnsToDrop) || 
	        	   Collections2.isNotEmpty(foreignKeysToDrop) ||
	        	   Collections2.isNotEmpty(indexesToDrop);
        }
	}
	
	protected static class GenericDropTable extends GenericDbCommand implements DropTable {

		protected final DbSchemaObjectName tableName;
		//protected final DbTable     	   table;
		
		protected GenericDropTable(GenericDb db,DbSchemaObjectName tableName) {
	        super(db);
	        Args.notNull(tableName,"table name");
	        this.tableName = tableName;
	        
	        /*
	        this.table     = db.getMetadata().tryGetTable(tableName);
	        if(null == this.table){
	        	throw new IllegalStateException("the to be dropped table '" + tableName.getQualifiedName() + "' not exists");
	        }	    
	        */    
        }
		
		@Override
        public DbSchemaObjectName getTableName() {
	        return tableName;
        }

		@Override
        public List<String> sqls() {
			return dialect.getDropTableSqls(tableName);
        }
	}
	
	protected static class GenericCreateColumn extends GenericDbCommand implements CreateColumn {
	
		protected final DbSchemaObjectName tableName;
		protected final DbColumn 		   column;
		
		public GenericCreateColumn(GenericDb db,DbSchemaObjectName tableName,DbColumn column) {
	        super(db);
	        
	        Args.notNull(tableName,"table name");
	        Args.notNull(column,"column");
	        
	        this.tableName = tableName;
	        this.column    = column;
	        
	        /*
	        this.table = db.getMetadata().tryGetTable(tableName);
	        Assert.isTrue(this.table != null,"the given table '" + tableName.toString() + "' not exists");
	        Assert.isTrue(this.table.findColumn(column.getName()) == null,"column '" + column.getName() + "' aleady exists in table '" + tableName.toString() + "");
	        this.column = column;
	        */
        }

		@Override
        public List<String> sqls() {
	        return db.getDialect().getCreateColumnSqls(tableName, column);
        }
	}
	
	protected static class GenericDropColumn extends GenericAlterColumnBase implements DropColumn {
		
		public GenericDropColumn(GenericDb db,DbSchemaObjectName tableName,String columnName) {
	        super(db,tableName,columnName);
        }

		@Override
        public List<String> sqls() {
	        return db.getDialect().getDropColumnSqls(tableName, columnName);
        }
	}
	
	protected static class GenericCreatePrimaryKey extends GenericDbCommand implements CreatePrimaryKey {
		
		protected final DbSchemaObjectName tableName;
		protected final DbPrimaryKey pk;
		
		public GenericCreatePrimaryKey(GenericDb db,DbSchemaObjectName tableName,DbPrimaryKey pk) {
	        super(db);
	        
	        Args.notNull(tableName,"table name");
	        Args.notNull(pk,"primary key");
	        
	        this.tableName = tableName;
	        this.pk        = pk;
	        
	        /*
	        this.table = db.getMetadata().tryGetTable(tableName);
	        Assert.isTrue(this.table != null,"the given table '" + tableName.toString() + "' not exists");
	        
	        Assert.isTrue(!table.hasPrimaryKey(),"cannot create primary key : a primary aleady exists in table '" + table.getName() + "'");
	        this.pk = pk;
	        */
        }

		@Override
        public List<String> sqls() {
	        return db.getDialect().getCreatePrimaryKeySqls(tableName, pk);
        }
	}
	
	protected static class GenericDropPrimaryKey extends GenericDbCommand implements DropPrimaryKey {
		
		//protected final DbTable table;
		
		protected final DbSchemaObjectName tableName;
		
		public GenericDropPrimaryKey(GenericDb db,DbSchemaObjectName tableName) {
	        super(db);
	        
	        Args.notNull(tableName,"table name");
	        this.tableName = tableName;
	        
	        /*
	        this.table = db.getMetadata().tryGetTable(tableName);
	        Assert.isTrue(this.table != null,"the given table '" + tableName.toString() + "' not exists");
	        
	        Assert.isTrue(table.hasPrimaryKey(),"cannot drop primary key : no primary key exists in table '" + table.getName() + "'");
	        */
        }
		
		@Override
        public DbSchemaObjectName getTableName() {
	        return tableName;
        }

		@Override
        public List<String> sqls() {
	        return db.getDialect().getDropPrimaryKeySqls(tableName);
        }
	}
	
	protected static class GenericCreateForeignKey extends GenericDbCommand implements CreateForeignKey {
		
		protected final DbSchemaObjectName tableName;
		protected final DbForeignKey fk;
		
		public GenericCreateForeignKey(GenericDb db,DbSchemaObjectName tableName, DbForeignKey fk) {
	        super(db);
	        
	        Args.notNull(tableName,"table name");
	        Args.notNull(fk,"foreign key");
	        
	        this.tableName = tableName;
	        this.fk        = fk;
	        
	        /*
	        this.table = db.getMetadata().tryGetTable(tableName);
	        Assert.isTrue(this.table != null,"the given table '" + tableName.toString() + "' not exists");
	        
	        Assert.isTrue(this.table.findForeignKey(fk.getName()) == null,"foreign key '" + fk.getName() + "' aleady exists in table '" + tableName.toString() + "");
	        this.fk= fk;
	        */
        }

		@Override
        public List<String> sqls() {
	        return db.getDialect().getCreateForeignKeySqls(tableName, fk);
        }
	}
	
	protected static class GenericDropForeignKey extends GenericDbCommand implements DropForeignKey {
		
		protected final DbSchemaObjectName tableName;
		protected final String             fkName;

		
		public GenericDropForeignKey(GenericDb db,DbSchemaObjectName tableName,String fkName) {
	        super(db);
	        
	        Args.notNull(tableName,"table name");
	        Args.notEmpty(fkName,"foreign key name");
	        
	        this.tableName = tableName;
	        this.fkName    = fkName;
	        
	        /*
	        this.table = db.getMetadata().tryGetTable(tableName);
	        Assert.isTrue(this.table != null,"the given table '" + tableName.toString() + "' not exists");

	        this.fk = table.findForeignKey(fkName);
	        Assert.isTrue(this.fk != null,"foreign key '" + fkName + "' not exists in table '" + table.getName() + "'");
	        */
        }
		
		@Override
        public DbSchemaObjectName getTableName() {
	        return tableName;
        }
		
		@Override
        public String getForeignKeyName() {
	        return fkName;
        }

		@Override
        public List<String> sqls() {
	        return db.getDialect().getDropForeignKeySqls(tableName,fkName);
        }
	}

	protected static class GenericCreateIndex extends GenericDbCommand implements CreateIndex {
		
		protected final DbSchemaObjectName tableName;
		protected final DbIndex 		   ix;

		public GenericCreateIndex(GenericDb db,DbSchemaObjectName tableName,DbIndex ix) {
	        super(db);
	        
	        Args.notNull(tableName,"table name");
	        Args.notNull(ix,"index");
	        
	        this.tableName = tableName;
	        this.ix        = ix;
	        
	        /*
	        this.table = db.getMetadata().tryGetTable(tableName);
	        Assert.isTrue(this.table != null,"the given table '" + tableName.toString() + "' not exists");
	        
	        Assert.isTrue(this.table.findIndex(ix.getName()) == null,"foreign key '" + ix.getName() + "' aleady exists in table '" + tableName.toString() + "");
	        this.ix = ix;
	        */
        }

		@Override
        public List<String> sqls() {
	        return db.getDialect().getCreateIndexSqls(tableName, ix);
        }
	}
	
	protected static class GenericDropIndex extends GenericDbCommand implements DropIndex {
		
		protected final DbSchemaObjectName tableName;
		protected final String			   ixName;

		public GenericDropIndex(GenericDb db,DbSchemaObjectName tableName,String ixName) {
	        super(db);
	        
	        Args.notNull(tableName,"table name");
	        Args.notEmpty(ixName,"index name");
	        
	        this.tableName = tableName;
	        this.ixName    = ixName;
	        
	        /*
	        this.table = db.getMetadata().tryGetTable(tableName);
	        Assert.isTrue(this.table != null,"the given table '" + tableName.toString() + "' not exists");

	        this.ix = table.findIndex(ixName);
	        Assert.isTrue(this.ix != null,"index '" + ixName + "' not exists in table '" + table.getName() + "'");
	        */
        }

		@Override
        public DbSchemaObjectName getTableName() {
	        return tableName;
        }

		@Override
        public String getIndexName() {
	        return ixName;
        }

		@Override
        public List<String> sqls() {
	        return db.getDialect().getDropIndexSqls(tableName, ixName);
        }
	}
	
	protected static class GenericCreateSequence extends GenericDbCommand implements CreateSequence {
		
		protected final DbSequence sequence;

		public GenericCreateSequence(GenericDb db,DbSequence sequence) {
	        super(db);
	        
	        Args.notNull(sequence,"sequence");
	        this.sequence = sequence;
	        
	        if(!dialect.supportsSequence()){
	        	throw new IllegalStateException("this db '" + db.getDescription() + "' not supports sequence");
	        }
	        
	        if(db.checkSequenceExists(sequence)){
	        	throw new ObjectExistsException("sequence '" + sequence.getQualifiedName() + "' aleady exists");
	        }
        }

		@Override
        public List<String> sqls() {
			return dialect.getCreateSequenceSqls(sequence);
		}
	}
	
	protected static class GenericDropSequence extends GenericDbCommand implements DropSequence {
		
		protected final DbSchemaObjectName sequenceName;

		public GenericDropSequence(GenericDb db,DbSchemaObjectName sequenceName) {
	        super(db);
	        
	        Args.notNull(sequenceName,"sequence name");
	        
	        this.sequenceName = sequenceName;

	        /*
	        this.sequence = db.getMetadata().tryGetSequence(sequenceName);
	        
	        if(null == sequence){
	        	throw new ObjectNotFoundException("cannot drop sequence '" + sequenceName.getQualifiedName() + "' :  not found");
	        }
	        */
        }
		
		@Override
        public DbSchemaObjectName getSequenceName() {
	        return sequenceName;
        }

		@Override
        public List<String> sqls() {
	        return db.getDialect().getDropSequenceSqls(sequenceName);
        }
	}
	
	protected static class GenericDropSchema extends GenericDbCommand implements DropSchema {
		
		protected final DbSchema schema;

		public GenericDropSchema(GenericDb db,DbSchema schema) {
	        super(db);
	        
	        Args.notNull(schema,"schema object");
	        this.schema = schema;
        }
		
		@Override
        public DbExecution execute(Connection connection) {
			Confirm.checkConfirmed("DropSchema","drop all tables in the schema");
	        return super.execute(connection);
        }

		@Override
        public List<String> sqls() {
	        return dialect.getDropSchemaSqls(schema);
        }
	}
	
	protected static class GenericRenameColumn extends GenericAlterColumnBase implements RenameColumn {
		
		protected String renameTo;
		
		public GenericRenameColumn(GenericDb db,DbSchemaObjectName tableName,String columnName, String renameTo) {
	        super(db,tableName,columnName);
	        /*
	        Assert.isTrue(this.table.findColumn(renameTo) == null,"column '" + column.getName() + "' aleady exists in table '" + tableName.toString() + "");
	        */
	        
	        this.renameTo = renameTo;
	        Assert.isTrue(dialect.supportsRenameColumn(),db.getDescription() + " does not supports column renaming");
        }

		@Override
        public List<String> sqls() {
	        return dialect.getRenameColumnSqls(tableName, columnName, renameTo);
        }
	}
}
