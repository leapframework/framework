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

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.web.api.config.ApiConfigException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kael on 2016/11/8.
 */
public class DefaultApiDescContainer implements ApiDescContainer,PostCreateBean {
    @Inject(name="controller")
    private DescriptionLoader<Object,OperationDescSet> controllerLoader;
    @Inject(name="model")
    private DescriptionLoader<Class<?>,ModelDesc>      modelLoader;

    private CommonDescContainer commonDescContainer = null;

    private final static Map<String, OperationDescSet> controllers = new ConcurrentHashMap<>();
    private final static Map<String, ModelDesc>     models      = new ConcurrentHashMap<>();

    @Override
    public OperationDescSet getOperationDescSet(Object controller) {
        if(controllers.containsKey(getKey(controller))){
            return controllers.get(getKey(controller));
        }
        OperationDescSet set = controllerLoader.load(this,controller);
        if(set == null){
            return null;
        }
        return set;
    }

    @Override
    public void addOperationDescSet(Object controller,OperationDescSet set) {
        if(controllers.containsKey(getKey(controller))){
            throw new ApiConfigException("duplicate OperationDescSet for controller:"+controller.getClass().getName());
        }
        controllers.put(getKey(controller),set);
    }

    @Override
    public ModelDesc getModelDesc(Class<?> modelType) {
        if(models.containsKey(modelType.getName())){
            return models.get(modelType.getName());
        }
        ModelDesc set = modelLoader.load(this,modelType);
        if(set != null){
            return set;
        }
        return null;
    }

    @Override
    public CommonDescContainer.Parameter getCommonParameter(Class<?> type) {
        if(commonDescContainer == null){
            return null;
        }
        return commonDescContainer.getCommonParam(type);
    }

    @Override
    public void addModelDesc(Class<?> modelType, ModelDesc desc) {
        if(models.containsKey(modelType.getName())){
            throw new ApiConfigException("duplicate ModelDesc for model:"+modelType.getName());
        }
        models.put(modelType.getName(),desc);
    }

    protected String getKey(Object controller){
        return controller.getClass().getName();
    }

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        commonDescContainer = factory.getAppConfig().getExtension(CommonDescContainer.class);
    }

}
