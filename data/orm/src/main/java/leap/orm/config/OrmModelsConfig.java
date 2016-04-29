/*
 * Copyright 2015 the original author or authors.
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
package leap.orm.config;

import leap.lang.Classes;

import java.util.LinkedHashSet;
import java.util.Set;

public class OrmModelsConfig {

	private Set<String> basePackages = new LinkedHashSet<String>();
	private Set<String> classNames	 = new LinkedHashSet<String>();

	public void addBasePackage(String p) {
		if(!p.endsWith(".")) {
			p = p + ".";
		}
		basePackages.add(p);
	}

    public boolean removeBasePackage(String p) {
        if(!p.endsWith(".")) {
            p = p + ".";
        }
        return basePackages.remove(p);
    }
	
	public void addClassName(String cn) {
		classNames.add(cn);
	}

    public boolean removeClassName(String cn) {
        return classNames.remove(cn);
    }
	
	public Set<String> getBasePackages() {
		return basePackages;
	}

	public Set<String> getClassNames() {
		return classNames;
	}

	public void addAll(OrmModelsConfig models) {
		basePackages.addAll(models.basePackages);
		classNames.addAll(classNames);
	}
	
	public boolean contains(Class<?> cls) {
		if(classNames.contains(cls.getName())) {
			return true;
		}
		
		String clsPackageName = Classes.getPackageName(cls) + ".";
		for(String basePackage : basePackages) {
			if(clsPackageName.startsWith(basePackage)) {
				return true;
			}
		}
		
		return false;
	}
}
