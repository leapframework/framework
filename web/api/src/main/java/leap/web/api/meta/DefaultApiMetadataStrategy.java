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
import leap.web.api.config.ApiConfig;
import leap.web.api.meta.model.MApiOperationBuilder;
import leap.web.api.meta.model.MApiPathBuilder;

public class DefaultApiMetadataStrategy implements ApiMetadataStrategy {

    private static final String SIMPLE_NAME_PATTERN = "[^0-9a-zA-Z_]+";

    @Override
    public boolean tryCreateOperationId(ApiConfig c, ApiMetadataBuilder m, MApiPathBuilder p, MApiOperationBuilder op) {
        if(!c.isUniqueOperationId()) {
            op.setId(op.getName());
            return true;
        }

        String name   = op.getName();
        String method = op.getMethod().name().toLowerCase();

        if(Strings.contains(name,"$Lambda$")){
            // Lambda expression do not create operation id
            return false;
        }

        if(!name.equalsIgnoreCase(method)) {
            //id = name
            if (trySetUniqueOperationId(m, op, name)) {
                return true;
            }

            //id = name + "with" + method
            if (!Strings.startsWithIgnoreCase(name, method)) {
                String id = name + "With" + Strings.upperFirst(method);
                if (trySetUniqueOperationId(m, op, id)) {
                    return true;
                }
            }
        }

        for(String tag : op.getTags()) {
            String tn = Strings.upperCamel(tag.replaceAll(SIMPLE_NAME_PATTERN, ""), '_');
            if(!Strings.isEmpty(tn) && !name.toLowerCase().contains(tn.toLowerCase())) {
                //id = name + "In" + tag
                String id = name + "In" + Strings.upperFirst(tn);
                if(trySetUniqueOperationId(m, op, id)) {
                    return true;
                }

                //id = id + "With" + method
                if (!Strings.startsWithIgnoreCase(id, method)) {
                    id = id + "With" + Strings.upperFirst(method);
                    if(trySetUniqueOperationId(m, op, id)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    protected boolean trySetUniqueOperationId(ApiMetadataBuilder m, MApiOperationBuilder op, String id) {
        String lowerId = Strings.lowerCase(id);
        if(m.getOperationIds().contains(lowerId)) {
            return false;
        }
        op.setId(id);
        m.getOperationIds().add(lowerId);
        return true;
    }

}
