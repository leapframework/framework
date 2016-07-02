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

package leap.core.transaction;

import leap.core.AppContext;
import leap.lang.Exceptions;
import leap.lang.asm.Type;
import leap.lang.asm.commons.Method;

/**
 * For instrumentation only.
 */
public abstract class TxInst {

    public static final Type   TYPE = Type.getType(TxInst.class);
    public static final Method MANAGER;

    static {
        try{
            MANAGER = Method.getMethod(TxInst.class.getMethod("manager"));
        }catch(Exception e) {
            throw Exceptions.uncheck(e);
        }
    }

    public static TransactionManager manager() {
        return AppContext.getBean(TransactionManager.class);
    }
}
