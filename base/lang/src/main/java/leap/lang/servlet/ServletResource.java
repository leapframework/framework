package leap.lang.servlet;

import javax.servlet.ServletContext;

import leap.lang.resource.ContextResource;

public interface ServletResource extends ContextResource {

	/**
	 * Return the ServletContext for this resource.
	 */
	ServletContext getServletContext();

	/**
	 * This implementation creates a {@link ServletResource}, applying the given path
	 * relative to the path of the underlying file of this resource descriptor.
	 */
	ServletResource createRelative(String relativePath);

	ServletResource[] scan(String subPattern);

}