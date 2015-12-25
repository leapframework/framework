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
package leap.lang.el.spel.ast;

import java.util.List;

import leap.lang.Classes;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.el.ElEvalContext;
import leap.lang.el.ElParseContext;
import leap.lang.el.ElParseException;

/**
 * T(className).
 */
public class AstType extends AstExpr {
	
	private static final List<String> DEFAULT_PACKAGES = 
			New.arrayList("java.lang",
						  "java.util",
						  Classes.getPackageName(Strings.class));//leap.lang
	
	private String   name;
	private Class<?> cls;
	
	public AstType(ElParseContext context, String name) {
		this.name = name;
		this.cls  = resolveClass(context, name);
		
		if(null == cls){
			throw new ElParseException("Class name '" + name + "' cannot be resolved");
		}
	}

	@Override
	public Object eval(ElEvalContext context) {
		return cls;
	}

	@Override
	public void toString(StringBuilder out) {
		out.append("T(").append(name).append(")");
	}

	@Override
	protected void doAccept(AstVisitor visitor) {
		visitor.startVisit(this);
	}
	
	protected Class<?> resolveClass(ElParseContext context,String name) {
		Class<?> cls = null;
		
		if((cls = Classes.tryForName(name)) != null){
			return cls;
		}
		
		if((cls = resolveClass(context.getImportedPackages(), name)) != null){
			return cls;
		}
		
		if((cls = resolveClass(DEFAULT_PACKAGES, name)) != null){
			return cls;
		}
		
		return null;
	}
	
	protected Class<?> resolveClass(List<String> pkgs,String name) {
		for(int i=pkgs.size() - 1;i>=0;i--){
			String pkg = pkgs.get(i);
			
			if((cls = Classes.tryForName(pkg + "." + name)) != null){
				return cls;
			}
		}
		return null;
	}
}