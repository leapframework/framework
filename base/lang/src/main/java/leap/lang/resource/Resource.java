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

package leap.lang.resource;

import leap.lang.Charsets;
import leap.lang.exception.NestedIOException;
import leap.lang.io.InputStreamSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Interface for a resource descriptor that abstracts from the actual
 * type of underlying resource, such as a file or class path resource.
 *
 * <p>An InputStream can be opened for every resource if it exists in
 * physical form, but a URL or File handle can just be returned for
 * certain resources. The actual behavior is implementation-specific.
 *
 * @author Juergen Hoeller
 */
public interface Resource extends InputStreamSource {
	
	/**
	 * Returns a new {@link InputStreamReader} wraps the {@link InputStream} returned from {@link #getInputStream()} with default charset.
	 * 
	 * @throws NestedIOException if I/O error occured.
	 * 
	 * @see Charsets#defaultCharset()
	 */
	InputStreamReader getInputStreamReader() throws NestedIOException;
	
	/**
	 * Returns a new {@link InputStreamReader} wraps {@link InputStream} returned from {@link #getInputStream()} with the given charset.
	 * 
	 * @throws NestedIOException if I/O error occured.
	 */
	InputStreamReader getInputStreamReader(Charset charset) throws NestedIOException;
	
	/**
	 * Reads the content as string from the underlying {@link InputStream}.
	 */
	String getContent() throws NestedIOException;

	/**
	 * Return whether this resource actually exists in physical form.
	 * <p>This method performs a definitive existence check, whereas the
	 * existence of a <code>Resource</code> handle only guarantees a
	 * valid descriptor handle.
	 */
	boolean exists();

	/**
	 * Return whether the contents of this resource can be read,
	 * e.g. via {@link #getInputStream()} or {@link #getFile()}.
	 * <p>Will be <code>true</code> for typical resource descriptors;
	 * note that actual content reading may still fail when attempted.
	 * However, a value of <code>false</code> is a definitive indication
	 * that the resource content cannot be read.
	 * @see #getInputStream()
	 */
	boolean isReadable();

	/**
	 * Return whether this resource represents a handle with an open
	 * stream. If true, the InputStream cannot be read multiple times,
	 * and must be read and closed to avoid resource leaks.
	 * <p>Will be <code>false</code> for typical resource descriptors.
	 */
	boolean isOpen();
	
	/**
	 * Returns whether this resources is a {@link File}.
	 */
	boolean isFile();

    /**
     * Returns true if the resource is a directory.
     */
    default boolean isDirectory() {
        return isFile() && getFile().isDirectory();
    }

	/**
	 * Return a URL handle for this resource.
	 * @throws IOException if the resource cannot be resolved as URL,
	 * i.e. if the resource is not available as descriptor
	 */
	URL getURL() throws IOException;
	
	/**
	 * Returns the external form of the {@link URL} returned by calling {@link #getURL()}.
	 * 
	 * @throws IllegalStateException if error calling {@link #getURL()}
	 */
	String getURLString() throws IllegalStateException;

	/**
	 * Return a URI handle for this resource.
	 * @throws IOException if the resource cannot be resolved as URI,
	 * i.e. if the resource is not available as descriptor
	 */
	URI getURI() throws IOException;

	/**
	 * Return a File handle for this resource.
	 * @throws IllegalStateException if the resource cannot be resolved as absolute
	 * file path, i.e. if the resource is not available in a file system
	 */
	File getFile() throws NestedIOException,IllegalStateException;

	/**
	 * Determine the content length for this resource.
	 * @throws IOException if the resource cannot be resolved
	 * (in the file system or as some other known physical resource type)
	 */
	long contentLength() throws IOException;

	/**
	 * Determine the last-modified timestamp for this resource.
	 * @throws IOException if the resource cannot be resolved
	 * (in the file system or as some other known physical resource type)
	 */
	long lastModified() throws IOException;

	/**
	 * Create a resource relative to this resource.
	 * @param relativePath the relative path (relative to this resource)
	 * @return the resource handle for the relative resource
	 * @throws IOException if the relative resource cannot be determined
	 */
	Resource createRelative(String relativePath) throws IOException;
	
	/**
	 * Converts this resource to {@link SimpleFileResource} if this resource is a {@link File}.
	 * 
	 * <p>
	 * Returns itself if this resource is {@link SimpleFileResource}.
	 * 
	 * @throws IllegalStateException if not a {@link File}.
	 */
	FileResource toFileResource() throws IllegalStateException;
	
	/**
	 * Determine a filename for this resource, i.e. typically the last
	 * part of the path: for example, "myfile.txt".
	 * <p>Returns <code>null</code> if this type of resource does not
	 * have a filename.
	 */
	String getFilename();
	
	/**
	 * Returns the absolute path of the underlying {@link File}.
     *
     * <p/>
     * Returns {@link #getURLString()} if not a {@link File}.
	 */
	String getFilepath() throws IllegalStateException;

    /**
     * Returns true if the class path is exists.
     */
    default boolean hasClasspath() {
        return null != getClasspath();
    }
	
	/**
	 * Returns the class path of this resource.
	 * 
	 * <p/>
	 * 
	 * Returns <code>null</code> if this resource is not a classpath resource.
	 */
	String getClasspath();

	/**
	 * Return a description for this resource,
	 * to be used for error output when working with the resource.
	 * <p>Implementations are also encouraged to return this value
	 * from their <code>toString</code> method.
	 * @see java.lang.Object#toString()
	 */
	String getDescription();

	/**
	 * Return the path within context or file path, or full url.
	 */
	default String getPath() {
	    if(hasClasspath()) {
	        return getClasspath();
        }
        if(isFile()) {
            return getFilepath();
        }
        return getURLString();
	}
}
