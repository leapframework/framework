/*
 * Copyright 2002-2012 the original author or authors.
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

package leap.lang.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import leap.lang.Args;
import leap.lang.Exceptions;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.net.Urls;
import leap.lang.path.PathMatcher;
import leap.lang.path.Paths;
import leap.lang.resource.AbstractFileResolvingResource;
import leap.lang.resource.ContextResource;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;

/**
 * {@link Resource} implementation for
 * {@link javax.servlet.ServletContext} resources, interpreting
 * relative paths within the web application root directory.
 *
 * <p>Always supports stream access and URL access, but only allows
 * {@code java.io.File} access when the web application archive
 * is expanded.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see javax.servlet.ServletContext#getResourceAsStream
 * @see javax.servlet.ServletContext#getResource
 * @see javax.servlet.ServletContext#getRealPath
 */
public class SimpleServletResource extends AbstractFileResolvingResource implements ContextResource, ServletResource {

	private static final Log log = LogFactory.get(SimpleServletResource.class);

	private final ServletContext servletContext;

	private final String path;

	/**
	 * Create a new ServletContextResource.
	 * <p>The Servlet spec requires that resource paths start with a slash,
	 * even if many containers accept paths without leading slash too.
	 * Consequently, the given path will be prepended with a slash if it
	 * doesn't already start with one.
	 * @param servletContext the ServletContext to load from
	 * @param path the path of the resource
	 */
	public SimpleServletResource(ServletContext servletContext, String path) {
		// check ServletContext
		Args.notNull(servletContext, "servletContext");
		this.servletContext = servletContext;

		// check path
		Args.notNull(path, "path");
		String pathToUse = Paths.normalize(path);
		if (!pathToUse.startsWith("/")) {
			pathToUse = "/" + pathToUse;
		}
		this.path = pathToUse;
	}

	/**
	 * Return the ServletContext for this resource.
	 */
	@Override
    public final ServletContext getServletContext() {
		return this.servletContext;
	}

	/**
	 * Return the path for this resource.
	 */
	public final String getPath() {
		return this.path;
	}


	/**
	 * This implementation checks {@code ServletContext.getResource}.
	 * @see javax.servlet.ServletContext#getResource(String)
	 */
	@Override
	public boolean exists() {
		try {
			URL url = this.servletContext.getResource(this.path);
			return (url != null);
		}
		catch (Exception ex) {
			log.info("Error calling ServletContext.getResource on path : " + path, ex);
			return false;
		}
	}

	/**
	 * This implementation delegates to {@code ServletContext.getResourceAsStream},
	 * which returns {@code null} in case of a non-readable resource (e.g. a directory).
	 * @see javax.servlet.ServletContext#getResourceAsStream(String)
	 */
	@Override
	public boolean isReadable() {
		InputStream is = this.servletContext.getResourceAsStream(this.path);
		if (is != null) {
			try {
				is.close();
			}
			catch (IOException ex) {
				// ignore
			}
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * This implementation delegates to {@code ServletContext.getResourceAsStream},
	 * but throws a FileNotFoundException if no resource found.
	 * @see javax.servlet.ServletContext#getResourceAsStream(String)
	 */
	public InputStream getInputStream() throws IOException {
		InputStream is = this.servletContext.getResourceAsStream(this.path);
		if (is == null) {
			throw new FileNotFoundException("Could not open " + getDescription());
		}
		return is;
	}

	/**
	 * This implementation delegates to {@code ServletContext.getResource},
	 * but throws a FileNotFoundException if no resource found.
	 * @see javax.servlet.ServletContext#getResource(String)
	 */
	@Override
	public URL getURL() throws IOException {
		URL url = this.servletContext.getResource(this.path);
		if (url == null) {
			throw new FileNotFoundException(
					getDescription() + " cannot be resolved to URL because it does not exist");
		}
		return url;
	}

	/**
	 * This implementation resolves "file:" URLs or alternatively delegates to
	 * {@code ServletContext.getRealPath}, throwing a FileNotFoundException
	 * if not found or not resolvable.
	 * @see javax.servlet.ServletContext#getResource(String)
	 * @see javax.servlet.ServletContext#getRealPath(String)
	 */
	@Override
	public File getFile() {
		try {
	        URL url = this.servletContext.getResource(this.path);
	        if (url != null && Urls.isFileUrl(url)) {
	        	// Proceed with file system resolution...
	        	return super.getFile();
	        }
	        else if(url != null && Urls.isJarUrl(url)){
	        	return new File(url.getFile());
	        }
	        else {
	        	String realPath = Servlets.getRealPath(this.servletContext, this.path);
	        	return new File(realPath);
	        }
        } catch (IOException e) {
        	throw Exceptions.wrap(e);
        }
	}

	/**
	 * This implementation creates a ServletContextResource, applying the given path
	 * relative to the path of the underlying file of this resource descriptor.
	 * @see org.springframework.util.StringUtils#applyRelativePath(String, String)
	 */
    @Override
	public SimpleServletResource createRelative(String relativePath) {
		String pathToUse = Paths.applyRelative(this.path, relativePath);
		return new SimpleServletResource(this.servletContext, pathToUse);
	}
	
	@Override
    @SuppressWarnings("unchecked")
    public ServletResource[] scan(String subPattern) {
		Args.notEmpty(subPattern,"subPattern");
		
		Set<String> subPaths = this.servletContext.getResourcePaths(this.path);
		
		List<SimpleServletResource> resources = new ArrayList<>();
		
		PathMatcher matcher = Resources.getPathMatcher();
		
		for(String subPath : subPaths){
			if(matcher.match(subPattern, subPath)){
				resources.add(new SimpleServletResource(servletContext, subPath));
			}
		}
		
		return resources.toArray(new SimpleServletResource[resources.size()]);
	}

	/**
	 * This implementation returns the name of the file that this ServletContext
	 * resource refers to.
	 * @see org.springframework.util.StringUtils#getFilename(String)
	 */
	@Override
	public String getFilename() {
		return Paths.getFileName(this.path);
	}

	/**
	 * This implementation returns a description that includes the ServletContext
	 * resource location.
	 */
	public String getDescription() {
		return "ServletContext resource [" + this.path + "]";
	}

	public String getPathWithinContext() {
		return this.path;
	}

	/**
	 * This implementation compares the underlying ServletContext resource locations.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof SimpleServletResource) {
			SimpleServletResource otherRes = (SimpleServletResource) obj;
			return (this.servletContext.equals(otherRes.servletContext) && this.path.equals(otherRes.path));
		}
		return false;
	}

	/**
	 * This implementation returns the hash code of the underlying
	 * ServletContext resource location.
	 */
	@Override
	public int hashCode() {
		return this.path.hashCode();
	}

}
