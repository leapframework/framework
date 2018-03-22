/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.spring.boot;

import leap.lang.Exceptions;
import leap.lang.resource.AbstractResource;
import leap.lang.resource.Resource;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class LeapResource extends AbstractResource implements Resource {

    private final org.springframework.core.io.Resource wrapped;

    public LeapResource(org.springframework.core.io.Resource wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean exists() {
        return wrapped.exists();
    }

    @Override
    public long contentLength() throws IOException {
        return wrapped.contentLength();
    }

    @Override
    public long lastModified() throws IOException {
        return wrapped.lastModified();
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
    public File getFile() {
        try {
            return wrapped.getFile();
        }catch (IOException e) {
            throw Exceptions.wrap(e);
        }
    }

    @Override
    public String getFilename() {
        return wrapped.getFilename();
    }

    @Override
    public URL getURL() throws IOException {
        return wrapped.getURL();
    }

    @Override
    public String getClasspath() {
        if(wrapped instanceof ClassPathResource) {
            return ((ClassPathResource) wrapped).getPath();
        }else{
            return null;
        }
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        return new LeapResource(wrapped.createRelative(relativePath));
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return wrapped.getInputStream();
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }
}
