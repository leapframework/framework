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

public interface CommandFactory {
	
	InsertCommand newInsertCommand(Dao dao,EntityMapping em);
	
	UpdateCommand newUpdateCommand(Dao dao,EntityMapping em);

	DeleteCommand newDeleteCommmand(Dao dao, EntityMapping em, Object id);

	DeleteAllCommand newDeleteAllCommand(Dao dao,EntityMapping em);
	
	<T> FindCommand<T> newFindCommand(Dao dao,EntityMapping em, Object id, Class<T> resultClass, boolean checkNotFound);
	
	<T> FindListCommand<T> newFindListCommand(Dao dao,EntityMapping em, Object ids[], Class<T> elementType,Class<? extends T> resultClass, boolean checkNotFound);
	
	<T> FindAllCommand<T> newFindAllCommand(Dao dao,EntityMapping em,Class<T> elementType,Class<? extends T> resultClass);
	
	ExistsCommand newCheckEntityExistsCommand(Dao dao,EntityMapping em,Object id);
	
	CountCommand newCountEntityCommand(Dao dao,EntityMapping em);
	
	CreateTableCommand newCreateTableCommand(Dmo dmo, EntityMapping em);
	
	CreateEntityCommand newCreateEntityCommand(Dmo dmo,Class<?> entityClass);

	DropTableCommand newDropTableCommand(Dmo dmo, EntityMapping em);
	
	UpgradeSchemaCommand newUpgradeSchemaCommand(Dmo dmo);
	
	UpgradeSchemaCommand newUpgradeSchemaCommand(Dmo dmo, EntityMapping... ems);
	
	TruncateCommand newTruncateEntityCommand(Dmo dmo,EntityMapping em);
	
	BatchInsertCommand newBatchInsertCommand(Dao dao,EntityMapping em, Object[] records);

	BatchUpdateCommand newBatchUpdateCommand(Dao dao, EntityMapping em, Object[] records);

	BatchDeleteCommand newBatchDeleteCommand(Dao dao,EntityMapping em, Object[] ids);

}
