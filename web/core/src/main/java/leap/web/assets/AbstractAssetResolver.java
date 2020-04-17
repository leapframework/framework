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
package leap.web.assets;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;

import java.util.Locale;

public abstract class AbstractAssetResolver implements AssetResolver {

    protected @Inject AssetManager manager;
    protected @Inject AssetConfig  config;

    @Override
    public Asset resolveAsset(String path, Locale locale) throws Throwable {
        return resolveAsset(path, locale, null);
    }

    @Override
    public Asset resolveAsset(String path, Locale locale, Resource dir) throws Throwable {
        path = Paths.prefixWithoutSlash(path);
        if(isExcluded(path)) {
            return null;
        }

        Resource resource = null;
        if (null != dir) {
            resource = Resources.getResource(dir, Strings.trimStart(path, '/'));
        }

        if (null == resource || !resource.exists()) {
            final String resourcePath = getResourcePath(path);

            resource = getLocaleResource(resourcePath,locale);

            if(null == resource || !resource.exists()){
                return null;
            }
        }

        return resolveAsset(path, resource);
    }

    protected abstract Resource resolveResource(String path);

    protected abstract Asset resolveAsset(String path, Resource resource);

    protected String getResourcePath(String path) {
        return path;
    }

    protected boolean isExcluded(String path) {
        return false;
    }

    protected Resource getLocaleResource(String resourcePath, Locale locale){
        String suffix     = "." + Paths.getFileExtension(resourcePath);
        String pathPrefix = resourcePath.substring(0,resourcePath.length() - suffix.length());

        String lang    = null == locale ? null : locale.getLanguage();
        String country = null == locale ? null : locale.getCountry();

        //{pathPrefix}_{lang}_{COUNTRY}{suffix}
        if(!Strings.isEmpty(country)){
            String path = pathPrefix + "_" + locale.getLanguage() + "_" + country + suffix;
            Resource resource = resolveResource(path);
            if(null != resource && resource.exists()){
                return resource;
            }
        }

        //{pathPrefix_{lang}{suffix}
        if(!Strings.isEmpty(lang)){
            String path = pathPrefix + "_" + locale.getLanguage() + suffix;
            Resource resource = resolveResource(path);
            if(null != resource && resource.exists()){
                return resource;
            }
        }

        //{pathPrefix}{suffix}
        String path = pathPrefix + suffix;
        Resource resource = resolveResource(path);
        if(null != resource && resource.exists()){
            return resource;
        }

        return null;
    }
}
