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

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ioc.PostCreateBean;
import leap.lang.Collections2;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.web.Utils;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServletAssetResolver extends AbstractAssetResolver implements PostCreateBean {

    protected static final Log log = LogFactory.get(ServletAssetResolver.class);

    protected @Inject ServletContext servletContext;

    protected String prefix;

    private List<String> excludedPaths;

    private final Map<String, ServletAsset> cache = new ConcurrentHashMap<String, ServletAsset>();

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths;
    }

    @Override
    public void postCreate(BeanFactory beanFactory) throws Throwable {
        if (null == prefix) {
            prefix = config.getPublicDirectory();
        }
        if (Collections2.isNotEmpty(excludedPaths)) {
            for (int i = 0; i < excludedPaths.size(); i++) {
                String excludedPath = excludedPaths.get(i);
                excludedPaths.set(i, Paths.prefixWithoutSlash(excludedPath));
            }
        }
    }

    @Override
    protected String getResourcePath(String path) {
        return getResourcePath(prefix, path);
    }

    protected String getResourcePath(String prefix, String path) {
        path = Paths.prefixWithoutSlash(path);
        if (Strings.isEmpty(prefix) || path.startsWith(prefix + "/")) {
            return path;
        } else {
            return Paths.suffixWithSlash(prefix) + path;
        }
    }

    @Override
    protected Resource resolveResource(String path) {
        return Utils.getResource(servletContext, path);
    }

    @Override
    protected Asset resolveAsset(String path, Resource resource) {
        ServletAsset asset = cache.get(resource.getPath());
        if (null != asset && asset.isExpired()) {
            cache.remove(resource.getPath());
            asset = null;
        }

        if (null == asset) {
            asset = new ServletAsset(manager, path, resource);
            cache.put(resource.getPath(), asset);
        }
        return asset;
    }

    protected boolean isExcluded(String path) {
        if (Collections2.isEmpty(excludedPaths)) {
            return false;
        }
        for (String excludedPath : excludedPaths) {
            if (path.startsWith(excludedPath)) {
                return true;
            }
        }
        return false;
    }
}