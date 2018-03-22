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

import leap.lang.resource.Resource;
import leap.lang.resource.ResourceLoader;

public class LeapResourceLoader implements ResourceLoader {

    private org.springframework.core.io.ResourceLoader wrapped;

    public LeapResourceLoader(org.springframework.core.io.ResourceLoader wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Resource getResource(String location) {
        org.springframework.core.io.Resource resource = wrapped.getResource(location);
        return new LeapResource(resource);
    }

    @Override
    public ClassLoader getClassLoader() {
        return wrapped.getClassLoader();
    }

}
