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

import java.util.ArrayList;
import java.util.List;

import leap.db.DbExecution;
import leap.lang.Error;
import leap.orm.dmo.Dmo;
import leap.orm.mapping.EntityMapping;

public class DefaultTruncateCommand extends AbstractDmoCommand implements TruncateCommand{
	
	protected final EntityMapping em;
	
	protected DbExecution execution;

	public DefaultTruncateCommand(Dmo dmo,EntityMapping em) {
		super(dmo);
		
		this.em = em;
	}

	@Override
	public List<? extends Error> errors() {
		return null == execution ? new ArrayList<Error>() : execution.errors();
	}

	@Override
	protected boolean doExecute() {
		execution = db.createExecution();
		
		execution.addAll(db.getDialect().getTruncateTableSqls(em.getTable()));
		
		return execution.setRefreshSchema(false).execute();
	}

}