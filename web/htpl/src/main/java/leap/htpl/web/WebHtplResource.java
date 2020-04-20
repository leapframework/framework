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
import leap.web.Utils;

import javax.servlet.ServletContext;

public class WebHtplResource extends SimpleHtplResource {

    private final ServletContext servletContext;
    private final String         prefix;
    private final String         suffix;

    public WebHtplResource(ServletContext servletContext, String prefix, String suffix, Resource resource, Locale locale) {
        super(resource, locale);
        this.servletContext = servletContext;
        this.prefix = Paths.suffixWithoutSlash(prefix);
        this.suffix = suffix;
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
            Resource rr = resource.createRelativeUnchecked(path);
            if (null != rr && rr.exists()) {
                r = new WebHtplResource(servletContext, prefix, suffix, rr, locale);
                break;
            }
        }

        if(isNotSureTemplate(r, ensureTemplate)) {
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
            Resource ar = Utils.getResource(servletContext, path);
            if (null != ar && ar.exists()) {
                r = new WebHtplResource(servletContext,prefix, suffix, ar, locale);
                break;
            }
        }

        if(isNotSureTemplate(r, ensureTemplate)) {
            return null;
        }

        return r;
    }

    protected boolean isNotSureTemplate(HtplResource r, boolean ensureTemplate) {
        if(!ensureTemplate) {
            return false;
        }
        if(null == r || null == r.getFileName()) {
            return true;
        }
        return !r.getFileName().endsWith(suffix);
    }

}
