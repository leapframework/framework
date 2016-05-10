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

package tested.transaction;

import leap.core.annotation.Bean;
import leap.core.annotation.Transactional;

@Bean
public class TransactionalBean {

    @Transactional
    public void doSuccess() {
        System.out.println("do success");
    }

    @Transactional
    public int doSuccessWithReturnValue() {
        System.out.println("do success");
        return 0;
    }

    @Transactional
    public void doSuccessTryFinally() {
        try{
            System.out.println("do success");
        }finally{
            System.out.println("do finally");
        }
    }

    @Transactional
    public void doFailure() {
        throw new RuntimeException("failure");
    }

    @Transactional
    public void doFailureTryFinally() {
        try{
            throw new RuntimeException("failure");
        }finally{
            System.out.println("do finally");
        }
    }

    @Transactional
    public void doFailureNested() {
        throwRuntimeException();
    }

    @Transactional
    public void doFailureThrowable() throws Throwable {
        throw new Exception();
    }

    private static void throwRuntimeException() {
        throw new RuntimeException("failure");
    }
}
