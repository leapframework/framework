/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package leap.orm.command;

import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.JoinFieldMapping;
import leap.orm.mapping.RelationMapping;
import leap.orm.query.CriteriaQuery;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultDeleteFullyCommand extends AbstractEntityDaoCommand implements DeleteFullyCommand {

    protected final DefaultDeleteCommand deleteCommand;

	public DefaultDeleteFullyCommand(Dao dao, EntityMapping em, Object id) {
        super(dao, em);
        deleteCommand = new DefaultDeleteCommand(dao, em, id);
    }

	@Override
	public boolean execute() {
        List<RelationMapping> oneToManyRelations = new ArrayList<>();
        for(RelationMapping rm : em.getRelationMappings()) {
            if(rm.isOneToMany()) {
                oneToManyRelations.add(rm);
            }
        }

        if(oneToManyRelations.isEmpty()) {
            return deleteCommand.execute() > 0;
        }else{
            Object id = deleteCommand.idParameter;

            AtomicBoolean result = new AtomicBoolean(false);

            dao.doTransaction((conn) -> {

                for(RelationMapping rm : oneToManyRelations) {
                    //find the inverse many-to-one relations.

                    EntityMapping target =
                            context.getMetadata().getEntityMapping(rm.getTargetEntityName());

                    RelationMapping inverse =
                            target.getRelationMapping(rm.getInverseRelationName());

                    CriteriaQuery query =
                            dao.createCriteriaQuery(target).whereByReference(inverse, id);

                    if(inverse.isOptional()) {
                        //update null
                        Map<String,Object> fields = new LinkedHashMap<>();

                        for(JoinFieldMapping jf : inverse.getJoinFields()) {
                            fields.put(jf.getReferencedFieldName(), null);
                        }

                        query.update(fields);
                    }else {
                        //delete
                        query.delete();
                    }
                }

                result.set(deleteCommand.execute() > 0);
            });

            return result.get();
        }
	}

}