/*
 * Copyright 2014 the original author or authors.
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
package leap.htpl.web;

import java.io.File;
import java.util.Locale;

import leap.htpl.HtplResource;
import leap.htpl.resolver.SimpleHtplResource;
import leap.lang.Locales;
import leap.lang.Strings;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.servlet.ServletResource;
import leap.lang.servlet.Servlets;

public class WebHtplResource extends SimpleHtplResource {

    private final String   prefix;
    private final String   suffix;
    private final Resource resource;
    private final ServletResource sr;

    public WebHtplResource(String prefix, String suffix, Resource resource, Locale locale) {
        super(resource, locale);
        this.prefix = Paths.suffixWithoutSlash(prefix);
        this.suffix = suffix;
        this.resource = resource;
        this.sr = resource instanceof ServletResource ? (ServletResource)resource : null;
    }

    @Override
    public ServletResource getResource() {
        return sr;
    }

    @Override
    public boolean isServletResource() {
        return null != sr;
    }

    @Override
    public File getFile() {
        return resource.getFile();
    }

    @Override
    public String getFileName() {
        return resource.getFilename();
    }

    @Override
    public HtplResource tryGetRelative(String relativePath, Locale locale, boolean ensureTemplate) {
        String[] paths;

        if (!Strings.isEmpty(Paths.getFileExtension(relativePath))) {
            paths = Locales.getLocaleFilePaths(locale, relativePath);
        } else {
            paths = Locales.getLocalePaths(locale, relativePath, suffix);
        }

        HtplResource r = null;

        for (String path : paths) {
            ServletResource rr = sr.createRelative(path);
            if (null != rr && rr.exists()) {
                r = new WebHtplResource(prefix, suffix, rr, locale);
                break;
            }
        }

        if (ensureTemplate && null != r && !r.getFileName().endsWith(suffix)) {
            return null;
        }
        return r;
    }

    @Override
    public HtplResource tryGetAbsolute(String absolutePath, Locale locale, boolean ensureTemplate) {
        absolutePath = Paths.prefixWithSlash(absolutePath);

        if (null != prefix) {
            absolutePath = prefix + absolutePath;
        }

        String[] paths;
        if (!Strings.isEmpty(Paths.getFileExtension(absolutePath))) {
            paths = Locales.getLocaleFilePaths(locale, absolutePath);
        } else {
            paths = Locales.getLocalePaths(locale, absolutePath, suffix);
        }

        HtplResource r = null;

        for (String path : paths) {
            ServletResource ar = Servlets.getResource(sr.getServletContext(), path);
            if (null != ar && ar.exists()) {
                r = new WebHtplResource(prefix, suffix, ar, locale);
                break;
            }
        }

        if (ensureTemplate && null != r && !r.getFileName().endsWith(suffix)) {
            return null;
        }
        return r;
    }

}
