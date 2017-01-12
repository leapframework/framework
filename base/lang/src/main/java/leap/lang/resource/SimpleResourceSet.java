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
package leap.lang.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.asm.ClassReader;
import leap.lang.collection.ArrayIterable;
import leap.lang.exception.NestedClassNotFoundException;
import leap.lang.exception.NestedIOException;
import leap.lang.io.IO;

public class SimpleResourceSet extends ArrayIterable<Resource> implements ResourceSet {
	
	protected Set<Class<?>> classes;
	
	public SimpleResourceSet(Resource[] values) {
	    super(values);
    }
	
	@Override
    public Resource[] toResourceArray() {
	    return values;
    }

	@Override
    public Resource getClasspathResource(String name) {
		Args.notEmpty(name,"name");
		
		for(Resource r : values){
			if(Strings.equals(r.getClasspath(), name)){
				return r;
			}
		}
		
	    return null;
    }

	@Override
    public Resource[] search(Predicate<Resource> predicate) {
	    return Arrays2.filter(values, predicate).toArray(new Resource[]{});
    }

	@Override
    public Resource[] searchUrls(String... locationPatterns) {
		Args.notNull(locationPatterns,"location patterns");
		
		Set<Resource> resources = new LinkedHashSet<Resource>();
		
		for(String locationPattern : locationPatterns){
			try {
		        for(int i=0;i<values.length;i++){
		        	Resource resource = values[i];
		        	
		        	if(Resources.matcher.match(locationPattern,resource.getURL().toExternalForm())){
		        		resources.add(resource);
		        	}
		        }
	        } catch (IOException e) {
	        	throw new NestedIOException(e);
	        }
		}

		return resources.toArray(new Resource[resources.size()]);
    }
	
	@Override
    public Resource[] searchClasspaths(String... classpathPatterns) {
		Args.notNull(classpathPatterns,"classpath patterns");
		
		Set<Resource> resources = new LinkedHashSet<Resource>();
		
		for(String classpathPattern : classpathPatterns){
			if(classpathPattern.startsWith("/")){
				classpathPattern = classpathPattern.substring(1);
			}
			
	        for(int i=0;i<values.length;i++){
	        	Resource resource = values[i];
	        	
	        	if(null != resource.getClasspath()){
		        	if(Resources.matcher.match(classpathPattern,resource.getClasspath())){
		        		resources.add(resource);
		        	}
	        	}
	        }
		}
		
		return resources.toArray(new Resource[resources.size()]);
    }
	
	@Override
    public Class<?>[] searchClasses() {
		loadAllClasses();
	    return classes.toArray(new Class<?>[classes.size()]);
    }

	@Override
    public Class<?>[] searchClasses(String basePackage) {
		Args.notEmpty(basePackage,"base package");

	    loadAllClasses();
	    
	    String basePackageEndsWithDot = null;
		
		if(basePackage.endsWith(".")){
			basePackageEndsWithDot  = basePackage;
			basePackage = basePackage.substring(0,basePackage.length()-1);
		}else{
			basePackageEndsWithDot = basePackage + ".";
		}
		
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>(16);
		
		for(Class<?> clazz : this.classes){
			String packageName = Classes.getPackageName(clazz);
			if(packageName.equals(basePackage) || packageName.startsWith(basePackageEndsWithDot) ){
				classes.add(clazz);
			}
		}
		
	    return classes.toArray(new Class<?>[classes.size()]);
    }

	@Override
    public Class<?>[] searchClasses(String basePackage, Predicate<Class<?>> predicate) {
		Args.notEmpty(basePackage,"base package");
		Args.notNull(predicate);
		
		loadAllClasses();
		
	    String basePackageEndsWithDot = null;
		
		if(basePackage.endsWith(".")){
			basePackageEndsWithDot  = basePackage;
			basePackage = basePackage.substring(0,basePackage.length()-1);
		}else{
			basePackageEndsWithDot = basePackage + ".";
		}
		
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>(16);
		
		for(Class<?> clazz : this.classes){
			String packageName = Classes.getPackageName(clazz);
			if((packageName.equals(basePackage) || packageName.startsWith(basePackageEndsWithDot)) && predicate.test(clazz)){
				classes.add(clazz);
			}
		}
		
	    return classes.toArray(new Class<?>[classes.size()]);
    }
	
	@Override
    public Class<?>[] searchClasses(Predicate<Class<?>> predicate) {
		Args.notNull(predicate);
		
		loadAllClasses();
		
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>(16);
		
		for(Class<?> clazz : this.classes){
			if(predicate.test(clazz)){
				classes.add(clazz);
			}
		}
		
	    return classes.toArray(new Class<?>[classes.size()]);
    }
	
	@Override
    public void process(Consumer<Resource> action) {
		Args.notNull(action);
		
		for(Resource r : values){
			action.accept(r);
		}
    }

	@Override
    public void processClasses(Consumer<Class<?>> action) {
		Args.notNull(action);
		
		loadAllClasses();
		
		for(Class<?> clazz : this.classes){
			action.accept(clazz);
		}
    }

	protected void loadAllClasses() {
		if(null == classes){
			classes = new LinkedHashSet<>();
			
	        for(int i=0;i<values.length;i++){
	        	Resource resource = values[i];
	        	
	        	if(null != resource.getClasspath() && Strings.endsWith(resource.getFilename(),Classes.CLASS_FILE_SUFFIX)){
	        		InputStream is  = null;
	        		try{
	        			is = resource.getInputStream();
	        			
	        			ClassReader classReader = new ClassReader(is);
	        			
	        			String internalClassName = classReader.getClassName();
	        			String className         = internalClassName.replace('/','.');
	        			
	        			classes.add(Class.forName(className, false, Classes.getClassLoader()));
	        		}catch(IOException e){
	        			throw new NestedIOException("Error loading .class file " + resource.getDescription() + "' : " + e.getMessage(),e);
	        		}catch(ClassNotFoundException e){
	        			throw new NestedClassNotFoundException(e.getMessage(), e);
	        		}finally{
	        			IO.close(is);
	        		}
	        	}
	        }
		}
	}
}