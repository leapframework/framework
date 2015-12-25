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
import leap.orm.mapping.EntityMapping;
import leap.orm.sql.SqlCommand;

public class DefaultDeleteAllCommand extends AbstractEntityDaoCommand implements DeleteAllCommand {
	
	protected final SqlCommand sqlCommand;
	
	public DefaultDeleteAllCommand(Dao dao,EntityMapping em) {
	    super(dao,em);
	    this.sqlCommand = metadata.getSqlCommand(em.getEntityName(), SqlCommand.DELETE_ALL_COMMAND_NAME);
    }

	@Override
	public int execute() {
	    //Confirm.checkConfirmed("deleteAll", "The deleteAll command will clear all the datas in table.");
		return sqlCommand.executeUpdate(this,null);
	}

}
