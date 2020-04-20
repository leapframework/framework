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
package leap.htpl.resolver;

import leap.htpl.HtplResource;
import leap.lang.Args;
import leap.lang.Locales;
import leap.lang.Out;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;

import java.io.*;
import java.util.Locale;

public class SimpleHtplResource implements HtplResource {
    private static final Log log = LogFactory.get(SimpleHtplResource.class);

    protected final Resource resource;
    protected final Locale   locale;
    protected final String   source;
    protected final File     file;

    private long lastModified = 0L;

    public SimpleHtplResource(Resource resource, Locale locale) {
        Args.notNull(resource, "resource");
        this.resource = resource;
        this.locale = locale;
        this.source = resource.getPath();
        this.file = resource.isFile() ? resource.getFile() : null;
        if (null != file) {
            this.lastModified = file.lastModified();
        }
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public Reader getReader() throws IOException {
        return resource.getInputStreamReader();
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public String getFileName() {
        if(null != file) {
            return file.getName();
        }else {
            return resource.getFilename();
        }
    }

    @Override
    public boolean reloadable() {
        return null != file;
    }

    @Override
    public boolean reload(Out<Reader> out) throws IOException {
        if (null != file) {
            long lastModified = file.lastModified();
            if (lastModified != this.lastModified) {

                //TODO : handles file not found.

                if (!file.exists()) {
                    return false;
                }

                try {
                    this.lastModified = lastModified;
                    out.set(new InputStreamReader(new FileInputStream(file)));
                    return true;
                } catch (FileNotFoundException e) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public HtplResource tryGetRelative(String relativePath, Locale locale) {
        if (null != file) {
            String[] paths = null == locale ? new String[]{relativePath} : Locales.getLocalePaths(locale, relativePath);

            for (String path : paths) {
                try {
                    Resource rr = resource.createRelative(path);
                    if (null != rr && rr.exists()) {
                        return new SimpleHtplResource(rr, locale);
                    }
                } catch (IOException e) {
                    log.info("Error creating relative resource '" + relativePath + "', " + e.getMessage(), e);
                }
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return resource.toString();
    }
}
