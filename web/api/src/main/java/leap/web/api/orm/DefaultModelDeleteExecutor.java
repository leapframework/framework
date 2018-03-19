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

import leap.web.api.mvc.params.DeleteOptions;

public class DefaultModelDeleteExecutor extends ModelExecutorBase implements ModelDeleteExecutor {

    protected final ModelDeleteHandler handler;

    public DefaultModelDeleteExecutor(ModelExecutorContext context, ModelDeleteHandler handler) {
        super(context);
        this.handler = handler;
    }

    @Override
    public DeleteOneResult deleteOne(Object id, DeleteOptions options) {
        if(null == options) {
            options = new DeleteOptions();
        }

        if(null != handler) {
            handler.processDeleteOptions(context, id, options);

            DeleteOneResult result = handler.handleDeleteExecution(context, id, options);
            if(null != result) {
                return result;
            }
        }

        DeleteOneResult result;
        if(!options.isCascadeDelete()) {
            result = new DeleteOneResult(dao.delete(em, id) > 0);
        }else{
            result = new DeleteOneResult(dao.cascadeDelete(em, id));
        }

        if(null != handler) {
            DeleteOneResult r = handler.postDeleteRecore(context, id, options, result);
            if (null != r) {
                result = r;
            }
        }

        return result;
    }

}
