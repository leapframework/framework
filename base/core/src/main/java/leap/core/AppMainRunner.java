/*
 * Copyright 2014 the original author or authors.
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
package leap.core;

import leap.lang.Try;

/**
 * Like {@link AppMainBase}, but not init the app context in static init method.
 */
public abstract class AppMainRunner {
	
	protected static AppContext  context;
    protected static AppConfig   config;
    protected static BeanFactory factory;
	
    /**
     * Executes the main.
     */
    protected static void run(Class<? extends AppMainRunner> mainClass, Object[] args) {
        AppContext.initStandalone();
        context = AppContext.current();
        config  = context.getConfig();
        factory = context.getBeanFactory();

        Try.throwUnchecked(() -> factory.createBean(mainClass).run(args));
    }

    /**
     * Called by the {@link #run(Class, Object[])}.
     */
    protected void run(Object[] args) throws Throwable {

    }

}