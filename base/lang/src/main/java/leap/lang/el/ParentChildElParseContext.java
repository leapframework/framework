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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParentChildElParseContext implements ElParseContext {
	
	private final ElParseContext parent;
	private final ElParseContext child;
	private final List<String>   pkgs = new ArrayList<String>();
	private final List<String>   pkgsImmutableView = Collections.unmodifiableList(pkgs);
	
	public ParentChildElParseContext(ElParseContext parent,ElParseContext child) {
		this.parent = parent;
		this.child  = child;
		
		for(String p : parent.getImportedPackages()){
			if(!child.getImportedPackages().contains(p)){
				pkgs.add(p);
			}
		}
		pkgs.addAll(child.getImportedPackages());
	}
	
	@Override
    public List<String> getImportedPackages() {
	    return pkgsImmutableView;
    }
	
	@Override
	public <T> T convert(Object v, Class<T> targetType) {
		return child.convert(v, targetType);
	}

	@Override
	public String toString(Object v) {
		return child.toString();
	}

	@Override
	public boolean test(Object v) {
		return child.test(v);
	}

	@Override
	public String getMessage(String key, Object... args) {
		return child.getMessage(key, args);
	}

	@Override
	public Object resolveVariable(String name) {
		Object o = child.resolveVariable(name);
		return null == o ? parent.resolveVariable(name) : o;
	}

	@Override
	public ElFunction resolveFunction(String fullname) {
		ElFunction f = child.resolveFunction(fullname);
		return null == f ? parent.resolveFunction(fullname) : f;
	}
}
