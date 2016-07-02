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

import leap.core.annotation.Inject;
import leap.lang.Disposable;
import leap.lang.Try;
import leap.lang.annotation.Internal;
import leap.lang.io.FileChangeMonitor;
import leap.lang.io.FileChangeObserver;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

@Internal
public class AppFileMonitor extends FileChangeMonitor implements Disposable {
	
	private static final Log log = LogFactory.get(AppFileMonitor.class);

	public static final long DEFAULT_INTERVAL = 2000; //2 seconds

    private @Inject AppConfig config;
	
	public AppFileMonitor() {
	    super(DEFAULT_INTERVAL);
    }

	public AppFileMonitor(long interval) {
	    super(interval);
    }

    @Override
    public void addObserver(FileChangeObserver observer) {
        super.addObserver(observer);
        if(!running) {
            if(config.isReloadEnabled()) {
                log.trace("Start app file monitor at interval : " + interval);
                Try.throwUnchecked(this::start);
            }
        }
    }

	@Override
    public void dispose() throws Throwable {
		if(running){
			log.trace("Stop app file monitor");
			stop();	
		}
    }
}