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

import leap.core.BeanFactory;
import leap.htpl.ast.Attr;
import leap.htpl.ast.Element;
import leap.htpl.escaping.EscapeType;
import leap.htpl.escaping.HtplEscaper;
import leap.htpl.exception.HtplParseException;
import leap.lang.exception.NestedIOException;
import leap.lang.resource.Resource;
import leap.web.assets.AssetManager;
import leap.web.assets.AssetSource;

import java.util.Locale;

public interface HtplEngine {
	
	/**
	 * Returns the {@link BeanFactory}.
	 */
	BeanFactory factory();

	/**
	 * Returns the config object.
	 */
	HtplConfig getConfig();
	
	/**
	 * Optional. Returns the {@link AssetSource}.
	 */
	AssetSource getAssetSource();
	
	/**
	 * Optional. Returns the {@link AssetManager}.
	 */
	AssetManager getAssetManager();
	
	/**
	 * Returns the {@link HtplExpressionManager}.
	 */
	HtplExpressionManager getExpressionManager();	
	
	/**
	 * Returns the {@link HtplEscaper} for the given escaping mode.
	 */
	HtplEscaper getEscaper(EscapeType mode);
	
	/**
	 * Parse the template from the given resource.
	 */
	HtplDocument parseDocument(HtplResource resource) throws HtplParseException,NestedIOException;
	
	/**
	 * Creates a new {@link HtplCompiler} for compiling {@link HtplDocument}.
	 */
	HtplCompiler createCompiler();
	
	boolean resolveElementProcessor(Element e);
	
	boolean resolveAttrProcessor(Element e,Attr a);
	
	HtplTemplate resolveTemplate(String templateName) throws HtplParseException,NestedIOException;
	
	HtplTemplate resolveTemplate(String templateName,Locale locale) throws HtplParseException,NestedIOException;

    HtplTemplate resolveTemplate(HtplResource current,String templateName,Locale locale) throws HtplParseException,NestedIOException;

    HtplTemplate createTemplate(HtplResource resource) throws HtplParseException,NestedIOException;

    HtplTemplate createTemplate(HtplResource resource, String templateName) throws HtplParseException,NestedIOException;
	
	HtplTemplate createTemplate(Resource resource) throws HtplParseException,NestedIOException;
	
	HtplTemplate createTemplate(Resource resource, Locale locale) throws HtplParseException,NestedIOException;
	
	HtplTemplate createTemplate(String content) throws HtplParseException;
	
	HtplTemplate createTemplate(String content, Locale locale) throws HtplParseException;
	
	/**
	 * Adds a managed template into this engine.
	 */
	void addTemplate(String name,HtplTemplate template);
	
	/**
	 * Adds a managed template into this engine.
	 */
	default void addTemplate(String name,String content) {
		addTemplate(name, createTemplate(content));
	}
	
	/**
	 * Adds a managed template into this engine. 
	 */
	default void addTemplate(String name,String content,Locale locale) {
		addTemplate(name, createTemplate(content, locale));
	}

	/**
	 * Adds a managed template into this engine. 
	 */
	default void addTemplate(String name, Resource resource) {
		addTemplate(name, createTemplate(resource));
	}
	
	/**
	 * Adds a managed template into this engine. 
	 */
	default void addTemplate(String name, Resource resource, Locale locale) {
		addTemplate(name, createTemplate(resource, locale));
	}
	
	/**
	 * Adds a managed template into this engine. 
	 */
	default void addTemplate(String name, HtplResource resource) {
		addTemplate(name, createTemplate(resource));
	}
	
	/**
	 * Removes a managed template from this engine.
	 * 
	 * <p>
	 * Returns the removed template or <code>null</code> if the template name does not exists.
	 */
	default HtplTemplate removeTemplate(String name) {
		return removeTempalte(name, null);
	}
	
	/**
	 * Removes a managed template from this engine.
	 * 
	 * <p>
	 * Returns the removed template or <code>null</code> if the template name with the given locale does not exists.
	 */
	HtplTemplate removeTempalte(String name,Locale locale);
}