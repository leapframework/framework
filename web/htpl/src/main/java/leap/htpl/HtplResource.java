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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Locale;

import leap.lang.Out;
import leap.lang.Sourced;
import leap.lang.Strings;
import leap.lang.resource.Resource;
import leap.lang.servlet.ServletResource;

public interface HtplResource extends Sourced {
	
	Locale getLocale();
	
	Reader getReader() throws IOException;
	
	/**
	 * Returns the {@link File} object if this resource is a {@link File} or <code>null</code>.
	 */
	default File getFile() {
		return null;
	}
	
	/**
	 * Returns the resource's file name or <code>null</code> if this resource is not a file.
	 */
	default String getFileName() {
		return null;
	}
	
	boolean reloadable();

	boolean reload(Out<Reader> out) throws IOException;
	
	default HtplResource tryGetResource(String path, Locale locale) {
		return tryGetResource(path, locale, false);
	}
	
	default HtplResource tryGetResource(String path, Locale locale, boolean ensureTemplate) {
		if(Strings.startsWith(path, "/")){
			return tryGetAbsolute(path, locale, ensureTemplate);
		}else{
			return tryGetRelative(path, locale, ensureTemplate);
		}
	}
	
	/**
	 * Returns a htpl resource of the path relative to this resource.
	 * 
	 * <p>
	 * Returns <code>null</code> if this resource does not supports this operation or the given path does not exists.
	 */
	default HtplResource tryGetRelative(String relativePath,Locale locale) {
		return tryGetRelative(relativePath, locale, false);
	}

	/**
	 * Returns a htpl resource of the absolute path.
	 * 
	 * <p>
	 * Returns <code>null</code> if this resource does not supports this operation or the given path does not exists.
	 */
	default HtplResource tryGetAbsolute(String absolutePath,Locale locale) {
		return tryGetAbsolute(absolutePath, locale, false);
	}
	
	/**
	 * Returns a htpl resource of the path relative to this resource.
	 * 
	 * <p>
	 * Returns <code>null</code> if this resource does not supports this operation or the given path does not exists.
	 */
	default HtplResource tryGetRelative(String relativePath,Locale locale, boolean ensureTemplate) {
		return null;
	}

	/**
	 * Returns a htpl resource of the absolute path.
	 * 
	 * <p>
	 * Returns <code>null</code> if this resource does not supports this operation or the given path does not exists.
	 */
	default HtplResource tryGetAbsolute(String absolutePath,Locale locale, boolean ensureTempalte) {
		return null;
	}
	
	default boolean isServletResource() {
		return getServletResource() instanceof ServletResource;
	}

	default boolean isResource() {
		return null != getServletResource();
	}
	
	default Resource getServletResource() {
		return null;
	}
}