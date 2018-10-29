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

package leap.web.api.orm;

import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.RelationMapping;
import leap.web.api.meta.model.MApiModel;

public interface RelationExecutorContext extends ModelExecutorContext {

    /**
     * Required. Returns the api model for returning data.
     */
    MApiModel getTargetApiModel();

    /**
     * Required. Returns the entity for returning data.
     */
    EntityMapping getTargetEntityMapping();

    /**
     * Required. Returns the relation of primary entity.
     */
    RelationMapping getRelation();

    /**
     * Required. Returns the inverse relation of {@link #getRelation()} at target entity.
     */
    RelationMapping getInverseRelation();

    /**
     * Creates a new {@link ModelExecutorContext} for target entity.
     */
    ModelExecutorContext newTargetExecutorContext();
}