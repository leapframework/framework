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
package leap.core.i18n;

import leap.core.AppConfig;
import leap.core.AppResource;
import leap.core.AppResources;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.lang.Arrays2;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

public class ClasspathMessageSource extends ResourceMessageSource implements MessageSource,PostCreateBean {

    private static final Log log = LogFactory.get(ClasspathMessageSource.class);

    protected @Inject AppConfig config;

	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
        AppResources ars = AppResources.get(config);

        AppResource[] resources =
                Arrays2.concat(ars.search("messages"),
                               ars.searchAllFiles(new String[]{"messages_*.*"}));

        log.info("Found {} messages files: [{}]", resources.length, Strings.join(resources, ','));
		super.readFromResources(resources);
    }

}