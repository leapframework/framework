/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.spring.boot;

import leap.lang.exception.NestedIOException;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class SpringResource extends AbstractResource implements Resource {

    private final leap.lang.resource.Resource wrapped;

    public SpringResource(leap.lang.resource.Resource wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean exists() {
        return wrapped.exists();
    }

    @Override
    public boolean isReadable() {
        return wrapped.isReadable();
    }

    @Override
    public boolean isOpen() {
        return wrapped.isOpen();
    }

    @Override
    public URL getURL() throws IOException {
        return wrapped.getURL();
    }

    @Override
    public URI getURI() throws IOException {
        return wrapped.getURI();
    }

    @Override
    public File getFile() throws IOException {
        try{
            File file = wrapped.getFile();
            if(null == file) {
                throw new FileNotFoundException("Resource '" + getDescription() + "' is not a file");
            }
            return file;
        }catch (NestedIOException e) {
            throw e.getIOException();
        }
    }

    @Override
    public long contentLength() throws IOException {
        if(wrapped.isFile()) {
            return getFile().length();
        }else {
            return super.contentLength();
        }
    }

    @Override
    public long lastModified() throws IOException {
        return super.lastModified();
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        return new SpringResource(wrapped.createRelative(relativePath));
    }

    @Override
    public String getFilename() {
        return wrapped.getFilename();
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return wrapped.getInputStream();
    }
}
