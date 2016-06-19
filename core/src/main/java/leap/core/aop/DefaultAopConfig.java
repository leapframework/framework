/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.core.aop;

import leap.core.aop.config.MethodInterceptionConfig;
import leap.core.aop.config.MethodInterceptorConfig;
import leap.core.aop.matcher.MethodInfo;

import java.util.ArrayList;
import java.util.List;

public class DefaultAopConfig implements AopConfig {

    protected boolean enabled = true;

    protected List<MethodInterceptionConfig> methodInterceptions = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addMethodInterception(MethodInterceptionConfig interception) {
        methodInterceptions.add(interception);
    }

    @Override
    public List<MethodInterceptorConfig> getMethodInterceptors(MethodInfo method) {
        List<MethodInterceptorConfig> list = null;

        for(MethodInterceptionConfig interception : methodInterceptions) {

            if(interception.matches(method)) {
                if(null == list) {
                    list = new ArrayList<>(1);
                }
                list.add(interception.getInterceptor());
            }

        }

        return list;
    }
}
