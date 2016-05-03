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
package leap.core.meta;

import leap.core.AppResources;
import leap.core.BeanFactory;
import leap.core.ioc.PostCreateBean;
import leap.lang.Patterns;
import leap.lang.Props;
import leap.lang.Strings;
import leap.lang.collection.SimpleCaseInsensitiveMap;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.extension.ExProperties;
import leap.lang.meta.MPattern;
import leap.lang.resource.Resource;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class DefaultMPatternSource implements MPatternSource,PostCreateBean {
	
	private static final String REGEX_SUFFIX = ".regex";
	private static final String FLAGS_SUFFIX = ".flags";

	protected Map<String, MPattern> patterns = new SimpleCaseInsensitiveMap<>();
	
	@Override
	public MPattern getPattern(String name) throws ObjectNotFoundException {
		MPattern p = patterns.get(name);
		if(null == p){
			throw new ObjectNotFoundException("MPattern '" + name + "' not found");
		}
		return p;
	}

	@Override
	public MPattern tryGetPattern(String name) {
		return patterns.get(name);
	}

	@Override
    public void postCreate(BeanFactory beanFactory) throws Throwable {
		loadPatterns(AppResources.getAllClasspathResources("patterns", ".properties"));
    }

	private void loadPatterns(Resource[] rs) {
		for(Resource r : rs){
			loadPatterns(r);
		}
	}
	
	private void loadPatterns(Resource r) {
		if(r.exists()){
			ExProperties p = Props.load(r);
			
			for(Entry<Object, Object> entry : p.entrySet()){
				String name  = (String)entry.getKey();
				String value = (String)entry.getValue();
				
				if(name.endsWith(FLAGS_SUFFIX)){
					continue;
				}
				
				if(name.endsWith(REGEX_SUFFIX)){
					name = name.substring(0,name.length() - REGEX_SUFFIX.length());
				}
				
				addPattern(r, name, value, p.get(name + FLAGS_SUFFIX));
			}
		}
	}
	
	private void addPattern(Resource r, String name, String regex, String flags) {
		try {
			Pattern p;
			
			if(Strings.isEmpty(flags)){
				p = Pattern.compile(regex);	
			}else{
				p = Pattern.compile(regex,Patterns.parseFlags(flags));
			}
	        
	        patterns.put(name, new MPattern(name, p));
        } catch (Exception e) {
        	throw new IllegalStateException("Found invalid pattern '" + name + "', regex '" + regex + 
        								    "' in file '" + r.getClasspath() + "', " + e.getMessage(), e);
        }
	}
}