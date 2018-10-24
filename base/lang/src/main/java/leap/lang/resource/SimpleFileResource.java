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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import leap.lang.Args;
import leap.lang.path.Paths;

class SimpleFileResource extends AbstractResource implements FileResource {

	private final File file;

	private final String path;
	
	private final String path1;
	
	private final String classpath;

	public SimpleFileResource(File file) {
		this(file,null);
	}
	
	public SimpleFileResource(File file,String classpath) {
		Args.notNull(file, "file");
		this.file = file;
		this.path = Paths.normalize(file.getPath());
		this.path1 = file.isDirectory() ? path + "/" : path;
		this.classpath = classpath;
	}	

	public SimpleFileResource(String path) {
		this(path,null);
	}
	
	public SimpleFileResource(String path,String classpath) {
		Args.notNull(path, "path");
		this.file = new File(path);
		this.path = Paths.normalize(path);
		this.path1 = file.isDirectory() && !path.endsWith("/") ? path + "/" : path;
		this.classpath = classpath;
	}

	public final String getPath() {
		return this.path;
	}

	@Override
	public boolean exists() {
		return this.file.exists();
	}

	@Override
	public boolean isReadable() {
		return (this.file.canRead() && !this.file.isDirectory());
	}
	
	@Override
    public boolean isFile() {
		return true;
    }

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(this.file);
	}

	@Override
	public URL getURL() throws IOException {
		return this.file.toURI().toURL();
	}

	@Override
	public URI getURI() throws IOException {
		return this.file.toURI();
	}

	@Override
	public File getFile() {
		return this.file;
	}

    @Override
	public long contentLength() throws IOException {
		return this.file.length();
	}

    @Override
	public SimpleFileResource createRelative(String relativePath) {
		String pathToUse = Paths.normalize(Paths.applyRelative(this.path1, relativePath));
		return new SimpleFileResource(pathToUse);
	}

	@Override
	public String getFilename() {
		return this.file.getName();
	}

	@Override
    public String getClasspath() {
	    return classpath;
    }

	public String getDescription() {
		return "file [" + this.file.getAbsolutePath() + "]";
	}

	public boolean isWritable() {
		return (this.file.canWrite() && !this.file.isDirectory());
	}

	public OutputStream getOutputStream() throws IOException {
		return new FileOutputStream(this.file);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj == this ||
		    (obj instanceof SimpleFileResource && this.path.equals(((SimpleFileResource) obj).path)));
	}

	@Override
	public int hashCode() {
		return this.path.hashCode();
	}

    @Override
    public String toString() {
        if(null != classpath && classpath.length() > 0) {
            return "classpath:" + classpath;
        }else{
            return getDescription();
        }
    }
	
	
}
