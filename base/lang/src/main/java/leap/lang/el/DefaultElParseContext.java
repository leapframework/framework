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
package leap.lang.el;

import java.util.Collection;
import java.util.Map;

public class DefaultElParseContext extends AbstractElParseContext {
	
	protected ElParseContext parent;

	public DefaultElParseContext() {
	    super();
    }

	public DefaultElParseContext(ElParseContext parent) {
		super();
		this.parent = parent;
	}
	
	public DefaultElParseContext(Map<String, ElFunction> funcs) {
	    super(funcs);
    }
	
	public ElParseContext getParent() {
		return parent;
	}
	
	public void setParent(ElParseContext parent) {
		this.parent = parent;
	}

	public Map<String,ElFunction> getFunctions() {
		return funcs;
	}
	
	public Map<String, Object> getVariables(){
		return vars;
	}
	
	public void setFunction(String fullName,ElFunction func){
		funcs.put(fullName, func);
	}
	
	public void setVariable(String name, Object value){
		vars.put(name, value);
	}
	
	public void importPackage(String name) {
		if(!pkgs.contains(name)){
			pkgs.add(name);	
		}
	}
	
	public void importPackages(Collection<String> c){
		for(String name : c){
			importPackage(name);
		}
	}
	
	@Override
    public Object resolveVariable(String name) {
		Object v = super.resolveVariable(name);
		if(null == v && null != parent){
			return parent.resolveVariable(name);
		}
		return v;
	}

	@Override
    public ElFunction resolveFunction(String fullname) {
	    ElFunction f = super.resolveFunction(fullname);
	    if(null == f && null != parent){
	    	return parent.resolveFunction(fullname);
	    }
	    return f;
    }

	@Override
    public String getMessage(String key, Object... args) {
	    return ElMessages.get(key, args);
    }
}