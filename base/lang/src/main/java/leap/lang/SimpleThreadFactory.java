/*
 * Copyright 2019 the original author or authors.
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

import java.util.concurrent.ThreadFactory;

public class SimpleThreadFactory implements ThreadFactory {

    private final String  name;
    private final boolean useIndex;
    private final boolean daemon;

    private volatile int index = 1;

    public SimpleThreadFactory(String name) {
        this(name, false, true);
    }

    public SimpleThreadFactory(String name, boolean useIndex) {
        this(name, useIndex, true);
    }

    public SimpleThreadFactory(String name, boolean useIndex, boolean daemon) {
        this.name = name;
        this.daemon = daemon;
        this.useIndex = useIndex;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);

        if(useIndex) {
            thread.setName(name + "-" + index++);
        }else {
            thread.setName(name);
        }

        thread.setDaemon(daemon);

        return thread;
    }
}