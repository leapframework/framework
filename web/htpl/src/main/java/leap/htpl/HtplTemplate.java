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

import leap.lang.Sourced;

import java.io.Writer;
import java.util.Locale;

public interface HtplTemplate extends Sourced {

    /**
     * Returns the name of template or returns null if no name.
     */
    String getName();
	
	/**
	 * Returns {@link HtplEngine engine} creates this template.
	 */
	HtplEngine getEngine();
	
	/**
	 * Returns the resource contains the content of this template.
	 */
	HtplResource getResource();
	
	/**
	 * Returns the locale of this template.
	 */
	Locale getLocale();
	
	/**
	 * Returns the {@link HtplDocument document} of this template.
	 */
	HtplDocument getDocument();
	
	/**
	 * Returns <code>true</code> if this template is reloadable.
	 */
	boolean reloadable();
	
	/**
	 * Creates a new {@link HtplPage} object for rendering this template.
	 */
	HtplPage createPage();
	
	/**
	 * Renders this template and writes the content to the given writer.
	 */
	void render(HtplContext context,Writer writer);
	
	/**
	 * Renders this template and writes the content to the given writer.
	 */
	void render(HtplContext context,HtplWriter writer);
	
	/**
	 * Renders this template as a child included by the parent template.
	 */
	void render(HtplTemplate parent,HtplContext context,HtplWriter writer);

    /**
     * Adds a listener.
     */
    void addListener(HtplTemplateListener listener);

    /**
     * Returns true if the listener exists.
     */
    boolean containsListener(HtplTemplateListener listener);

    /**
     * Removes the listener.
     *
     * <p/>
     * Returns true if the listener exists.
     */
    boolean removeListener(HtplTemplateListener listener);

    /**
     * Adds an interceptor.
     */
    void addInterceptor(HtplTemplateInterceptor interceptor);

    /**
     * Returns true if the interceptor exists.
     */
    boolean containsInterceptor(HtplTemplateInterceptor interceptor);

    /**
     * Removes the interceptor.
     *
     * <p/>
     * Returns true if the interceptor exists.
     */
    boolean removeInterceptor(HtplTemplateInterceptor interceptor);
}