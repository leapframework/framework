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
package leap.web.assets;

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.App;
import leap.web.AppListener;

public class AssetListener implements AppListener {
	private static final Log log = LogFactory.get(AssetListener.class);
	
	protected AssetManager manager;
	protected AssetTask    task;
	
	public AssetManager getManager() {
		return manager;
	}

	public void setManager(AssetManager manager) {
		this.manager = manager;
	}

	@Override
    public void onAppInited(App app) throws Throwable {
		this.task = manager.getTask(app.getServletContext());
    }

	@Override
	public void onAppStarting(App app) {
		if(null == task){
			return;
		}
		
		log.info("Start asset task, source directory '{}', output directory '{}'", 
				  manager.getConfig().getSourceDirectory(),
				  manager.getConfig().getPublicDirectory());
		task.start();
	}

	@Override
	public void onAppEnded(App app) {
		task.stop();
		log.debug("Stop asset task");
	}
}