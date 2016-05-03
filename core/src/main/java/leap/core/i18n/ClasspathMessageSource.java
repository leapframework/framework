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

import leap.core.AppResources;
import leap.core.BeanFactory;
import leap.core.ioc.PostCreateBean;
import leap.lang.Arrays2;
import leap.lang.resource.Resource;

public class ClasspathMessageSource extends ResourceMessageSource implements MessageSource,PostCreateBean {

	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		Resource[] resources =
				Arrays2.concat(AppResources.getAllClasspathResources("messages",".*"),
                               AppResources.getAllClasspathResourcesWithPattern("messages_*",".*"));
	
		super.readFromResources(resources);
    }

}