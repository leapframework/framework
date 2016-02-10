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
package leap.web.action;

import leap.core.AppConfigException;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.web.path.PathTemplate;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.http.HTTP;
import leap.lang.path.Paths;
import leap.web.App;
import leap.web.annotation.*;
import leap.web.annotation.http.AMethod;
import leap.web.config.WebConfig;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * The default implementation of {@link ActionStrategy}.
 */
public class DefaultActionStrategy implements ActionStrategy {

    public static final String   CONTROLLER_SUFFIX   = "Controller";
    public static final String[] CONTROLLER_PACKAGES = new String[]{"controllers", "controller"};

    protected @Inject App         app;
    protected @Inject BeanFactory factory;
    protected @Inject WebConfig   config;

	@Override
    public boolean isControllerClass(Class<?> cls) {
		if(cls.isAnnotationPresent(NonController.class)){
			return false;
		}

        if(cls.isInterface() || Modifier.isAbstract(cls.getModifiers())) {
            return false;
        }

        //must defines public constructor(s).
        if(!Modifier.isPublic(cls.getModifiers()) && cls.getConstructors().length == 0) {
            return false;
        }

        if(Classes.isAnnotatioinPresent(cls.getAnnotations(), Controller.class, true)) {
            return true;
        }

        String name = cls.getSimpleName();
        if(name.endsWith(CONTROLLER_SUFFIX) && name.length() > CONTROLLER_SUFFIX.length()) {
            return true;
        }

		return false;
    }

    @Override
    public boolean isActionMethod(Method m) {
        if(m.isAnnotationPresent(NonAction.class)) {
            return false;
        }

        if(!Modifier.isPublic(m.getModifiers())) {
            return false;
        }

        if(Modifier.isStatic(m.getModifiers())) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isIndexAction(ActionBuilder action) {
	    return Strings.equalsIgnoreCase(config.getIndexActionName(),action.getName()) ||
               Classes.isAnnotatioinPresent(action.getAnnotations(), Index.class);
    }

	@Override
    public String getControllerName(Class<?> cls) {
		int suffixStartIndex = cls.getSimpleName().indexOf(CONTROLLER_SUFFIX);
		if(suffixStartIndex > 0){
			return Strings.lowerUnderscore(cls.getSimpleName().substring(0,suffixStartIndex));
		}else{
			return Strings.lowerUnderscore(cls.getSimpleName());
		}
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getControllerInstance(Class<?> cls) {
		Object controller = factory.tryGetBean((Class<Object>)cls);
		if(null != controller){
			return controller;
		}
		
        try {
        	return factory.newInstance(cls);
        } catch (Exception e) {
        	throw new AppConfigException("Error creating instance of controller '" + cls.getName() + "' : " + e.getMessage(), e);
        }
    }

	@Override
    public String[] getControllerPaths(Class<?> cls) {
		//get path from annotation
		Path a = cls.getAnnotation(Path.class);
		if(null != a){
            if(a.value().length > 1) {
                throw new AppConfigException("The @Path annotation only supports one path value in controller");
            }

            if(cls.getEnclosingClass() != null && isControllerClass(cls.getEnclosingClass())) {
                String[] parentPaths = getControllerPaths(cls.getEnclosingClass());
                if(parentPaths.length == 1) {
                    return new String[]{parentPaths[0] + Paths.prefixWithSlash(a.value()[0])};
                }else{
                    throw new IllegalStateException("Controller only supports one path value");
                }
            }
            return new String[]{Paths.prefixWithSlash(a.value()[0])};
		}

		//get path conventional
		String controllerName = getControllerName(cls);
		
		String   pathPrefix             = "";
		String   clsPackageName         = Classes.getPackageName(cls);
		String[] baseControllerPackages = controllerPackagePrefixes();
		
		for(String baseControllerPackage : baseControllerPackages) {
			if(clsPackageName.startsWith(baseControllerPackage)){
				//package name starts with {base-package}.controller
				pathPrefix = clsPackageName.substring(baseControllerPackage.length()).replace('.', '/');
				break;
			}
		}

        String pathSuffix = isHome(controllerName) ? "" : controllerName.toLowerCase();

        if(cls.getEnclosingClass() != null && isControllerClass(cls.getEnclosingClass())) {
            String[] parentPaths = getControllerPaths(cls.getEnclosingClass());
            if(parentPaths.length == 1) {
                pathPrefix = pathPrefix + Paths.prefixWithSlash(parentPaths[0]);
            }else{
                throw new IllegalStateException("Controller only supports one path value");
            }
        }

        if("/".equals(pathPrefix)) {
            pathPrefix = "";
        }

		if(Strings.isEmpty(pathPrefix)){
			return new String[]{pathSuffix};
		}else{
			return new String[]{pathPrefix + "/" + pathSuffix};
		}
    }

	public ActionMapping[] getActionMappings(ActionBuilder action) {
		List<ActionMapping> mappings = new ArrayList<>();
		
		Annotation[] annotations = action.getAnnotations();
		
		String defaultMethod = "*";
		for(Annotation a : annotations){
			if(a.annotationType().isAnnotationPresent(AMethod.class)){
				defaultMethod = a.annotationType().getSimpleName().toUpperCase();
				break;
			}
		}
		
		String defaultPath = "";
		
		//Path annotation
		Path pa = Classes.getAnnotation(annotations, Path.class);
		if(null != pa){
			if(pa.value().length == 1){
				defaultPath = pa.value()[0];
			}
			
			for(String path : pa.value()){
				mappings.add(new ActionMapping(path,defaultMethod));
			}
		}
		
		if(Strings.isEmpty(defaultPath)){
			defaultPath = getActionPathByName(action.getName());
		}
		
		//RequestMappings annotation
		RequestMappings rms = Classes.getAnnotation(annotations, RequestMappings.class);
		if(null != rms){
			for(RequestMapping rm : rms.value()){
				mappings.add(createActionMapping(action, defaultMethod, defaultPath, rm));
			}
		}
		
		//RequestMapping annotation
		RequestMapping rm = Classes.getAnnotation(annotations, RequestMapping.class);
		if(null != rm){
			mappings.add(createActionMapping(action, defaultMethod, defaultPath, rm));
		}

        //Check is restful style.
        Object controller = action.getController();
        boolean restful = false;
        if(null != controller && controller.getClass().isAnnotationPresent(Restful.class)) {
            restful = true;
        }

		//Default mappings
		if(mappings.isEmpty()){
            //index path ""
            if(isIndexAction(action)){
                mappings.add(new ActionMapping("", defaultMethod));
            }

            if(restful) {
                //checks default-method
                if("*".equals(defaultMethod)) {
                    if(action.hasReturnType()) {
                        defaultMethod = HTTP.Method.GET.name();
                    }else{
                        defaultMethod = HTTP.Method.POST.name();
                    }
                }
                mappings.add(new ActionMapping("", defaultMethod));
            }else{
                //action name to path
                mappings.add(new ActionMapping(getActionPathByName(action.getName()), defaultMethod));
            }
		}
		
		return mappings.toArray(new ActionMapping[mappings.size()]);
	}
	
	protected ActionMapping createActionMapping(ActionBuilder action, String defaultMethod, String defaultPath, RequestMapping mapping) {
		String method = mapping.method().isNone() ? defaultMethod : mapping.method().name();
		String path   = Strings.firstNotEmpty(mapping.path(),defaultPath);
		String params = mapping.params();
		
		ActionMapping am = new ActionMapping(path, method);
		
		if(!Strings.isEmpty(params)){
			String[] keyValues = Strings.split(params,"&");
			for(String keyValue : keyValues){
				String[] keyValuePair = Strings.split(keyValue,"=");
				if(keyValuePair.length != 2){
					throw new AppConfigException("Invalid 'params' value '" + keyValue + "' in RequestMapping annotation on action '" + action + "'");
				}
				am.getParams().put(keyValuePair[0], keyValuePair[1]);
			}
		}
		
		return am;
	}

	@Override
    public String[] getDefaultViewNames(ActionBuilder action, String controllerPath, String actionPath, PathTemplate pathTemplate) {
		String fullActionPath  = pathTemplate.getTemplate();
		String defaultViewPath = null;
		if(fullActionPath.equals("/")){
			defaultViewPath = "/" + Strings.lowerUnderscore(action.getName());
		}else if(!pathTemplate.hasVariables()){
			defaultViewPath = pathTemplate.getTemplate();
		}
		
		String controllerActionPath = controllerPath + "/" + Strings.lowerUnderscore(action.getName());
		if(null == defaultViewPath){
			return new String[]{controllerActionPath};
		}else if(!Strings.equals(defaultViewPath, controllerActionPath)){
			return new String[]{defaultViewPath,controllerActionPath};
		}else{
			return new String[]{defaultViewPath};
		}
    }

    protected String[] controllerPackagePrefixes() {
        String[] a = new String[CONTROLLER_PACKAGES.length];
        for(int i=0;i<a.length;i++) {
            a[i] = app.config().getBasePackage() + "." + CONTROLLER_PACKAGES[i] + ".";
        }
        return a;
    }

    protected String getControllerPathByName(String controllerName) {
	    return nameToPath(controllerName);
    }

    protected String getActionPathByName(String actionName) {
	    return nameToPath(actionName);
    }

	protected String nameToPath(String name){
		//Removes the underscore like _name or name_ or _name_
		if(name.startsWith("_")){
			name = name.substring(1);
		}
		
		if(name.endsWith("_")){
			name = name.substring(0,name.length() - 1);
		}
		
		return Strings.lowerUnderscore(name);
	}
	
	protected boolean isHome(String controllerName){
		return Strings.equalsIgnoreCase(config.getHomeControllerName(),controllerName);
	}

}