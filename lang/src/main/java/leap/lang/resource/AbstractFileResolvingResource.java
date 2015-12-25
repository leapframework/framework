/*
 * Copyright 2002-2011 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import leap.lang.Exceptions;
import leap.lang.exception.NestedIOException;
import leap.lang.io.Files;
import leap.lang.net.Urls;


/**
 * Abstract base class for resources which resolve URLs into File references,
 * such as {@link UrlResource} or {@link ClassPathResource}.
 *
 * <p>Detects the "file" protocol as well as the JBoss "vfs" protocol in URLs,
 * resolving file system references accordingly.
 *
 * @author Juergen Hoeller
 */
public abstract class AbstractFileResolvingResource extends AbstractResource {

	/**
	 * This implementation returns a File reference for the underlying class path
	 * resource, provided that it refers to a file in the file system.
	 * @see bingo.lang.io.Resources.util.ResourceUtils#getFile(java.net.URL, String)
	 */
	@Override
	public File getFile() throws NestedIOException {
		try {
	        URL url = getURL();
	        if (url.getProtocol().startsWith(Urls.PROTOCOL_VFS)) {
	        	return VfsResourceDelegate.getResource(url).getFile();
	        }
	        return Files.fromUrl(url, getDescription());
        } catch (IOException e) {
        	throw Exceptions.wrap(e);
        }
	}

	/**
	 * This implementation determines the underlying File
	 * (or jar file, in case of a resource in a jar/zip).
	 */
	@Override
	protected File getFileForLastModifiedCheck() throws IOException {
		URL url = getURL();
		if (Urls.isJarUrl(url)) {
			URL actualUrl = Urls.getJarFileURL(url);
			if (actualUrl.getProtocol().startsWith(Urls.PROTOCOL_VFS)) {
				return VfsResourceDelegate.getResource(actualUrl).getFile();
			}
			return Files.fromUrl(actualUrl, "Jar URL");
		}
		else {
			return getFile();
		}
	}

	/**
	 * This implementation returns a File reference for the underlying class path
	 * resource, provided that it refers to a file in the file system.
	 */
	protected File getFile(URI uri) throws IOException {
		if (uri.getScheme().startsWith(Urls.PROTOCOL_VFS)) {
			return VfsResourceDelegate.getResource(uri).getFile();
		}
		return Files.fromUri(uri, getDescription());
	}


	@Override
	public boolean exists() {
		try {
			URL url = getURL();
			if (Urls.isFileUrl(url)) {
				// Proceed with file system resolution...
				return getFile().exists();
			}
			else {
				// Try a URL connection content-length header...
				URLConnection con = url.openConnection();
				Urls.setUseCachesIfNecessary(con);
				HttpURLConnection httpCon =
						(con instanceof HttpURLConnection ? (HttpURLConnection) con : null);
				if (httpCon != null) {
					httpCon.setRequestMethod("HEAD");
					int code = httpCon.getResponseCode();
					if (code == HttpURLConnection.HTTP_OK) {
						return true;
					}
					else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
						return false;
					}
				}
				if (con.getContentLength() >= 0) {
					return true;
				}
				if (httpCon != null) {
					// no HTTP OK status, and no content-length header: give up
					httpCon.disconnect();
					return false;
				}
				else {
					// Fall back to stream existence: can we open the stream?
					InputStream is = getInputStream();
					is.close();
					return true;
				}
			}
		}
		catch (IOException ex) {
			return false;
		}
	}

	@Override
	public boolean isReadable() {
		try {
			URL url = getURL();
			if (Urls.isFileUrl(url)) {
				// Proceed with file system resolution...
				File file = getFile();
				return (file.canRead() && !file.isDirectory());
			}
			else {
				return true;
			}
		}
		catch (IOException ex) {
			return false;
		}
	}

	@Override
	public long contentLength() throws IOException {
		URL url = getURL();
		if (Urls.isFileUrl(url)) {
			// Proceed with file system resolution...
			return getFile().length();
		}
		else {
			// Try a URL connection content-length header...
			URLConnection con = url.openConnection();
			Urls.setUseCachesIfNecessary(con);
			if (con instanceof HttpURLConnection) {
				((HttpURLConnection) con).setRequestMethod("HEAD");
			}
			return con.getContentLength();
		}
	}

	@Override
	public long lastModified() throws IOException {
		URL url = getURL();
		if (Urls.isFileUrl(url) || Urls.isJarUrl(url)) {
			// Proceed with file system resolution...
			return super.lastModified();
		}
		else {
			// Try a URL connection last-modified header...
			URLConnection con = url.openConnection();
			Urls.setUseCachesIfNecessary(con);
			if (con instanceof HttpURLConnection) {
				((HttpURLConnection) con).setRequestMethod("HEAD");
			}
			return con.getLastModified();
		}
	}

	/**
	 * Inner delegate class, avoiding a hard JBoss VFS API dependency at runtime.
	 */
	private static class VfsResourceDelegate {

		public static Resource getResource(URL url) throws IOException {
			return new VfsResource(VfsUtils.getRoot(url));
		}

		public static Resource getResource(URI uri) throws IOException {
			return new VfsResource(VfsUtils.getRoot(uri));
		}
	}
}
