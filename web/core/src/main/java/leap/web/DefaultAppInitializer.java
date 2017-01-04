/*
 * Copyright 2013 the original author or authors.
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

import leap.core.AppConfig;
import leap.core.AppConfigException;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import leap.web.action.ActionStrategy;
import leap.web.config.ModuleConfig;
import leap.web.error.ErrorsConfig;
import leap.web.route.RouteManager;

public class DefaultAppInitializer implements AppInitializer {
	
	private static final Log log = LogFactory.get(DefaultAppInitializer.class);

    protected @Inject @M ActionStrategy as;
    protected @Inject @M RouteManager   rm;

	@Override
	public void initialize(App app) throws AppConfigException {
		this.loadConfig(app);
		this.loadRoutesFromConfigs(app);
		this.loadRoutesFromClasses(app);
	}
	
	protected void loadConfig(App app) {
		AppConfig config = app.config();
		
		ErrorsConfig errorsConfig = config.removeExtension(ErrorsConfig.class);
		if(null != errorsConfig){
			app.errorViews().addErrorViews(errorsConfig);
			app.errorCodes().addErrorCodes(errorsConfig.getExceptionCodeMappings());
		}
	}
	
	protected void loadRoutesFromConfigs(App app){
		//not implemented
	}
	
	//Loads the routes from scanned classes
	protected void loadRoutesFromClasses(final App app){
		//Load web app's routes.
        log.debug("Load routes[base-path=/] from classes in base package '{}'", app.getBasePackage());
		final String basePackage = app.getBasePackage();
		app.config().getResources().processClasses((cls) -> {
			if(cls.getName().startsWith(basePackage)){
				if(as.isControllerClass(cls)){
					loadControllerClass(app, "/", cls);
				}					
			}
		});

		//Load web module's routes.
		for(ModuleConfig module : app.getWebConfig().getModules()){
			// don't duplicate load controller
			if(Strings.startsWith(module.getBasePackage()+".",basePackage+".")){
				if(Strings.isEmpty(module.getBasePath())||Strings.equals("/",module.getBasePath())){
					continue;
				}
			}
            ResourceSet rs = Resources.scanPackage(module.getBasePackage());

            if(rs.isEmpty()) {
                log.info("No resource scanned in base package '{}' of module '{}', is the module exists?");
            }else{
                String appContextPath    = app.getContextPath().equals("") ? "/" : app.getContextPath();
                String moduleContextPath = module.getContextPath();

                if(Strings.isEmpty(moduleContextPath) || appContextPath.equals(moduleContextPath)) {

                    log.debug("Load routes[base-path={}' from classes in base package '{}' of module '{}'.",
                            module.getBasePath(), module.getBasePackage(), module.getName());

                    rs.processClasses((cls) -> {
                        if(as.isControllerClass(cls)) {
                            loadControllerClass(app, module.getBasePath(), cls);
                        }
                    });

                }
            }
        }
	}
	
	protected void loadControllerClass(App app, String basePath, Class<?> cls) {
        rm.loadRoutesFromController(app.routes(), cls, basePath);
	}
}