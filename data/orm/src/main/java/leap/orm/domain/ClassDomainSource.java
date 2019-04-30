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
package leap.orm.domain;

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.lang.resource.ResourceSet;

public class ClassDomainSource implements DomainSource {

    protected @Inject AppConfig     config;
    protected @Inject DomainCreator creator;

	@Override
    public void loadDomains(Domains context) {
		ResourceSet resources = config.getResources();
		
		resources.processClasses((c) -> {
			if(c.isAnnotation()){
			    DomainBuilder domain = creator.tryCreateFieldDomainByAnnotation(context, c);
			    if(null != domain) {
			        if(domain.isUnnamed()) {
			            context.addAnnotationType(c, domain.build());
			        }else{
			            context.addDomain(domain.build());
			        }
			    }
			}
		});	
    }

}