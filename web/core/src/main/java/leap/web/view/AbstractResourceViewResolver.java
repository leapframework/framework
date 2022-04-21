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

package leap.web.view;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ioc.PostCreateBean;
import leap.lang.Locales;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import java.util.Locale;

public abstract class AbstractResourceViewResolver<R extends Resource>
        extends AbstractViewResolver implements ServletResourceViewResolver, PostCreateBean {

    protected final Log log = LogFactory.get(this.getClass());

    protected @Inject @M ViewStrategy viewStrategy;

    protected String prefix = "";
    protected String suffix = "";

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public void postCreate(BeanFactory beanFactory) throws Throwable {
        if(Strings.isEmpty(prefix)){
            prefix = webConfig.getViewsLocation();
        }
    }

    @Override
    public View resolveView(String location, String viewName, Locale locale, Resource dir) throws Throwable {
        return doResolveView(location, viewName, locale, dir);
    }

    @Override
    public View resolveView(String viewName, Locale locale) throws Throwable {
        return doResolveView(prefix, viewName, locale, null);
    }

    protected View doResolveView(String prefix, String viewName, Locale locale, Resource dir) throws Throwable {
        R resource = getLocaleResource(prefix, suffix, viewName, locale, dir);
        if(null == resource || !resource.exists()){

            String[] candidateViewPaths = viewStrategy.getCandidateViewPaths(viewName);

            for(String candidateViewPath : candidateViewPaths){
                resource = getLocaleResource(prefix,suffix, candidateViewPath, locale, dir);

                if(null != resource && resource.exists()){
                    break;
                }
            }
        }

        if(null == resource || !resource.exists()){
            return null;
        }

        return loadView(prefix, suffix, viewName, locale, resource.getPath(), resource);
    }

    protected R getLocaleResource(String prefix, String suffix, String viewPath, Locale locale) {
        return getLocaleResource(prefix, suffix, viewPath, locale, null);
    }

    protected R getLocaleResource(String prefix, String suffix, String viewPath, Locale locale, Resource dir) {
        String[] paths;

        if (null != dir) {
            paths = Locales.getLocalePaths(locale, Strings.trimStart(viewPath, '/'), suffix);
            for(String path : paths){
                Resource r = Resources.createRelativeResource(dir, path);
                if(null != r && r.exists()){
                    return (R) r;
                }
            }
        }

        String pathPrefix = Paths.suffixWithoutSlash(prefix) + Paths.prefixWithSlash(viewPath);

        if(!Strings.isEmpty(suffix) && pathPrefix.endsWith(suffix)){
            return loadResource(pathPrefix);
        }

        paths = Locales.getLocalePaths(locale, pathPrefix, suffix);
        for(String path : paths){
            R r = loadResource(path);
            if(null != r && r.exists()){
                return r;
            }
        }

        return null;
    }

    protected abstract R loadResource(String path);

    protected abstract View loadView(String prefix, String suffix, String viewName, Locale locale, String resourcePath, R resource);

}
