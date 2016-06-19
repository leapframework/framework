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

import leap.core.aop.interception.MethodInterception;

public class NopAopProvider implements AopProvider {

    public static AopProvider INSTANCE = new NopAopProvider();

    private NopAopProvider() {

    }

    @Override
    public void run(MethodInterception interception) {
        interception.getRunnable().run();
    }

    @Override
    public <T> T runWithResult(MethodInterception interception) {
        return (T)interception.getSupplier().get();
    }
}
