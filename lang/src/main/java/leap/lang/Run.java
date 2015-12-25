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

public class Run {
    
    private static final Log log = LogFactory.get(Run.class);

    public static void catchThrowable(CatchThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            log.warn("Error running, " + e.getMessage(), e);
        }
    }
    
    public static interface CatchThrowableRunnable {
        void run() throws Throwable;
    }
    
    protected Run() {
        
    }
}
