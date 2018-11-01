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

import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.lang.servlet.Servlets;

public abstract class AbstractServletOrClassResourceViewResolver extends AbstractResourceViewResolver<Resource> {

    protected String classPathPrefix = "classpath:META-INF/resources";

    @Override
    protected Resource loadResource(String path) {
        Resource resource = Servlets.getResource(servletContext, path);
        if(null != resource && resource.exists()) {
            return resource;
        }
        return Resources.getResource(classPathPrefix + path);
    }

}