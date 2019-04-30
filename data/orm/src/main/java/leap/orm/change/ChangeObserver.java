/*
 * Copyright 2017 the original author or authors.
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
package leap.orm.change;

import java.util.concurrent.TimeUnit;

public interface ChangeObserver {

    /**
     * Adds a listener
     *
     * @throws IllegalStateException if the observer has been started.
     */
    ChangeObserver setListener(ChangeListener listener);

    /**
     * The default period is one second.
     */
    ChangeObserver setPeriod(TimeUnit timeUnit, int period);

    /**
     * Limits the max changes loaded by observer.  The default is 100.
     */
    ChangeObserver limit(int maxChanges);

    /**
     * Start watching.
     *
     * @throws IllegalStateException if already started or no listener(s).
     */
    void start();

    /**
     * Stop watching.
     */
    void stop();

}
