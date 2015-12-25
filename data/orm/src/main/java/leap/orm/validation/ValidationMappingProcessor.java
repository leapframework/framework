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
package leap.orm.validation;

import java.lang.annotation.Annotation;

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.validation.ValidationManager;
import leap.core.validation.Validator;
import leap.lang.beans.BeanProperty;
import leap.orm.mapping.EntityMappingBuilder;
import leap.orm.mapping.FieldMappingBuilder;
import leap.orm.mapping.MappingProcessorAdapter;
import leap.orm.metadata.MetadataContext;
import leap.orm.metadata.MetadataException;

public class ValidationMappingProcessor extends MappingProcessorAdapter {

	protected @Inject @M ValidationManager validationManager;
	
	@Override
    public void postMappingField(MetadataContext context, EntityMappingBuilder emb, FieldMappingBuilder fmb) throws MetadataException {
		BeanProperty bp = fmb.getBeanProperty();
		
		if(null != bp){
			Annotation[] annotations = bp.getAnnotations();
			
			for(Annotation a : annotations){
				Validator validator = validationManager.tryCreateValidator(a, bp.getType());
				if(null != validator){
					fmb.addValidator(new DefaultFieldValidator(validationManager, validator));
				}
			}
		}
	}
}