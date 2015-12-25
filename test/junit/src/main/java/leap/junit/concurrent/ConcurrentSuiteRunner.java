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

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class ConcurrentSuiteRunner extends Suite {
    public ConcurrentSuiteRunner(Class<?> klass) throws InitializationError {
        super(klass, new AllDefaultPossibilitiesBuilder(true) {
            @Override
            public Runner runnerForClass(Class<?> testClass) throws Throwable {
                List<RunnerBuilder> builders = Arrays.asList(
                    new RunnerBuilder() {
                        @Override
                        public Runner runnerForClass(Class<?> testClass) throws Throwable {
                        	Concurrent annotation = testClass.getAnnotation(Concurrent.class);
                            return annotation != null ? new ConcurrentRunner(testClass) : null;
                        }
                    },
                    ignoredBuilder(),
                    annotatedBuilder(),
                    suiteMethodBuilder(),
                    junit3Builder(),
                    junit4Builder());
                for (RunnerBuilder each : builders) {
                    Runner runner = each.safeRunnerForClass(testClass);
                    if (runner != null)
                        return runner;
                }
                return null;
            }
        });
        int nThreads = 0;
        if (klass.isAnnotationPresent(Concurrent.class))
            nThreads = Math.max(0, klass.getAnnotation(Concurrent.class).value());
        if (nThreads == 0) {
            SuiteClasses suiteClasses = klass.getAnnotation(SuiteClasses.class);
            nThreads = suiteClasses != null && suiteClasses.value().length > 0 ? suiteClasses.value().length : Runtime.getRuntime().availableProcessors();
        }
        setScheduler(new ConcurrentRunnerScheduler(klass.getSimpleName(), nThreads));
    }
}
