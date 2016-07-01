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
package leap.orm.mapping;

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.lang.resource.ResourceSet;
import leap.orm.metadata.MetadataException;

public class ClassMapper implements Mapper {
	
    protected @Inject @M AppConfig       config;
    protected @Inject @M MappingStrategy mappingStrategy;
	
	@Override
    public void loadMappings(final MappingConfigContext context) throws MetadataException {
		ResourceSet resources = config.getResources();
		
		resources.processClasses((cls) -> {

			if(mappingStrategy.isExplicitEntity(context,cls)){

                if(!mappingStrategy.isContextModel(context.getOrmContext(), cls)) {
                    return;
                }

                context.addEntityMapping(mappingStrategy.createEntityClassMapping(context, cls));
            }
		});
		
    }
	
}