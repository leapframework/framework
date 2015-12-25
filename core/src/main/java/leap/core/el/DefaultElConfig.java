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
package leap.core.el;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import leap.core.AppConfig;
import leap.core.BeanFactory;
import leap.core.el.ElConfigFunctions.ElConfigFunction;
import leap.core.ioc.PostCreateBean;
import leap.core.variable.VariableEnvironment;
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.el.ElFunction;
import leap.lang.el.ElStaticMethod;

public class DefaultElConfig implements ElConfig,PostCreateBean {
	protected Map<String, Object>     variables = new HashMap<>();
	protected Map<String, ElFunction> functions = new HashMap<>();
	protected List<String>            packages  = new ArrayList<String>();
	
	@Override
    public List<String> getImportedPackages() {
	    return packages;
    }
	
	/**
	 * Returns the registered variables map.
	 */
	@Override
    public Map<String, Object> getRegisteredVariables() {
		return variables;
	}
	
	/**
	 * Returns the registered functions map.
	 */
	@Override
    public Map<String, ElFunction> getRegisteredFunctions() {
		return functions;
	}
	
	@Override
    public boolean isPackageImported(String name) {
	    return packages.contains(name);
    }

	/**
	 * Returns <code>true</code> if the given variable name aleady registered.
	 */
	@Override
    public boolean isVariableRegistered(String name){
		return variables.containsKey(name);
	}
	
	/**
	 * Returns <code>true
	 */
	@Override
    public boolean isFunctionRegistered(String prefix,String name) {
		return functions.containsKey(getFunctionFullName(prefix, name));
	}
	
	@Override
    public ElConfig importPackage(String packageName) {
		if(!packages.contains(packageName)){
			packages.add(packageName);	
		}
		return this;
    }
	
	/**
	 * Register a global variable.
	 */
	@Override
    public ElConfig registerVariable(String name,Object value) {
		variables.put(name,value);
		return this;
	}
	
	/**
	 * Register a global function.
	 */
	@Override
    public void registerFunction(String prefix,String name,Method m) {
		int modifiers = m.getModifiers();
		Args.assertTrue(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers), 
						"Method '" + m.getName() + "' must be 'public static'");
		functions.put(getFunctionFullName(prefix, name), new ElStaticMethod(m));
	}
	
	@Override
    public void registerFunction(String prefix, String name, ElFunction func) {
		functions.put(getFunctionFullName(prefix, name), func);
    }

	/**
	 * Register filtered methods in the give class as functions.
	 * 
	 * <p>
	 * The methods are not public and static will be removed default.
	 */
	@Override
    public void registerFunctions(String prefix, Class<?> c,Predicate<Method> filter) {
		for(Method m : c.getMethods()) {
			if(Modifier.isStatic(m.getModifiers()) && filter.test(m)){
				registerFunction(prefix, m.getName(), m);
			}
		}
	}

	public String getFunctionFullName(String prefix, String name) {
		return Strings.isEmpty(prefix) ? name : (prefix + FUNCTION_NAME_SEPERATOR + name);
	}

	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		registerDefaults(factory);
		registerConfigs(factory.getAppConfig());
		
		for(Entry<String, ElFunction> entry : factory.getNamedBeans(ElFunction.class).entrySet()){
			registerFunction(null, entry.getKey(), entry.getValue());
		}
		
		for(ElConfigurator bean : factory.getBeans(ElConfigurator.class)){
			bean.configure(this);
		}
		
		ElConfig config = factory.getAppConfig().removeExtension(ElConfig.class);
		if(null != config){
			registerConfigs(config);
		}
    }
	
	protected void registerDefaults(final BeanFactory beanFactory) {
		registerVariable("env",new EnvPropertyResolver(beanFactory.getBean(VariableEnvironment.class)));
		registerVariable("beans",new BeansPropertyResolver(beanFactory));
	}
	
	protected void registerConfigs(AppConfig config) {
		ElConfigFunctions funcs = config.removeExtension(ElConfigFunctions.class);
		if(null != funcs) {
			for(ElConfigFunction func : funcs.all()){
				func.resolve();
				registerFunction(func.funcPrefix, func.funcName, func.function);
			}
		}
	}
	
	protected void registerConfigs(ElConfig config){
		variables.putAll(config.getRegisteredVariables());
		functions.putAll(config.getRegisteredFunctions());
	}
}