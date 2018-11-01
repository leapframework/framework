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
package leap.web.view;

import leap.lang.resource.Resource;
import leap.lang.servlet.ServletResource;
import leap.web.App;
import leap.web.Request;

public abstract class AbstractResourceView<R extends Resource> extends AbstractView {

    public static final String VIEW_RESOURCE_ATTRIBUTE = AbstractResourceView.class.getName() + "$view_resource";

    protected final R      resource;
    protected final String resourcePath;

    public AbstractResourceView(App app, String path, R resource) {
        super(app, path);
        this.resource = resource;
        this.resourcePath = resource instanceof ServletResource  ?
                                ((ServletResource) resource).getPathWithinContext() :
                                (
                                    resource.hasClasspath() ? resource.getClasspath() : resource.getURLString()
                                );
    }

    @Override
    protected void exposeHelpers(Request request) throws Exception {
        request.setAttribute(VIEW_RESOURCE_ATTRIBUTE, resource);
    }

    @Override
    public String toString() {
        return resourcePath;
    }
}