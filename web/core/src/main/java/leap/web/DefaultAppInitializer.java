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
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.validation.ValidationManager;
import leap.core.web.path.PathTemplate;
import leap.core.web.path.PathTemplateFactory;
import leap.lang.Strings;
import leap.lang.beans.BeanException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.naming.NamingStyles;
import leap.lang.path.Paths;
import leap.lang.reflect.ReflectClass;
import leap.lang.reflect.ReflectMethod;
import leap.lang.reflect.ReflectParameter;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import leap.web.action.*;
import leap.web.action.Argument.Location;
import leap.web.annotation.*;
import leap.web.config.ModuleConfig;
import leap.web.error.ErrorsConfig;
import leap.web.format.ResponseFormat;
import leap.web.multipart.MultipartFile;
import leap.web.route.RouteBuilder;
import leap.web.view.View;
import leap.web.view.ViewSource;

import javax.servlet.http.Part;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DefaultAppInitializer implements AppInitializer {
	
	private static final Log log = LogFactory.get(DefaultAppInitializer.class);
	
    protected @Inject @M BeanFactory         factory;
    protected @Inject @M ActionStrategy      as;
    protected @Inject @M ActionManager       am;
    protected @Inject @M ViewSource          viewSource;
    protected @Inject @M ValidationManager   validationManager;
    protected @Inject @M PathTemplateFactory pathTemplateFactory;	

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
            ResourceSet rs = Resources.scanPackage(module.getBasePackage());

            if(rs.isEmpty()) {
                log.info("No resource scanned in base package '{}' of module '{}', is the module exists?");
            }else{
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
	
	protected void loadControllerClass(App app, String basePath, Class<?> cls) {
		//An controller can defines two or more controller path
		//i.e User.class can define paths : /user and /users
		String[] controllerPaths = as.getControllerPaths(cls);

        String pathPrefix = "";
        if(!basePath.equals("/")) {
            pathPrefix = Paths.prefixWithAndSuffixWithoutSlash(basePath);
        }

        for(String controllerPath : controllerPaths){
			//Normalize the path
			if(!controllerPath.startsWith("/")){
				controllerPath = "/" + controllerPath;
			}
			if(controllerPath.endsWith("/")){
				controllerPath = controllerPath.substring(0,controllerPath.length() - 1);
			}

            controllerPath = pathPrefix + controllerPath;
			
			//Scan all action methods in controller class.
			ReflectClass rcls = ReflectClass.of(cls);
			
			//Create controller instance
			Object controller = as.getControllerInstance(cls);
			
	        ControllerHolder controllerHolder = new ControllerHolder(controller,rcls,controllerPath);
	        
			for(ReflectMethod rm : rcls.getMethods()){
				if(rm.isGetterMethod() || rm.isSetterMethod()) {
                    continue;
                }

                if(!as.isActionMethod(rm.getReflectedMethod())) {
                    continue;
                }

				Class<?> declaringClass = rm.getReflectedMethod().getDeclaringClass();
				if(declaringClass.equals(ControllerBase.class) || declaringClass.equals(Results.class)){
					continue;
				}
				
				loadActionMethod(app, controllerHolder, rm);
			}
		}
	}
	
	protected void loadActionMethod(App app,ControllerHolder cholder, ReflectMethod rm){
		ActionBuilder action = createAction(app, cholder, rm);
		
		ActionMapping[] mappings = as.getActionMappings(action);
		
		for(ActionMapping m : mappings){
			addActionRoute(app, cholder.cls(), cholder.path(), action, m);
		}
	}
	
	protected void addActionRoute(App app, 
							      Class<?> controllerClass,String controllerPath,
							      ActionBuilder action,ActionMapping mapping) {
		
		StringBuilder path = new StringBuilder();
		
		String actionPath = mapping.getPath();
		
		ResponseFormat responseFormat = null;
		int lastDotIndex = actionPath.lastIndexOf('.');
		if(lastDotIndex > 0) {
			String actionPathWithoutExt = actionPath.substring(0, lastDotIndex);
			String actionPathExt		= actionPath.substring(lastDotIndex + 1);
			
			responseFormat = app.getWebConfig().getFormatManager().tryGetResponseFormat(actionPathExt);
			if(null != responseFormat) {
				actionPath = actionPathWithoutExt;
			}
		}

		boolean restful = null != controllerClass && controllerClass.isAnnotationPresent(RestController.class);
		
		if(!Strings.isEmpty(actionPath)){
			if(!restful && actionPath.startsWith("/")){
				path.append(actionPath);
			}else{
				path.append(controllerPath).append(Paths.prefixWithSlash(actionPath));
			}
		}else{
			path.append(controllerPath);
		}
		
		if(path.length() > 0 && path.charAt(path.length() - 1) == '/'){
			path.deleteCharAt(path.length() - 1);
		}
		
		//create path template
		PathTemplate pathTemplate = pathTemplateFactory.createPathTemplate(path.toString());
		
		//Resolve path variables
		if(pathTemplate.hasVariables()) {
			for(ArgumentBuilder a : action.getArguments()) {
				if(null == a.getLocation()) {
					String name = NamingStyles.LOWER_UNDERSCORE.of(a.getName());
					for(String v : pathTemplate.getTemplateVariables()) {
						if(name.equals(NamingStyles.LOWER_UNDERSCORE.of(v))){
							a.setLocation(Location.PATH_PARAM);
							a.setName(v);
						}
					}
				}
			}
		}
		
		//create route
		RouteBuilder route = new RouteBuilder();
		route.setSource(controllerClass);
		route.setControllerPath(controllerPath);
		route.setMethod(mapping.getMethod());
		route.setPathTemplate(pathTemplate);
		route.setResponseFormat(responseFormat);
		route.setRequiredParameters(mapping.getParams());
		route.setSupportsMultipart(supportsMultipart(action));
		
		//resole default view path
		String[] defaultViewNames = as.getDefaultViewNames(action, controllerPath, actionPath, pathTemplate);
		for(String defaultViewName : defaultViewNames){
			try {
				View view = resolveView(app, defaultViewName);
				if(null != view){
					route.setDefaultView(resolveView(app, defaultViewName));
					route.setDefaultViewName(defaultViewName);
					break;
				}else if(null == route.getDefaultViewName()){
					route.setDefaultViewName(defaultViewName);
				}
	        } catch (Throwable e) {
	        	throw new AppConfigException("Error resolving action's default view '" + defaultViewName + "', " + e.getMessage(),e);
	        }
		}
		
		Action act = action.build();
		
		//create action.
		route.setAction(act);
		
		//validation
		AcceptValidationError acceptValidationError = act.searchAnnotation(AcceptValidationError.class);
		if(null != acceptValidationError) {
		    route.setAcceptValidationError(true);
		}
		
		//https only
		HttpsOnly httpsOnly = act.searchAnnotation(HttpsOnly.class);
		if(null != httpsOnly) {
		    route.setHttpsOnly(httpsOnly.value());
		}
		
		//resolve failure handlers
		Failures failures = act.searchAnnotation(Failures.class);
		if(null != failures) {
			for(Failure failure : failures.value()) {
				addFailureHandler(route, failure);
			}
		}
		
		Failure failure = route.getAction().searchAnnotation(Failure.class);
		if(null != failure) {
			addFailureHandler(route, failure);
		}
		
		//prepare the action
		am.prepareAction(route);
		
		//add route
		app.routes().add(route.build());
	}
	
	protected void addFailureHandler(RouteBuilder route, Failure a) {
		Function<ActionExecution, Boolean> cond = null;
		if(!Object.class.equals(a.exception())) {
			cond = (execution) -> null != execution.getException() && a.exception().isAssignableFrom(execution.getException().getClass());
		}
		
		if(!FailureHandler.class.equals(a.handler())) {
			FailureHandler handler = factory.createBean(a.handler());
			route.addFailureHandler(new ConditionalFailureHandler(cond) {
				@Override
				protected void doHandlerFailure(ActionContext context, ActionExecution execution, Result result) {
					handler.handleFailure(context, execution, result);
				}
			});
		}else if(!a.view().isEmpty() || !a.value().isEmpty()) {
			route.addFailureHandler(new RenderViewFailureHandler(cond, Strings.firstNotEmpty(a.view(), a.value())));
		}
	}

	protected ActionBuilder createAction(App app,ControllerHolder ch,ReflectMethod m) {
		MethodActionBuilder action = new MethodActionBuilder(ch.controller,m);

		action.getInterceptors().addAll(resolveActionInterceptors(app, ch, m));
		
		for(int i=0;i<m.getParameters().length;i++){
			action.addArgument(createArgument(app, m, m.getParameters()[i]));
		}
		
		return action;
	}
	
	protected boolean supportsMultipart(ActionBuilder action) {
		if(action.isAnnotationPresent(Multipart.class)) {
			return true;
		}
		
		for(ArgumentBuilder a : action.getArguments()) {
			if(a.getType().equals(Part.class) 
					|| a.getType().equals(MultipartFile.class) 
					|| a.getType().isAnnotationPresent(Multipart.class)) {
				return true;
			}
		}
		
		return false;
	}
	
	protected ArgumentBuilder createArgument(App app,ReflectMethod m, ReflectParameter p) {
		ArgumentBuilder a = new ArgumentBuilder(validationManager, p);
		
		return a;
	}
	
	@SuppressWarnings("unchecked")
    protected List<ActionInterceptor> resolveActionInterceptors(App app,ControllerHolder ch,ReflectMethod m) {
		List<ActionInterceptor> interceptors = new ArrayList<>();
		
		InterceptedBy a = m.getAnnotation(InterceptedBy.class);
		if(null != a && a.value().length > 0) {
			for(Class<? extends ActionInterceptor> c : a.value()) {
				try {
	                interceptors.add(app.factory().createBean((Class<ActionInterceptor>)c));
                } catch (BeanException e) {
                	log.error("Error creating bean of action interceptor '{}' : {}", c, e.getMessage());
                	throw e;
                }
			}
		}
		
		if(ch.controller instanceof ActionInterceptor) {
			interceptors.add((ActionInterceptor)ch.controller);
		}
		
		return interceptors;
	}
	
	protected View resolveView(App app,String viewPath) throws Throwable {
		return viewSource.getView(viewPath, app.getDefaultLocale());
	}
	
	protected static class ControllerHolder {
		private final Object       controller;
		private final ReflectClass reflectClass;
		private final String	   path;
		
		protected ControllerHolder(Object controller,ReflectClass reflectClass,String path){
			this.controller   = controller;
			this.reflectClass = reflectClass;
			this.path         = path;
		}

		public Object controller() {
			return controller;
		}

		public Class<?> cls(){
			return reflectClass.getReflectedClass();
		}

		public String path() {
			return path;
		}
	}
}