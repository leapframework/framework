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
package leap.core.schedule;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import leap.lang.Args;
import leap.lang.Disposable;
import leap.lang.Try;

public class DefaultSchedulerManager implements SchedulerManager, Disposable {

    protected final Set<FixedThreadPoolScheduler> fixedThreadPoolSchedulers = new CopyOnWriteArraySet<>();
    
    @Override
    public Scheduler newFixedThreadPoolScheduler(String name, int corePoolSize) {
        Args.notEmpty(name,"name");
        FixedThreadPoolScheduler scheduler = new FixedThreadPoolScheduler(name , corePoolSize);
        
        fixedThreadPoolSchedulers.add(scheduler);
        
        return scheduler;
    }

    @Override
    public void dispose() throws Throwable {
        if(!fixedThreadPoolSchedulers.isEmpty()) {
            for(FixedThreadPoolScheduler scheduler : fixedThreadPoolSchedulers) {
                Try.catchAll(() -> scheduler.dispose());
            }
        }
    }

}
