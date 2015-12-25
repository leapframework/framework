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

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.annotation.R;
import leap.core.ioc.PostCreateBean;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.action.ActionStrategy;
import leap.web.route.RoutesPrinter;

public abstract class AppHandlerBase implements AppHandler,PostCreateBean {
	
	protected final Log     log    = LogFactory.get(this.getClass());
	protected final boolean _trace = log.isTraceEnabled();
	protected final boolean _debug = log.isDebugEnabled();	
	
	private boolean _started;
	private boolean _stopped;
	private Object  _token;
	
    protected @Inject @M AppInitializer[] initializers;
    protected @Inject @M RoutesPrinter    routesPrinter;
    protected @Inject @M ActionStrategy   actionStrategy;

    protected @M App    app;
    protected @R String homePath;
	
	@Override
    public void setApp(App app) {
		this.app = app;
    }
	
	@Override
    public void initApp() throws Throwable {
		app._configure();
		
		for(AppInitializer initializer : initializers){
			initializer.initialize(app);
		}
		
		app._init();
    }

	@Override
    public final synchronized Object startApp() throws ServletException, IllegalStateException {
		if(_started){
			throw new IllegalStateException("The app aleady started");
		}
		
		_token = new Object();
		
		log.debug("Starting app[{}]...",getContextPathForPrint(app));
		
		this.preStart(app);
		
		try {
			app._start();
		} catch (Throwable e) {
			throw new ServletException("Error starting app '" + app.getClass().getName() + "' : " + e.getMessage(), e);
		}
		
		printRoutes(app);
		
		log.info("App[{}] started!\n",getContextPathForPrint(app)); 
		
		this.postStart(app);
		
		this._started = true;
		return _token;
    }
	
	@Override
    public final synchronized void stopApp(Object token) throws IllegalStateException {
		if(_stopped){
			throw new IllegalStateException("App aleady stopped");
		}
		
		if(token != _token){
			throw new IllegalStateException("The given token is invalid, cannot stop this app");
		}
		
		try {
			app._end();
		} catch (Throwable e) {
			log.warn("Error stopping app '" + app.getClass().getName() + "' : " + e.getMessage(), e);
		}
		
		this.doDestroy(app);
		
		log.info("App[{}] ended!\n",getContextPathForPrint(app));
		
		this._stopped = true;
    }
	
	protected void printRoutes(App app){
		if(log.isInfoEnabled()){
			if(app.routes().isEmpty()){
				log.info("No routes defined in app '{}'",getContextPathForPrint(app));
			}else{
				StringWriter printedRoutes = new StringWriter();
				PrintWriter writer = new PrintWriter(printedRoutes);
				routesPrinter.print(app.routes(), writer);
				log.info("Routes of app '{}' : \n\n{}",getContextPathForPrint(app), printedRoutes.toString());	
			}
		}
	}
	
	protected String getContextPathForPrint(App app){
	    return AppBootstrap.getAppDisplayName(app.getServletContext());
	}
	
	@Override
    public void postCreate(BeanFactory beanFactory) throws Throwable {
		this.homePath = "/" + actionStrategy.getIndexActionName();
    }

	protected void preStart(App app) {
		
	}
	
	protected void postStart(App app){
		
	}
	
	protected void doDestroy(App app) {
		
	}
}
