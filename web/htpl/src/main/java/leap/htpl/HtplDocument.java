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

import leap.htpl.ast.Fragment;
import leap.htpl.ast.Node;
import leap.htpl.ast.NodeContainer;
import leap.lang.accessor.AttributeAccessor;
import leap.lang.accessor.PropertyAccessor;
import leap.lang.exception.ObjectExistsException;

import java.util.Locale;
import java.util.Map;

/**
 * Html (template) document object.
 */
public interface HtplDocument extends AttributeAccessor,PropertyAccessor {

    /**
     * Returns the underlying resource of template.
     */
	HtplResource getResource();

    /**
     * Returns the locale of current template or returns null if not specified.
     */
	Locale getLocale();

    /**
     * Returns this layout of current template or return null if not specified.
     */
	String getLayout();

    /**
     * Sets the layout.
     */
	void setLayout(String layout);

    /**
     * Returns the title of template.
     */
	String getTitle();

    /**
     * sets the title of template.
     */
	void setTitle(String title);

    /**
     * Returns a {@link NodeContainer} contains all the nodes in this document.
     */
    NodeContainer nodes();

    /**
     * Returns all the attribute objects.
     *
     * <p/>
     * The attributes can't be accessed in template.
     */
    Map<String, Object> getAttributes();

    /**
     * Returns a {@link Map} contains all properties of the template.
     *
     * <p/>
     * The properties can be accessed in template.
     */
	Map<String, String> getProperties();

    /**
     * Puts the map as properties.
     */
	void putProperties(Map<String, String> properties);

    /**
     * Returns all the {@link Fragment} in template.
     */
	Map<String, Fragment> getFragments();

    /**
     * Returns the {@link Fragment} or null if not exists.
     */
	Fragment getFragment(String name);

    /**
     * Adds a {@link Fragment}.
     */
	void addFragment(String name,Fragment fragment) throws ObjectExistsException;

    /**
     * Adds all the {@link Fragment}.
     */
	void addFragments(Map<String,Fragment> m);

    /**
     * Removes a {@link Fragment}.
     */
	Fragment removeFragment(String name);

    /**
     * Adds an included template.
     */
	void addIncludedTemplate(String name, HtplTemplate tpl);

    /**
     * Removes an included template.
     */
	HtplTemplate removeIncludedTemplate(String name);

    /**
     * Sets all the included templates.
     */
	void putIncludedTemplates(Map<String, HtplTemplate> m);

    /**
     * Returns all the included templates.
     */
	Map<String,HtplTemplate> getIncludedTemplates();

    /**
     * Returns the required header nodes.
     */
	Node[] getRequiredHeaders();
	
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