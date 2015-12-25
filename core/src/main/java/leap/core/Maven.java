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

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.FileResource;
import leap.lang.resource.Resources;
import leap.lang.servlet.ServletResource;
import leap.lang.servlet.Servlets;

class Maven {
	private static final Log log = LogFactory.get(Maven.class);
	
	static boolean isMavenProject(Object externalContext) {
		return getMavenProjectHome(externalContext) != null;
	}

	static FileResource getMavenProjectHome(Object externalContext) {
		if(isServletContext(externalContext)){
			return getWebAppMavenProjectHome(externalContext);
		}else{
			return getStandaloneMavenProjectHome();
		}
	}
	
	private static boolean isServletContext(Object externalContext) {
		return externalContext instanceof javax.servlet.ServletContext;
	}
	
	private static FileResource getWebAppMavenProjectHome(Object externalContext) {
		javax.servlet.ServletContext sc = (javax.servlet.ServletContext)externalContext;
		
		try {
	        ServletResource r = Servlets.getResource(sc, "/WEB-INF");
	        if(r.exists() && r.isFile()){
	        	FileResource dir = Resources.createFileResource(r.getFile());
	        	
	        	//project
	        	//  /src/main/webapp/WEB-INF
	        	//  /target/classes
	        	if(dir.createRelative("../../../../target/classes").exists()){
	        		return  dir.createRelative("../../../../");
	        	}
	        	
	        	//project
	        	//  /src/main/webapps/{appname}/WEB-INF
	        	//  /target/classes
	        	if(dir.createRelative("../../../../../target/classes").exists()){
	        		return  dir.createRelative("../../../../../");
	        	}
	        }
	        
	        return null;
        } catch (Exception e) {
        	log.warn("Error detecting maven project home in webapp," + e.getMessage(), e);
        	return null;
        }
	}
	
	protected static FileResource getStandaloneMavenProjectHome() {
		FileResource userDir = Resources.getUserDir();
		
		if(userDir.createRelative("target/classes").exists()){
			return userDir;
		}
		
		return null;
	}
}
