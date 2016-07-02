/**
 * Copyright (C) 2010 Mycila <mathieu.carbou@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.junit.concurrent;

import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class ConcurrentRunner extends BlockJUnit4ClassRunner {
    public ConcurrentRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
        
        int threads = 0;
        
        if (clazz.isAnnotationPresent(Concurrent.class)){
        	threads = Math.max(0, clazz.getAnnotation(Concurrent.class).value());
        }
            
        if (threads == 0){
        	threads = new TestClass(clazz).getAnnotatedMethods(Test.class).size();
        }
            
        if (threads == 0){
        	threads = Runtime.getRuntime().availableProcessors();
        }
            
        setScheduler(new ConcurrentRunnerScheduler(clazz.getSimpleName(), threads));
    }
}