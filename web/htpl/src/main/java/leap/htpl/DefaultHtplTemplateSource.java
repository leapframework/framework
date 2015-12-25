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
package leap.htpl;

import java.util.Locale;

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.annotation.R;
import leap.core.cache.Cache;

public class DefaultHtplTemplateSource extends AbstractCachingHtplTemplateSource implements HtplTemplateSource {

    @Inject(name = "htpl")
    protected @M Cache<Object, HtplTemplate> cache;

    @Inject
    protected @R HtplTemplateResolver[]      resolvers;
	
	@Override
    protected Cache<Object, HtplTemplate> getCache() {
	    return cache;
    }

	@Override
    protected HtplTemplate resolveTemplate(String name, Locale locale) {
		HtplTemplate t ;
		for(int i=0;i<resolvers.length;i++){
			if((t = resolvers[i].resolveTemplate(name, locale)) != null){
				return t;
			}
		}
		return null;
    }
}
