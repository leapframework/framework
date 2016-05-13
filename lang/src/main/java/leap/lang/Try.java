/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.lang;

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.util.function.Consumer;

public class Try {
    
    private static final Log log = LogFactory.get(Try.class);

    public static void catchAll(CatchRunnable runnable, Consumer<Throwable> errorHandler) {
        try{
            runnable.run();
        }catch(Throwable e) {
            errorHandler.accept(e);
        }
    }

    public static void catchAll(CatchRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            log.warn(e);
        }
    }

    public static void throwUnchecked(CatchRunnable runnable) {
        try{
            runnable.run();
        }catch (Throwable e) {
            throw Exceptions.uncheck(e);
        }
    }

    public static <T> T throwUncheckedWithResult(CatchSupplier<T> supplier) {
        try{
            return supplier.run();
        }catch (Throwable e) {
            throw Exceptions.uncheck(e);
        }
    }

    public interface CatchRunnable {
        void run() throws Throwable;
    }

    public interface CatchSupplier<T> {
        T run() throws Throwable;
    }
    
    protected Try() {
        
    }
}