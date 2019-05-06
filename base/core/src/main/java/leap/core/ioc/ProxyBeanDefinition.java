/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.core.ioc;

public class ProxyBeanDefinition extends BeanDefinitionBase {

    protected final TargetBeanDefinition target;

    public ProxyBeanDefinition(Object source) {
        super(source);
        this.target = new TargetBeanDefinition(source, this);
        this.target.setOverride(true);
    }

    public void setTargetType(Class<?> targetType) {
        target.setType(targetType);
    }

    public void setTargetNamespace(String targetNamespace) {
        target.setNamespace(targetNamespace);
    }

    public void setTargetId(String targetId) {
        target.setId(targetId);
    }

    public void setTargetName(String targetName) {
        target.setName(targetName);
    }

    public void setTargetPrimary(boolean targetPrimary) {
        target.setPrimary(targetPrimary);
    }

    public TargetBeanDefinition getTarget() {
        if(null == target.getBeanClass()) {
            target.setBeanClass(beanClass);
        }
        return target;
    }

    static class TargetBeanDefinition extends BeanDefinitionBase {
        protected final ProxyBeanDefinition proxy;

        public TargetBeanDefinition(Object source, ProxyBeanDefinition proxy) {
            super(source);
            this.proxy = proxy;
        }

        public ProxyBeanDefinition getProxy() {
            return proxy;
        }
    }
}
