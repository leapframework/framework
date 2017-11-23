/*
 * Copyright 2015 the original author or authors.
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
package leap.orm.command;

import leap.db.DbExecution;
import leap.db.model.DbTable;
import leap.db.model.DbTableBuilder;
import leap.lang.Args;
import leap.lang.Error;
import leap.lang.Strings;
import leap.orm.dmo.Dmo;
import leap.orm.mapping.EntityMapping;

import java.util.List;

public class DefaultCreateTableCommand extends AbstractDmoCommand implements CreateTableCommand {

    protected final EntityMapping em;

    protected DbExecution execution;
    protected String      tableName;

    public DefaultCreateTableCommand(Dmo dmo, EntityMapping em) {
        super(dmo);
        this.em = em;
        this.tableName = em.getTableName();
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public CreateTableCommand changeTableName(String name) {
        Args.notEmpty(name, "table name");
        this.tableName = name;
        return this;
    }

    @Override
    public List<? extends Error> errors() {
        return null == execution ? null : execution.errors();
    }

    @Override
    protected boolean doExecute() {
        DbTable table;

        if(em.isRemote()){
        	return false;
        }

        if(Strings.equalsIgnoreCase(tableName, em.getTableName())) {
            table = em.getTable();
        }else{
            table = new DbTableBuilder(em.getTable()).updateTableName(tableName).build();
        }

        if(db.checkTableExists(table)) {
            return false;
        }

        execution = db.cmdCreateTable(table).execute();
        return true;
    }

}