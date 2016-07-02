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

package leap.core.aop.config;

import leap.core.aop.matcher.MethodInfo;
import leap.core.aop.matcher.MethodMatcher;

import java.util.ArrayList;
import java.util.List;

public class DefaultMethodInterceptionConfig implements MethodInterceptionConfig {

    protected MethodInterceptorConfig interceptor;
    protected List<MethodMatcher>     matchers = new ArrayList<>();

    public MethodInterceptorConfig getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(MethodInterceptorConfig interceptor) {
        this.interceptor = interceptor;
    }

    public void addMatcher(MethodMatcher matcher) {
        matchers.add(matcher);
    }

    @Override
    public boolean matches(MethodInfo method) {
        for(MethodMatcher matcher : matchers) {
            if(matcher.matches(method)) {
                return true;
            }
        }
        return false;
    }
}