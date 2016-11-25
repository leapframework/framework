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

package leap.web.api.meta;

import leap.lang.Strings;
import leap.web.api.meta.model.MApiOperationBuilder;
import leap.web.api.meta.model.MApiPathBuilder;

public class DefaultApiMetadataStrategy implements ApiMetadataStrategy {

    @Override
    public boolean tryCreateOperationId(ApiMetadataBuilder m, MApiPathBuilder p, MApiOperationBuilder op) {
        String name   = op.getName();
        String method = op.getMethod().name().toLowerCase();

        if(!name.equalsIgnoreCase(method)) {
            //id = name
            if (tryCreateOperationId(m, op, name)) {
                return true;
            }

            //id = name + "with" + method
            String id = name + "With" + Strings.upperFirst(method);
            if (tryCreateOperationId(m, op, id)) {
                return true;
            }
        }

        for(String tag : op.getTags()) {
            if(!name.toLowerCase().contains(tag.toLowerCase())) {
                //id = name + tag
                String id = name + Strings.upperFirst(tag);

                if(tryCreateOperationId(m, op, id)) {
                    return true;
                }

                //id = id + "With" + method
                id = id + "With" + Strings.upperFirst(method);
                if(tryCreateOperationId(m, op, id)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean tryCreateOperationId(ApiMetadataBuilder m, MApiOperationBuilder op, String id) {
        if(m.getOperationIds().contains(id)) {
            return false;
        }

        op.setId(id);
        m.getOperationIds().add(id);
        return true;
    }
}
