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

import leap.db.DbExecution;
import leap.db.command.DropTable;
import leap.lang.Confirm;
import leap.lang.Error;
import leap.orm.dmo.Dmo;
import leap.orm.mapping.EntityMapping;

import java.util.List;

public class DefaultDropTableCommand extends AbstractDmoCommand implements DropTableCommand {

    protected final EntityMapping em;

    protected DbExecution execution;

    public DefaultDropTableCommand(Dmo dmo, EntityMapping em) {
        super(dmo);
        this.em = em;
    }

    @Override
    public List<? extends Error> errors() {
        return null == execution ? null : execution.errors();
    }

    @Override
    protected boolean doExecute() {
        Confirm.checkConfirmed("dmo.cmdDropTable", "Will drop the table in db and lost all the data in table");

        if(!db.checkTableExists(em.getTable())) {
            return false;
        }
        execution = db.cmdDropTable(em.getTable()).execute();
        return true;
    }
}
