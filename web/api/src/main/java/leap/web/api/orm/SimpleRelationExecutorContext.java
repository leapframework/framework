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

import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.RelationMapping;
import leap.web.api.Api;
import leap.web.api.meta.model.MApiModel;

public class SimpleRelationExecutorContext extends SimpleModelExecutorContext implements RelationExecutorContext {

    private final MApiModel       tam;
    private final EntityMapping   tem;
    private final RelationMapping rm;
    private final RelationMapping irm;

    public SimpleRelationExecutorContext(Api api, Dao dao,
                                         MApiModel am, MApiModel tam,
                                         EntityMapping em, EntityMapping tem,
                                         RelationMapping rm, RelationMapping irm) {
        super(api, am, dao, em);
        this.tam = tam;
        this.tem = tem;
        this.rm  = rm;
        this.irm = irm;
    }

    @Override
    public MApiModel getTargetApiModel() {
        return tam;
    }

    @Override
    public EntityMapping getTargetEntityMapping() {
        return tem;
    }

    @Override
    public RelationMapping getRelation() {
        return rm;
    }

    @Override
    public RelationMapping getInverseRelation() {
        return irm;
    }

    @Override
    public ModelExecutorContext newTargetExecutorContext() {
        return new SimpleModelExecutorContext(ac, amd, tam, dao, tem);
    }
}
