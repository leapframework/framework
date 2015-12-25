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

import leap.core.ioc.PostCreateBean;
import leap.lang.Disposable;
import leap.lang.annotation.Internal;
import leap.lang.io.FileChangeMonitor;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

@Internal
public class AppFileMonitor extends FileChangeMonitor implements PostCreateBean,Disposable {
	
	private static final Log log = LogFactory.get(AppFileMonitor.class);

	public static final long DEFAULT_INTERVAL = 2000; //2 seconds
	
	public AppFileMonitor() {
	    super(DEFAULT_INTERVAL);
    }

	public AppFileMonitor(long interval) {
	    super(interval);
    }
	
	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		if(factory.getAppConfig().isReloadEnabled()) {
			log.debug("Start app file monitor at interval : " + interval);
			start();
		}
    }

	@Override
    public void dispose() throws Throwable {
		if(running){
			log.debug("Stop app file monitor");
			stop();	
		}
    }
}