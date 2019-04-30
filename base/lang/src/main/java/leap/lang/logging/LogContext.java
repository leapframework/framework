/*
 * Copyright 2013 the original author or authors.
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
package leap.lang.logging;

import java.util.function.Supplier;

public class LogContext {

    private static final ThreadLocal<LogLevel> levelContext = new ThreadLocal<>();

    public static LogLevel level() {
        LogLevel level = levelContext.get();
        return null == level ? LogLevel.ALL : level;
    }

    public static void exec(LogLevel level, Runnable func) {
        levelContext.set(level);
        try{
            func.run();
        }finally{
            levelContext.remove();
        }
    }

    public static <T> T execWithResult(LogLevel level, Supplier<T> func) {
        levelContext.set(level);
        try{
            return func.get();
        }finally{
            levelContext.remove();
        }
    }

    protected LogContext() {

    }
}