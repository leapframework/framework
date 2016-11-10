/*
 *
 *  * Copyright 2013 the original author or authors.
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

package leap.web.api.meta.desc;

import leap.web.action.Action;
import leap.web.api.config.ApiConfigException;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by kael on 2016/11/8.
 */
public class DefaultOperationDescSet implements OperationDescSet {

    private List<OperationDesc> descs = new LinkedList<>();

    @Override
    public OperationDesc getOperationDesc(Action action) {
        if(action == null){
            return null;
        }
        for(OperationDesc desc : descs){
            if(desc.getAction() == action){
                return desc;
            }
        }
        return null;
    }

    public void addOperationDesc(Action action, OperationDesc desc){
        if(null != getOperationDesc(action)){
            throw new ApiConfigException("duplicate description for operation :"+action.getName());
        }
        descs.add(desc);
    }

}
