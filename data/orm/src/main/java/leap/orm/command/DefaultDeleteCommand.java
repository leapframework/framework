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

import leap.lang.params.Params;
import leap.orm.dao.Dao;
import leap.orm.event.DeleteEntityEventImpl;
import leap.orm.event.EntityEventHandler;
import leap.orm.mapping.EntityMapping;
import leap.orm.sql.SqlCommand;

public class DefaultDeleteCommand extends AbstractEntityDaoCommand implements DeleteCommand {

    protected final EntityEventHandler eventHandler;
    protected final Object             id;
    protected final Params             idParameter;
    protected final SqlCommand         primaryCommand;
    protected final DeleteHandler      handler;

    public DefaultDeleteCommand(Dao dao, EntityMapping em, Object id) {
        super(dao, em);

        this.eventHandler = context.getEntityEventHandler();
        this.primaryCommand = metadata.getSqlCommand(em.getEntityName(), SqlCommand.DELETE_COMMAND_NAME);
        this.id = id;
        this.idParameter = context.getParameterStrategy().createIdParameters(context, em, id);
        this.handler = null == em.getDeleteHandler() ? DeleteHandler.NOP : em.getDeleteHandler();
    }

    @Override
    public int execute() {
        if (eventHandler.isHandleDeleteEvent(context, em)) {
            int result;

            DeleteEntityEventImpl e = new DeleteEntityEventImpl(context, em, id);

            //pre without transaction.
            eventHandler.preDeleteEntityNoTrans(context, em, e);

            if (em.hasSecondaryTable() || eventHandler.isDeleteEventTransactional(context, em)) {
                result = dao.doTransaction((status) -> {
                    e.setTransactionStatus(status);

                    //pre with transaction.
                    eventHandler.preDeleteEntityInTrans(context, em, e);

                    int affected = doExecuteDelete();

                    //post with transaction.
                    e.setAffected(affected);
                    eventHandler.postDeleteEntityInTrans(context, em, e);

                    e.setTransactionStatus(null);

                    return affected;
                });

            } else {
                result = doExecuteDelete();
                e.setAffected(result);
            }

            //post without transaction.
            eventHandler.postDeleteEntityNoTrans(context, em, e);

            return result;
        } else {
            return doExecuteDelete();
        }
    }

    protected int doExecuteDelete() {
        if (em.hasSecondaryTable()) {
            context.getSqlFactory().createDeleteCommand(context, em, true).executeUpdate(this, idParameter);
        }

        return handler.handleDelete(this, idParameter, () -> primaryCommand.executeUpdate(this, idParameter));
    }

}
