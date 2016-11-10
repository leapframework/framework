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

import leap.core.annotation.Inject;
import leap.web.api.config.ApiConfigException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kael on 2016/11/8.
 */
public class DefaultApiDescContainer implements ApiDescContainer {
    @Inject
    private DescriptionLoader loader;
    private final static Map<String, OperationDescSet> container = new ConcurrentHashMap<>();

    @Override
    public OperationDescSet getAllOperationDescSet(Object controller) {
        if(container.containsKey(getKey(controller))){
            return container.get(getKey(controller));
        }
        OperationDescSet set = loader.load(this,controller);
        if(set == null){
            return null;
        }
        container.put(getKey(controller),set);
        return set;
    }

    @Override
    public void addOperationDescSet(Object controller,OperationDescSet set) {
        if(container.containsKey(getKey(controller))){
            throw new ApiConfigException("duplicate OperationDescSet for controller:"+controller.getClass().getName());
        }
        container.put(getKey(controller),set);
    }

    protected String getKey(Object controller){
        return controller.getClass().getName();
    }
}
