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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class OrmModelsConfig {

	private String dataSource;
	private Map<String, OrmModelPkgConfig> basePackages = new LinkedHashMap<>();
	private Map<String, OrmModelClassConfig> classes = new LinkedHashMap<>();

	public void addBasePackage(OrmModelPkgConfig c) {
		basePackages.put(c.getPkg(),c);
	}

    public OrmModelPkgConfig removeBasePackage(String p) {
        return basePackages.remove(p);
    }
	
	public void addClassConfig(OrmModelClassConfig clzz) {
		classes.put(clzz.getClassName(),clzz);
	}

    public OrmModelClassConfig removeClass(String cn) {
        return classes.remove(cn);
    }
	
	public Map<String, OrmModelPkgConfig> getBasePackages() {
		return basePackages;
	}

	public Map<String, OrmModelClassConfig> getClasses() {
		return classes;
	}

	public void addAll(OrmModelsConfig models) {
		basePackages.putAll(models.basePackages);
		classes.putAll(models.classes);
	}
	
	public boolean contains(Class<?> cls) {
		return isClassesContains(cls)|| isPackageContains(cls);
	}

	public boolean isClassesContains(Class<?> cls){
		return classes.containsKey(cls.getName());
	}
	
	public boolean isPackageContains(Class<?> cls){
		String clsPackageName = Classes.getPackageName(cls) + ".";
		for(OrmModelPkgConfig basePackage : basePackages.values()) {
			if(clsPackageName.startsWith(basePackage.getPkg())) {
				return true;
			}
		}
		return false;
	}
	
	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
}
