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

import leap.lang.Args;
import leap.lang.Exceptions;
import leap.lang.net.NET;
import leap.lang.net.Urls;
import leap.lang.path.Paths;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

public class UrlResource extends AbstractFileResolvingResource {

	private final URL url;
	private final URL cleanedUrl;
	private final URI uri;
	private final String classpath;
    private String classpathPrefix;

    public UrlResource(URL url) {
        this(url, null);
    }

	public UrlResource(URL url, String classpathPrefix) {
		Args.notNull(url, "url");
		this.url = url;
		this.cleanedUrl = getCleanedUrl(this.url, url.toString());
		this.uri = null;
        this.classpathPrefix = classpathPrefix;
		this.classpath = determinateClasspath();
	}

	public UrlResource(URI uri) throws MalformedURLException {
		Args.notNull(uri, "uri");
		this.url = uri.toURL();
		this.cleanedUrl = getCleanedUrl(this.url, uri.toString());
		this.uri = uri;
		this.classpath = determinateClasspath();
	}

	public UrlResource(String path) throws MalformedURLException {
		Args.notNull(path, "path");
		this.url = new URL(path);
		this.cleanedUrl = getCleanedUrl(this.url, path);
		this.uri = null;
		this.classpath = determinateClasspath();
	}
	
	private String determinateClasspath(){
		if(Urls.isJarUrl(url)){
			String urlString = url.toString();
			int separatorIndex = urlString.lastIndexOf(Urls.JAR_URL_SEPARATOR);
			if (separatorIndex != -1) {
				return urlString.substring(separatorIndex+2);
			}
		}else if(null != classpathPrefix){
            if(Urls.isFileUrl(url)) {
                String urlString = url.toString();
                int separatorIndex = urlString.indexOf(classpathPrefix);
                if (separatorIndex != -1) {
                    return urlString.substring(separatorIndex);
                }
            }
        }
		return null;
	}

	private URL getCleanedUrl(URL originalUrl, String originalPath) {
		try {
			return new URL(Paths.normalize(originalPath));
		}
		catch (MalformedURLException ex) {
			// Cleaned URL path cannot be converted to URL
			// -> take original URL.
			return originalUrl;
		}
	}

	public InputStream getInputStream() throws IOException {
		URLConnection con = this.url.openConnection();
		NET.useCachesIfNecessary(con);
		try {
			return con.getInputStream();
		}
		catch (IOException ex) {
			// Close the HTTP connection (if applicable).
			if (con instanceof HttpURLConnection) {
				((HttpURLConnection) con).disconnect();
			}
			throw ex;
		}
	}

	@Override
	public URL getURL() throws IOException {
		return this.url;
	}

	@Override
	public URI getURI() throws IOException {
		if (this.uri != null) {
			return this.uri;
		}
		else {
			return super.getURI();
		}
	}

	@Override
	public File getFile() {
		if (this.uri != null) {
			try {
	            return super.getFile(this.uri);
            } catch (IOException e) {
            	throw Exceptions.wrap(e);
            }
		}
		else {
			return super.getFile();
		}
	}

	@Override
	public Resource createRelative(String relativePath) throws MalformedURLException {
		if (relativePath.startsWith("/")) {
			relativePath = relativePath.substring(1);
		}
		return new UrlResource(new URL(this.url, relativePath),classpathPrefix);
	}

	@Override
	public String getFilename() {
		return new File(this.url.getFile()).getName();
	}
	
	@Override
    public String getClasspath() {
		return classpath;
	}

	public String getDescription() {
		return "URL [" + this.url + "]";
	}

	@Override
	public boolean equals(Object obj) {
		return (obj == this ||
		    (obj instanceof UrlResource && this.cleanedUrl.equals(((UrlResource) obj).cleanedUrl)));
	}

	@Override
	public int hashCode() {
		return this.cleanedUrl.hashCode();
	}
}
