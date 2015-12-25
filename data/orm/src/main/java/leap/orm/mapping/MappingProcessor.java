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

import leap.orm.metadata.MetadataContext;
import leap.orm.metadata.MetadataException;

public interface MappingProcessor {
	
	void preMapping(MetadataContext context,EntityMappingBuilder emb) throws MetadataException;
	
	void preMappingEntity(MetadataContext context,EntityMappingBuilder emb) throws MetadataException;
	
	void postMappingEntity(MetadataContext context,EntityMappingBuilder emb) throws MetadataException;
	
	void preMappingField(MetadataContext context,EntityMappingBuilder emb,FieldMappingBuilder fmb) throws MetadataException;
	
	void postMappingField(MetadataContext context,EntityMappingBuilder emb,FieldMappingBuilder fmb) throws MetadataException;
	
	void preMappingRelation(MetadataContext context,EntityMappingBuilder emb,RelationMappingBuilder rmb) throws MetadataException;
	
	void postMappingRelation(MetadataContext context,EntityMappingBuilder emb,RelationMappingBuilder rmb) throws MetadataException;

	void postMapping(MetadataContext context,EntityMappingBuilder emb) throws MetadataException;

}