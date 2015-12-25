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
package leap.core;

import leap.lang.Args;
import leap.lang.Named;

public class AppProfile implements Named{
	
	public static final AppProfile DEVELOPMENT = new AppProfile("development");
	public static final AppProfile TESTING     = new AppProfile("testing");
	public static final AppProfile PRODUCTION  = new AppProfile("production");
	
	private final String name;
	
	public AppProfile(String name){
		Args.notEmpty(name,"profile name");
		this.name = name;
	}

	@Override
    public String getName() {
	    return name;
    }
	
	public boolean isDevelopment(){
		return DEVELOPMENT.matches(name);
	}

	public boolean isTesting(){
		return TESTING.matches(name);
	}
	
	public boolean isProduction(){
		return PRODUCTION.matches(name);
	}
	
	public boolean matches(String name){
		return matches(name,true);
	}
	
	public boolean matches(String name,boolean ignorecase){
		return ignorecase ? this.name.equalsIgnoreCase(name) : this.name.equals(name);
	}

	@Override
    public String toString() {
		return name;
    }
}