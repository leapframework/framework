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
package leap.orm.metadata;

import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.MappingConfigContext;

public interface OrmMetadataManager {

    /**
     * Creates a new {@link OrmMetadata}.
     */
	OrmMetadata createMetadata();

    /**
     * Adds a new entity to the given orm context.
     */
	void createEntity(MetadataContext context, EntityMapping em) throws MetadataException;

    /**
     * Loads default metadata for the given orm context.
     */
	void loadMetadata(OrmContext context) throws MetadataException;

    /**
     * Processing the mappings.
     */
    void processMappings(MappingConfigContext context);
}