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

package leap.core.config;

import leap.lang.accessor.PropertyGetter;

public class Context {

    private static Context CURRENT;

    public static Context get() {
        if (null == CURRENT) {
            CURRENT = new Context();
        }
        return CURRENT;
    }

    public static void remove() {
        CURRENT = null;
    }

    private InitialProfileResolver initialProfileResolver;
    private PropertyGetter         initialPropertySource;

    public InitialProfileResolver getInitialProfileResolver() {
        return initialProfileResolver;
    }

    public void setInitialProfileResolver(InitialProfileResolver initialProfileResolver) {
        this.initialProfileResolver = initialProfileResolver;
    }

    public PropertyGetter getInitialPropertySource() {
        return initialPropertySource;
    }

    public void setInitialPropertySource(PropertyGetter initialPropertySource) {
        this.initialPropertySource = initialPropertySource;
    }

    protected Context() {

    }

    public interface InitialProfileResolver {

        String[] getProfiles();

        void setProfiles(String... profiles);
    }
}
