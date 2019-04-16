/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.web.api.orm;

import leap.orm.event.EntityListeners;
import leap.web.api.mvc.params.DeleteOptions;
import leap.web.api.remote.RestResource;

public class DefaultModelDeleteExecutor extends ModelExecutorBase implements ModelDeleteExecutor {

    protected final ModelDeleteExtension ex;

    private DeleteHandler   handler;
    private EntityListeners listeners;

    public DefaultModelDeleteExecutor(ModelExecutorContext context, ModelDeleteExtension ex) {
        super(context);
        this.ex = null == ex ? ModelDeleteExtension.EMPTY : ex;
    }

    @Override
    public ModelDeleteExecutor withHandler(DeleteHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public ModelDeleteExecutor withListeners(EntityListeners listeners) {
        this.listeners = listeners;
        return this;
    }

    @Override
    public DeleteOneResult deleteOne(Object id, DeleteOptions options) {
        ModelExecutionContext context = new DefaultModelExecutionContext(this.context);

        if (null == options) {
            options = new DeleteOptions();
        }

        ex.processDeleteOneOptions(context, id, options);
        ex.processDeleteOneOptions((ModelExecutorContext) context, id, options);

        DeleteOneResult result;
        if (null != ex.handler) {
            ex.handler.processDeleteOptions(context, id, options);
            result = ex.handler.handleDeleteExecution(context, id, options);
        } else {
            result = ex.handleDeleteOne(context, id, options);
        }

        if (null == result) {
            if (!em.isRemoteRest()) {
                final DeleteOptions finalOptions = options;

                result = em.withContextListeners(listeners, () -> {
                    if(null != handler) {
                        return new DeleteOneResult(handler.deleteOne(context, id, finalOptions) > 0);
                    }else {
                        if (!finalOptions.isCascadeDelete()) {
                            return dao.withEvents(() -> new DeleteOneResult(dao.delete(em, id) > 0));
                        } else {
                            return dao.withEvents(() -> new DeleteOneResult(dao.cascadeDelete(em, id)));
                        }
                    }
                });
            } else {
                RestResource restResource = restResourceFactory.createResource(dao.getOrmContext(), em);
                if (restResource.delete(id, options)) {
                    result = new DeleteOneResult(true);
                } else {
                    result = new DeleteOneResult(false);
                }
            }
        }

        if (null != ex.handler) {
            DeleteOneResult r = ex.handler.postDeleteRecord(context, id, options, result);
            if (null != r) {
                result = r;
            }
        }

        Object entity = ex.processDeleteOneResult(context, id, result.success);
        if (null != entity) {
            result = new DeleteOneResult(result.success, entity);
        }

        return result;
    }

}
