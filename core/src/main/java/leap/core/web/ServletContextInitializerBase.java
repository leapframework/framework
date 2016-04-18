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
package leap.core.web;

import leap.core.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ServletContextInitializerBase {

	protected ServletContext     servletContext;
	protected AppContext         appContext;
	protected ServletBeanFactory beanFactory;
	
	protected final void initAppContext(ServletContext servletContext,Map<String, String> initProperties) throws ServletException {
		this.initAppContext(servletContext, initProperties, null);
	}
	
	protected final void initAppContext(final ServletContext      		   sc,
										final Map<String, String>          initParams,
										final Consumer<ServletBeanFactory> callback) throws ServletException {
		
		this.servletContext = sc;
		
		if(sc.getAttribute(AppContext.APP_CONTEXT_ATTRIBUTE) != null){
			throw new ServletException("AppContext aleady inited for the given servlet context '" + sc.getContextPath() + "'");
		}
		
		AppContext.removeCurrent();
		AppContextInitializer.initExternal(
		   sc,
		   
		   (config) -> {
				onAppConfigReady(config, initParams);
				
				beanFactory = new ServletBeanFactory(config,null);
				
				if(null != callback){
					callback.accept(beanFactory);
				}
				
				return beanFactory;
		   },
		   
		   (appContext) -> {
				ServletContextInitializerBase.this.appContext = appContext;
				sc.setAttribute(AppContext.APP_CONTEXT_ATTRIBUTE, appContext);
				
				beanFactory.load(appContext);
				onBeanFactoryReady(beanFactory);
		   },
		   
		   initParams
		);
		
		onAppContextReady(appContext);
	}
	
	protected void onAppConfigReady(AppConfig config, Map<String, String> initParams) {
		
	}
	
	protected void onBeanFactoryReady(BeanFactory factory) {
		
	}
	
	protected void onAppContextReady(AppContext context) {
		
	}
	
	protected final void destroyAppContext(){
		try{
			if(null != beanFactory){
				beanFactory.close();	
			}
		}finally{
			if(null != servletContext){
				servletContext.removeAttribute(AppContext.APP_CONTEXT_ATTRIBUTE);	
			}
			AppContext.removeCurrent();
		}
	}
	
	protected final class ServletBeanFactory extends DefaultBeanFactory {

		public ServletBeanFactory(AppConfig config, BeanFactory externalFactory) {
			super(config, externalFactory);
		}
		
		@Override
        protected DefaultBeanFactory load(AppContext appContext) {
	        return super.load(appContext);
        }
	}
}