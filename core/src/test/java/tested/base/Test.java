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

package tested.base;

import leap.core.annotation.Inject;
import leap.core.aop.AopProvider;
import leap.core.aop.interception.MethodInterception;
import leap.core.aop.interception.MethodInterceptor;
import leap.core.aop.interception.SimpleMethodInterception;
import tested.aop.TAopInterceptor;

public class Test {

    private @Inject                AopProvider       aopProvider;
    private @Inject                TAopInterceptor   interceptor1;
    private @Inject(name = "test") MethodInterceptor interceptor2;

    public String m3() {
        MethodInterception interception =
                new SimpleMethodInterception("cls","name","desc",
                        this,
                        new MethodInterceptor[]{interceptor1},
                        () -> m3$aop());

        return aopProvider.runWithResult(interception);
    }

    private String m3$aop() {
        return "hello";
    }

}
