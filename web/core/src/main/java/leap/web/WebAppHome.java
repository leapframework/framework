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
package leap.web;

import leap.core.DefaultAppHome;
import leap.lang.resource.FileResource;

public class WebAppHome extends DefaultAppHome implements AppAware {

	protected App app;
	
	@Override
    public void setApp(App app) {
		this.app = app;
    }

	@Override
    protected void initHomeDirectoryByDefault(FileResource userDir) throws Throwable {
		//Auto detect development environment (maven)
		if(userDir.createRelative("./target/classes").exists()){
			dir = userDir.createRelative("./target");
		}else{
			FileResource baseDir = app.getBaseDir();
			if(null != baseDir){
				dir = baseDir.createRelative("./WEB-INF");
			}else{
				dir = userDir;
			}
		}
    }

}