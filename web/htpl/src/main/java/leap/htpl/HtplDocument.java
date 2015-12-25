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
package leap.htpl;

import java.util.Locale;
import java.util.Map;

import leap.htpl.ast.Fragment;
import leap.htpl.ast.Node;
import leap.htpl.ast.NodeContainer;
import leap.lang.accessor.AttributeAccessor;
import leap.lang.accessor.PropertyAccessor;
import leap.lang.exception.ObjectExistsException;

public interface HtplDocument extends AttributeAccessor,PropertyAccessor {
	
	HtplResource getResource();
	
	Locale getLocale();

	String getLayout();
	
	void setLayout(String layout);
	
	String getTitle();
	
	void setTitle(String title);

	Map<String, String> getProperties();

	void putProperties(Map<String, String> properties);

	Map<String, Object> getAttributes();

	Map<String, Fragment> getFragments();

	Fragment getFragment(String name);
	
	void addFragment(String name,Fragment fragment) throws ObjectExistsException;
	
	void addFragments(Map<String,Fragment> m);
	
	Fragment removeFragment(String name);
	
	void addIncludedTemplate(String name, HtplTemplate tpl);
	
	HtplTemplate removeIncludedTemplate(String name);
	
	void putIncludedTemplates(Map<String, HtplTemplate> m);

	Map<String,HtplTemplate> getIncludedTemplates();
	
	Node[] getRequiredHeaders();
	
	/**
	 * Returns a {@link NodeContainer} contains all the root nodes in this document.
	 */
	NodeContainer nodes();
	
	/**
	 * Deep clones this document.
	 */
	HtplDocument deepClone();

	/**
	 * Locks this document, a locked document cannot be modified.
	 * 
	 * @throws IllegalStateException if this document is locked.
	 */
	void lock() throws IllegalStateException;
	
	/**
	 * Returns a boolean value indicates this document is locked or not.
	 */
	boolean isLocked();
	
	/**
	 * Returns the document itself.
	 * 
	 * @throws IllegalStateException if this document is locked.
	 */
	HtplDocument process() throws IllegalStateException;

	/**
	 * Compiles this docuemnt use the compiler returned by {@link HtplEngine#createCompiler()}.
	 */
	HtplCompiled compile();
	
	/**
	 * Compiles this document and returns a {@link HtplCompiled} object.
	 */
	HtplCompiled compile(HtplCompiler compiler);
}