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
package leap.orm.command;

import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;
import leap.orm.mapping.EntityMapping;

public class DefaultCommandFactory implements CommandFactory {
	
	@Override
    public InsertCommand newInsertCommand(Dao dao, EntityMapping em) {
	    return new DefaultInsertCommand(dao, em);
    }
	
	@Override
    public UpdateCommand newUpdateCommand(Dao dao, EntityMapping em) {
	    return new DefaultUpdateCommand(dao, em);
    }

	@Override
    public DeleteCommand newDeleteCommand(Dao dao, EntityMapping em, Object id) {
	    return new DefaultDeleteCommand(dao, em, id);
    }

	@Override
    public DeleteAllCommand newDeleteAllCommand(Dao dao, EntityMapping em) {
	    return new DefaultDeleteAllCommand(dao, em);
    }

	@Override
    public <T> FindCommand<T> newFindCommand(Dao dao, EntityMapping em, Object id, Class<T> resultClass, boolean checkNotFound) {
	    return new DefaultFindCommand<T>(dao, em, id, resultClass,checkNotFound);
    }
	
	@Override
    public <T> FindListCommand<T> newFindListCommand(Dao dao, EntityMapping em, 
                                                     Object[] ids, Class<T> elementType,Class<? extends T> resultClass, boolean checkNotFound) {
	    return new DefaultFindListCommand<T>(dao, em, ids, elementType, resultClass, checkNotFound);
    }

	@Override
    public <T> FindAllCommand<T> newFindAllCommand(Dao dao, EntityMapping em, Class<T> elementType, Class<? extends T> resultClass) {
	    return new DefaultFindAllCommand<T>(dao, em, elementType, resultClass);
    }

	@Override
    public ExistsCommand newCheckEntityExistsCommand(Dao dao, EntityMapping em, Object id) {
	    return new DefaultExistsCommand(dao, em, id);
    }

	@Override
    public CountCommand newCountEntityCommand(Dao dao, EntityMapping em) {
	    return new DefaultCountEntityCommand(dao, em);
    }
	
	@Override
    public CreateTableCommand newCreateTableCommand(Dmo dmo, EntityMapping em) {
        return new DefaultCreateTableCommand(dmo, em);
    }

    @Override
    public DropTableCommand newDropTableCommand(Dmo dmo, EntityMapping em) {
        return new DefaultDropTableCommand(dmo, em);
    }

    @Override
    public CreateEntityCommand newCreateEntityCommand(Dmo dmo, Class<?> entityClass) {
	    return new DefaultCreateEntityCommand(dmo, entityClass);
    }

	@Override
    public UpgradeSchemaCommand newUpgradeSchemaCommand(Dmo dmo) {
	    return new DefaultUpgradeSchemaCommand(dmo);
    }
	
	@Override
    public UpgradeSchemaCommand newUpgradeSchemaCommand(Dmo dmo, EntityMapping... ems) {
	    return new DefaultUpgradeSchemaCommand(dmo, ems);
    }

    @Override
    public TruncateCommand newTruncateEntityCommand(Dmo dmo, EntityMapping em) {
	    return new DefaultTruncateCommand(dmo, em);
    }

	@Override
    public BatchInsertCommand newBatchInsertCommand(Dao dao, EntityMapping em, Object[] records) {
	    return new DefaultBatchInsertCommand(dao, em, records);
    }

    @Override
    public BatchUpdateCommand newBatchUpdateCommand(Dao dao, EntityMapping em, Object[] records) {
        return new DefaultBatchUpdateCommand(dao, em , records);
    }

    @Override
    public BatchDeleteCommand newBatchDeleteCommand(Dao dao, EntityMapping em, Object[] ids) {
	    return new DefaultBatchDeleteCommand(dao, em, ids);
    }
}
