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

import java.io.File;
import java.io.IOException;

import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.web.assets.AssetTask;

public class AssetTaskTest extends AssetTestCase {

	public static final File WEBAPP_DIR;
	public static final File SOURCE_DIR;
	public static final File OUTPUT_DIR;

	static {
		try {
			Resource curr   = Resources.getResource(AssetTaskTest.class, AssetTaskTest.class.getSimpleName() + ".class");
			Resource webapp = Resources.getResource("file:" + Paths.getDirPath(curr.getFile().getAbsolutePath()) + "../../../../src/test/webapp/");

			Resource assets = webapp.createRelative(manager.getConfig().getSourceDirectory());
			Resource pub    = webapp.createRelative(manager.getConfig().getPublicDirectory());
			
			WEBAPP_DIR = webapp.getFile();
			SOURCE_DIR = assets.getFile();
			OUTPUT_DIR = pub.getFile();
        } catch (IOException e) {
        	throw new RuntimeException(e.getMessage(),e);
        }
	}
	
	public static void main(String[] args) throws Exception {
		AssetTask service = manager.createTask(WEBAPP_DIR);
		service.setForceUpdate(true);
		service.start();
	}
	
}
