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
import leap.core.validation.annotations.NotNull;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.FileResource;
import leap.lang.resource.Resources;
import leap.lang.servlet.ServletResource;
import leap.lang.servlet.Servlets;

import java.io.File;

public class DefaultAppHome implements AppHome, PostCreateBean {
	
	private static final Log log = LogFactory.get(DefaultAppHome.class);
	
	protected @NotNull AppContext   context;
	protected @NotNull AppConfig    config;
	protected @NotNull FileResource dir;
	
	@Override
	public boolean exists() {
		return dir.exists();
	}

	@Override
	public File dir() {
		return dir.getFile();
	}
	
	@Override
    public AppHome forceCreate() {
		if(!dir.getFile().exists()){
			dir.getFile().mkdirs();
		}
		return this;
    }

	@Override
    public FileResource getResource(String relativePath) {
	    return dir.createRelative(relativePath);
    }

	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		this.context = factory.getAppContext();
		this.config  = factory.getAppConfig();
		
		initHomeDirectory();

        /*
        if(log.isInfoEnabled()) {
            log.info("\n\n   *** app home : {} ***\n", dir.getFilepath());
        }
        */

    }
	
	protected void initHomeDirectory() throws Throwable {
		if(initHomeDirectoryFromConfig()){
			return;
		}else if(initHomeDirectoryFromWebapp()){
			return;
		}else{
			FileResource userDir = Resources.createFileResource(System.getProperty("user.dir"));
			initHomeDirectoryByDefault(userDir);
			if(null == dir){
				dir = userDir;
			}
		}
	}
	
	protected boolean initHomeDirectoryFromConfig() throws Throwable {
		String homeDirectory = config.getProperty(AppConfig.PROPERTY_HOME);
		if(!Strings.isEmpty(homeDirectory)){
			dir = Resources.createFileResource(homeDirectory);
			return true;
		}else {
			return false;
		}
	}
	
	protected boolean initHomeDirectoryFromWebapp() throws Throwable {
		if(context.isServletEnvironment()){
			ServletResource r = Servlets.getResource(context.getServletContext(), "/WEB-INF");
			if(r.exists() && r.isFile()){
				dir = Resources.createFileResource(r.getFile());
				
				//project
				//  /src/main/webapp/WEB-INF
				//  /target/classes
				if(dir.createRelative("../../../../target/classes").exists()){
					dir = dir.createRelative("../../../../target");
				}
				
				return true;
			}
		}
		return false;
	}
	
	protected void initHomeDirectoryByDefault(FileResource userDir) throws Throwable {
		//Auto detect development environment (maven)
		if(userDir.createRelative("target/classes").exists()){
			dir = userDir.createRelative("target");
		}else{
			dir = userDir;
		}
	}
	
}
